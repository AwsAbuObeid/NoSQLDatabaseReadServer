package com.nosqldb.readserver.requesthandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.requesthandlers.queryutils.QueryService;
import com.nosqldb.readserver.requesthandlers.queryutils.QueryValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ReadHandler service is responsible for handling and sending all the queries to the query service.
 * it uses a QueryValidator instance to check the if its a valid query, then sends it to the query service
 * to be executed, and returns the queried data.
 */
@Service
public class ReadHandler {
    @Autowired
    private QueryValidator queryValidator;
    @Autowired
    private QueryService service;
    private ObjectMapper mapper;

    public ReadHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectNode executeQuery(String DB, JsonNode query) throws IOException {

        ObjectNode ret= mapper.createObjectNode();
        ArrayNode content;
        String queryStatus= queryStatus(DB,query);

        if(queryStatus.equals("OK")) {
            ret.put("status", "GOOD QUERY");
            if (query.has("index"))
                 content=service.queryIndex(DB,query.get("collection").asText(), query.get("index"));
            else if(query.has("contains"))
                 content=service.queryContains(DB,query.get("collection").asText(), ((ArrayNode) query.get("contains")));
            else content=service.queryAll(DB,query.get("collection").asText());

            ret.put("size",content.size());
            ret.set("content", content);

        }else{
            ret.put("status", "QUERY FAILED");
            ret.put("error",queryStatus);
        }
        return ret;
    }

    public String queryStatus(String DB, JsonNode query) throws IOException {
        if(!queryValidator.isValidFormat(query))
            return "Bad query format";
        if(!queryValidator.resourceExists(DB, query.get("collection").asText()))
            return "Collection "+query.get("collection").asText()+" doesnt Exist!";
        if(!query.has("index"))
            return "OK";
        return queryValidator.isValidIndexQuery(DB, query.get("collection").asText(),query.get("index"));
    }


}
