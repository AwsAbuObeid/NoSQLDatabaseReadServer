package com.nosqldb.readserver.requesthandlers.queryutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.database.dao.DocumentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;


/**
 * ValidationService class is an implementation of QueryValidator interface, its
 * responsible for validating each query on the database schema, and checking the query
 * format.
 */
@Service
public class ValidationService implements QueryValidator {

    @Autowired
    private DocumentDao dao;

    @Override
    public String isValidIndexQuery(String DB, String collection,JsonNode index) throws IOException {
        ObjectNode schema=(ObjectNode) dao.getSchema(DB).get(collection).get("properties");
        return testIndexObject(index,schema);
    }

    @Override
    public boolean isValidFormat(JsonNode query) {
        if(!query.has("collection")||!(query.get("collection").isTextual()))
            return false;
        if(query.has("index")&&!(query.get("index").isObject()))
            return false;
        if(query.has("contains")) {
            JsonNode contains=query.get("contains");
            if (!contains.isArray()) return false;
            else for (JsonNode i :contains)
                if(!i.isTextual())
                    return false;
        }
        return query.size() <= 2;
    }

    @Override
    public boolean resourceExists(String DB, String collection) throws IOException {
        return dao.getSchema(DB).has(collection);
    }

    private String testIndexObject(JsonNode index, JsonNode schemaObj){
        for (Iterator<String> it = index.fieldNames(); it.hasNext(); ) {
            String i = it.next();
            if (!schemaObj.has(i))
                return "Key: "+i+" Doesnt Exist";
            if(!(index.get(i).isObject() == schemaObj.get(i).get("type").asText().equals("object")))
                return "Wrong Key Type :"+i;
            if(schemaObj.get(i).get("type").asText().equals("array"))
                return "Wrong Key Type :"+i;
            if (index.get(i).isObject()) {
                String response= testIndexObject(index.get(i),schemaObj.get(i).get("properties"));
                if (!response.equals("OK"))
                    return response;
            }
        }
        return "OK";
    }
}
