package edu.northwestern.ssa;

import org.archive.io.ArchiveReader;
import org.archive.io.warc.WARCReaderFactory;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
                    System.out.println("No new crawl data");
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
}
