package com.nosqldb.readserver.requesthandlers.queryutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nosqldb.readserver.database.dao.DocumentDao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * QueryService Class is responsible for executing queries, uses the DocumentDao
 * proxy to get the data, then sorts the data based on the query.
 *
 * it supports 4 different types of queries:
 * - queryAll, gets all the documents in a collection.
 * - queryIndex, gets documents on an index or multiple indexes or an Embedded index
 *                                               (an index inside an Embedded object)
 * - queryContains, gets the documents that include specific attributes.
 * - queryById, fast query by document ID.
 */
@Service
public class QueryService {

    @Autowired
    private DocumentDao dao;
    private final ObjectMapper mapper;

    public QueryService() {
        mapper = new ObjectMapper();
    }

    public ArrayNode queryAll(String DB, String colName) throws IOException {
        return mapper.createArrayNode().addAll(dao.getDocuments(DB, colName).values());
    }

    public ArrayNode queryIndex(String DB, String colName, JsonNode index) throws IOException {
        ArrayNode out = mapper.createArrayNode();
        if (index.has("_id")) {
            ObjectNode byID = queryById(DB, colName, index.get("_id").asText());
            if (byID != null) out.add(byID);
        } else {
            ArrayNode all = queryAll(DB, colName);
            for (JsonNode j : all)
                if (testMatch(j, index)) out.add(j);
        }
        return out;
    }

    public ArrayNode queryContains(String DB, String colName, ArrayNode contain) throws IOException {
        ArrayNode all = queryAll(DB, colName);
        ArrayNode out = mapper.createArrayNode();
        boolean match;
        for (JsonNode j : all) {
            match = true;
            for (JsonNode i : contain)
                if (!j.has(i.asText()))
                    match = false;
            if (match)
                out.add(j);
        }
        return out;
    }

    public ObjectNode queryById(String DB, String collName, String id) throws IOException {
        HashMap<String, ObjectNode> collection = dao.getDocuments(DB, collName);
        return collection.get(id);
    }

    private boolean testMatch(JsonNode document, JsonNode index) {
        for (Iterator<String> it = index.fieldNames(); it.hasNext(); ) {
            String i = it.next();
            if (!document.has(i))
                return false;
            if (index.get(i).isObject()) {
                if (!testMatch(document.get(i),index.get(i)))
                    return false;
            } else if (!document.get(i).equals(index.get(i)))
                return false;
        }
        return true;
    }
}
