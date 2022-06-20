package com.nosqldb.readserver.requesthandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.database.dao.DocumentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * ReadHandler service is responsible for handling and sending all the queries to the query service.
 * it uses a QueryValidator instance to check the if its a valid query, then sends it to the query service
 * to be executed, and returns the queried data.
 */
@Service
public class ReadHandler {
    @Autowired
    private DocumentDao dao;
    private ObjectMapper mapper;

    public ReadHandler(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ObjectNode indexQuery(String DB, String colName, ObjectNode query) throws IOException {
        ObjectNode ret = mapper.createObjectNode();
        if (!dao.getSchema(DB).has(colName)) {
            ret.put("status", "QUERY FAILED");
            ret.put("error", "collection does not exist!");
            return ret;
        }
        JsonNode schema = dao.getSchema(DB).get(colName);
        Iterator<String> it = query.fieldNames();
        while (it.hasNext()) {
            String index = it.next();
            if (!isAvailableIndex(index, (ArrayNode) schema.get("index"))) {
                ret.put("status", "QUERY FAILED");
                ret.put("error", colName + " is not indexed on: " + index);
                return ret;
            }
        }

        it = query.fieldNames();
        String firstIndex = it.next();
        List<String> allObjects = dao.getIndexTable(DB, colName, firstIndex).get(query.get(firstIndex));
        while (it.hasNext()) {
            String index = it.next();
            List<String> objects = dao.getIndexTable(DB, colName, index).get(query.get(index));
            allObjects.retainAll(objects);
        }

        ObjectNode collection = dao.getCollection(DB, colName);
        ArrayNode content = mapper.createArrayNode();
        for (String i : allObjects)
            content.add(collection.get(i));

        ret.put("status", "GOOD QUERY");
        ret.set("content", content);
        return ret;
    }

    public ObjectNode idQuery(String DB, String colName, String id) throws IOException {
        ObjectNode ret = mapper.createObjectNode();
        if (!dao.getSchema(DB).has(colName)) {
            ret.put("status", "QUERY FAILED");
            ret.put("error", "collection does not exist!");
            return ret;
        }
        ret.put("status", "GOOD QUERY");
        JsonNode obj = dao.getCollection(DB, colName).get(id);
        if (obj != null) ret.set("content", mapper.createArrayNode().add(obj));
        else ret.set("content", mapper.createArrayNode());
        return ret;
    }

    public ObjectNode searchQuery(String DB, String colName, ObjectNode query) throws IOException {
        ObjectNode ret = mapper.createObjectNode();
        if (!dao.getSchema(DB).has(colName)) {
            ret.put("status", "QUERY FAILED");
            ret.put("error", "collection does not exist!");
            return ret;
        }
        ArrayNode content = mapper.createArrayNode();
        Iterator<JsonNode> it = dao.getCollection(DB, colName).elements();
        while (it.hasNext()) {
            JsonNode next = it.next();
            if (testMatch((ObjectNode) next, query))
                content.add(next);
        }
        ret.put("status", "GOOD QUERY");
        ret.set("content", content);
        return ret;
    }

    public ObjectNode getAll(String DB, String colName) throws IOException {
        ObjectNode ret = mapper.createObjectNode();
        if (!dao.getSchema(DB).has(colName)) {
            ret.put("status", "QUERY FAILED");
            ret.put("error", "collection does not exist!");
            return ret;
        }
        ArrayNode content = mapper.createArrayNode();
        Iterator<JsonNode> it = dao.getCollection(DB, colName).elements();
        while (it.hasNext()) content.add(it.next());
        ret.put("status", "GOOD QUERY");
        ret.set("content", content);
        return ret;
    }

    private boolean testMatch(ObjectNode document, ObjectNode query) {
        for (Iterator<String> it = query.fieldNames(); it.hasNext(); ) {
            String i = it.next();
            if (!document.has(i))
                return false;
            if (!document.get(i).equals(query.get(i)))
                return false;
        }
        return true;
    }

    private boolean isAvailableIndex(String index, ArrayNode schemaIndexes) {
        for (JsonNode i : schemaIndexes)
            if (i.asText().equals(index))
                return true;
        return false;
    }


}
