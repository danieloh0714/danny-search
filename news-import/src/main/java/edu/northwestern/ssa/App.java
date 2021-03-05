package edu.northwestern.ssa;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReaderFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        /*
        Download a WARC.
         */

        // Create an S3Client object.
        S3Client s3 = S3Client.builder()
                        .region(Region.US_EAST_1)
                        .overrideConfiguration(ClientOverrideConfiguration.builder()
                                .apiCallTimeout(Duration.ofMinutes(30)).build())
                        .build();

        // Get the latest crawl data key.
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket("commoncrawl")
                .prefix("crawl-data/CC-NEWS/2021")
                .build();
        ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);
        List<S3Object> listContents = listResponse.contents();
        String latestKey = listContents.get(listContents.size() - 1).key();

        // Check if the latest key has already been uploaded. If so, then return.
        Boolean isNewCrawlData = isNewCrawlData(latestKey);
        if (!isNewCrawlData) {
            s3.close();
            return;
        }

        // If there is new crawl data, then continue on as normal.
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket("commoncrawl")
                .key(latestKey)
                .build();

        // Create file to store crawl data.
        File f = new File("data.warc");

        // Execute the request.
        s3.getObject(request, ResponseTransformer.toFile(f));

        // Close the S3 object.
        s3.close();

        /*
        Parse the WARC file.
         */

        // Create an ArchiveReader object.
        ArchiveReader reader = WARCReaderFactory.get(f);

        // Create a new Elasticsearch index.
        ElasticSearch es = new ElasticSearch("es");
        HttpExecuteResponse esIndexResponse = es.createIndex();
        esIndexResponse.responseBody().get().close();

        int count = 0;

        // Iterate through the ArchiveRecord objects.
        for (ArchiveRecord record : reader) {
            // Get the WARC type.
            Object warcType = record.getHeader().getHeaderValue("WARC-Type");

            if (warcType.equals("response")) {
                // Use the read() method to iterate through the record.
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = record.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                String contents = buffer.toString(String.valueOf(StandardCharsets.UTF_8));

                // The first occurrence of "\r\n\r\n" (blank new line) marks the transition to HTML.
                int firstIndex = contents.indexOf("\r\n\r\n");

                // Get the HTML.
                String html = contents.substring(firstIndex + 4);

                /*
                Parse the HTML and post the <url, title, txt> to Elasticsearch if in English.
                 */

                try {
                    // Create a Document object from the HTML string extracted above.
                    Document doc = Jsoup.parse(html);

                    // Get the language.
                    String lang = getLang(doc);

                    if (lang.equals("en")) {
                        // Extract the document title.
                        String title = getTitle(doc);

                        // Extract the plain text.
                        String txt = getTxt(doc);

                        // Get the URL of the page.
                        String url = record.getHeader().getUrl();

                        // Post the document to the Elasticsearch index.
                        postDocument(es, title, txt, url);

                        count++;
                        if (count % 100 == 0) {
                            System.out.println(count);
                        }
                    }
                } catch (Exception e) {
                    // Ignore the exception.
                }
            }
        }

        // Close the ElasticSearch object.
        es.close();

        // Delete the downloaded crawl data file.
        f.deleteOnExit();

        System.out.println("Done.");
    }

    private static Boolean isNewCrawlData(String latestKey) {
        try {
            File latestCrawl = new File("latest-crawl.txt");
            if (latestCrawl.createNewFile()) {
                System.out.println("Getting new crawl data.");
                writeLatestCrawl(latestCrawl, latestKey);
                return true;
            } else {
                String lastCrawl = readLastCrawl(latestCrawl);
                if (lastCrawl.equals(latestKey)) {
                    System.out.println("No new crawl data.");
                    return false;
                } else {
                    System.out.println("Getting new crawl data.");
                    writeLatestCrawl(latestCrawl, latestKey);
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static void writeLatestCrawl(File latestCrawl, String latestKey) {
        try {
            FileWriter myWriter = new FileWriter(latestCrawl, false);
            myWriter.write(latestKey);
            myWriter.close();
        } catch (Exception e) {
            // Ignore the exception.
        }
    }

    private static String readLastCrawl(File latestCrawl) {
        try {
            String lastCrawl = "";
            Scanner myReader = new Scanner(latestCrawl);
            while (myReader.hasNextLine()) {
                lastCrawl = myReader.nextLine();
            }
            myReader.close();
            return lastCrawl;
        } catch (Exception e) {
            return "";
        }
    }

    private static String getLang(Document doc) {
        try {
            return doc.select("html").attr("lang");
        } catch (Exception e) {
            return "";
        }
    }

    private static String getTitle(Document doc) {
        try {
            return doc.title();
        } catch (Exception e) {
            return "";
        }
    }

    private static String getTxt(Document doc) {
        try {
            return doc.text();
        } catch (Exception e) {
            return "";
        }
    }

    private static void postDocument(ElasticSearch es, String title, String txt, String url) throws IOException {
        HttpExecuteResponse postDocResponse;

        // Keep trying to post document until it succeeds.
        while (true) {
            try {
                postDocResponse = es.postDoc(title, txt, url);
                break;
            } catch (Exception e) {
                // Ignore the exception.
            }
        }

        postDocResponse.responseBody().get().close();
    }
}
