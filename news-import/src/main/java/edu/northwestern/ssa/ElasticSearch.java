package edu.northwestern.ssa;

import org.json.JSONObject;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpMethod;

import java.io.IOException;
import java.util.Optional;

public class ElasticSearch extends AwsSignedRestRequest {
    ElasticSearch(String serviceName) {
        super(serviceName);
    }

    // Create index method.
    public HttpExecuteResponse createIndex() throws IOException {
        return restRequest(SdkHttpMethod.PUT, System.getenv("ELASTIC_SEARCH_HOST"), System.getenv("ELASTIC_SEARCH_INDEX"), Optional.empty());
    }

    // POST document method.
    public HttpExecuteResponse postDoc(String title, String txt, String url) throws IOException {
        JSONObject body = new JSONObject();
        body.put("title", title);
        body.put("txt", txt);
        body.put("url", url);
        return restRequest(SdkHttpMethod.POST, System.getenv("ELASTIC_SEARCH_HOST"), System.getenv("ELASTIC_SEARCH_INDEX") + "/_doc/", Optional.empty(), Optional.of(body));
    }
}
