package edu.northwestern.ssa.api;

import edu.northwestern.ssa.AwsSignedRestRequest;
import edu.northwestern.ssa.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpMethod;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Path("/search")
public class Search {

    /** when testing, this is reachable at http://localhost:8080/api/search?query=hello */
    @GET
    public Response getMsg(
            @QueryParam("query") String q,
            @QueryParam("language") String language,
            @QueryParam("date") String date,
            @QueryParam("count") String count,
            @QueryParam("offset") String offset
    ) throws IOException {
        // If query is missing, then return a 400 response.
        if (q == null) {
            return Response.status(400).type("application/json").entity("'query' is missing from url.")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        // If count or offset are not integers, then return a 400 response.
        try {
            if (count != null) {
                Integer.parseInt(count);
            }
            if (offset != null) {
                Integer.parseInt(offset);
            }
        } catch (Exception e) {
            String entity = "Invalid number passed as a query parameter to 'offset' or 'count'.";
            return Response.status(200).type("application/json").entity(entity)
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        // Initialize results JSONObject to return.
        JSONObject results = new JSONObject();

        // If query is blank, then return empty body.
        if (q.equals("")) {
            results.put("returned_results", 0);
            results.put("articles", new JSONArray());
            results.put("total_results", 0);
            return Response.status(200).type("application/json").entity(results.toString(4))
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        // Create Elasticsearch instance.
        AwsSignedRestRequest es = new AwsSignedRestRequest("es");

        // Make GET request and get response.
        HttpExecuteResponse response = makeGetRequest(es, q, language, date, count, offset);

        // Get response body.
        Optional<AbortableInputStream> responseBodyStream = response.responseBody();
        JSONObject responseBody = getResponseBody(responseBodyStream);

        // Close Elasticsearch instance.
        es.close();

        // Get necessary information from response body.
        int totalResults = getTotalResults(responseBody);

        results.put("total_results", totalResults);

        return Response.status(200).type("application/json").entity(results.toString(4))
                .header("Access-Control-Allow-Origin", "*").build();
    }

    private static HttpExecuteResponse makeGetRequest(
            AwsSignedRestRequest es,
            String q,
            String language,
            String date,
            String count,
            String offset
    ) throws IOException {
        // Initialize GET request parameters.
        SdkHttpMethod method = SdkHttpMethod.GET;
        String host = Config.getParam("ELASTIC_SEARCH_HOST");
        String path = Config.getParam("ELASTIC_SEARCH_INDEX") + "/_search";
        Map<String, String> queryParameters = buildQueryParameters(q, language, date, count, offset);

        // Make request and return response.
        return es.restRequest(method, host, path, Optional.of(queryParameters));
    }

    private static Map<String, String> buildQueryParameters(
            String q,
            String language,
            String date,
            String count,
            String offset
    ) {
        Map<String, String> queryParameters = new HashMap<>();

        // Query.
        String query = "txt:(" + q.replace(" ", " AND ") + ")";

        // Language.
        if (language != null) {
            query += " AND lang:" + language;
        }

        // Date.
        if (date != null) {
            query += " AND date:" + date;
        }

        queryParameters.put("q", query);

        // Count.
        if (count != null) {
            queryParameters.put("size", count);
        }

        // Offset.
        if (offset != null) {
            queryParameters.put("from", offset);
        }

        return queryParameters;
    }

    private static JSONObject getResponseBody(Optional<AbortableInputStream> responseBodyStream) {
        try {
            AbortableInputStream stream = responseBodyStream.get();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = stream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] byteArray = buffer.toByteArray();
            String responseText = new String(byteArray, StandardCharsets.UTF_8);
            return new JSONObject(responseText);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static int getTotalResults(JSONObject responseBody) {
        JSONObject hits = (JSONObject) responseBody.get("hits");
        JSONObject total = (JSONObject) hits.get("total");
        return (int) total.get("value");
    }
}
