package edu.northwestern.ssa.api;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;

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

        JSONArray results = new JSONArray();
        results.put("hello world!");
        results.put(q);
        return Response.status(200).type("application/json").entity(results.toString(4))
                // below header is for CORS
                .header("Access-Control-Allow-Origin", "*").build();
    }
}
