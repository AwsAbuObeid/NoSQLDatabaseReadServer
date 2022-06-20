package com.nosqldb.readserver.requesthandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.database.dao.DocumentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Iterator;

/**
 * WriteHandler service is responsible for handling the write requests that come from the
 * main database controller, it finds the needed operation and executes it.
 * it first caches the collection by calling the get function on the proxy,
 * and then does the write op, which then evicts the affected cache collection from the cache
 */
@Service
public class WriteHandler {

    @Autowired
    DocumentDao dao;

    public synchronized void executeWrite(ObjectNode body) throws IOException {
        Operation op= Enum.valueOf(Operation.class,body.get("op").asText());

        switch (op) {
            case ADD_DOCUMENT:
                addDocument(body.get("DB").asText(), body.get("collection").asText(), body.get("document"));
                break;
            case DELETE_DOCUMENT:
                deleteDocument(body.get("DB").asText(), body.get("collection").asText(), body.get("doc_ID").asText());
                break;
            case DELETE_COLLECTION:
                deleteCollection(body.get("DB").asText(), body.get("collection").asText());
                break;
            case SET_SCHEMA:
                setSchema(body.get("DB").asText(), body.get("schema"));
                break;
            case DELETE_DATABASE:
                deleteDatabase(body.get("DB").asText());
                break;
            case ADD_ATTRIBUTE:
                addAttribute(body.get("DB").asText(), body.get("collection").asText(),
                        body.get("attribName").asText(),body.get("attribute"));
                break;
            case ADD_COLLECTION:
                addCollection(body.get("DB").asText(), body.get("collection").asText(), body.get("schema"));
                break;
        }
    }

    private void addDocument(String db, String collection, JsonNode document) throws IOException {
        dao.getCollection(db,collection);
        dao.addDocument(db,collection, (ObjectNode) document);
    }
    private void addCollection(String db, String collection, JsonNode schema) throws IOException {
        dao.getSchema(db);
        dao.addCollection(db,collection,schema);
    }
    private void deleteCollection(String db, String collection) throws IOException {
        dao.getCollection(db,collection);
        dao.deleteCollection(db,collection);
    }
    private void deleteDocument(String db, String collection, String doc_ID) throws IOException {
        dao.getCollection(db,collection);
        dao.deleteDocument(db,collection,doc_ID);
    }
    private void setSchema(String db, JsonNode schema) throws IOException {
        dao.getSchema(db);
        dao.setSchema(db,(ObjectNode)schema);
    }
    private void deleteDatabase(String db) throws IOException {
        dao.deleteDatabase(db);
    }
    private void addAttribute(String db, String colName, String attribName, JsonNode attribute) throws IOException {
        ObjectNode schema=dao.getSchema(db);
        ((ObjectNode)schema.get(colName).get("properties")).set(attribName,attribute);
        dao.setSchema(db,schema);
        if(attribute.has("required")&&attribute.get("required").asBoolean()) {
            ObjectNode collection = dao.getCollection(db, colName);
            for (Iterator<String> it = collection.fieldNames(); it.hasNext(); ) {
                String i = it.next();
                ((ObjectNode)collection.get(i)).set(attribName,attribute.get("default"));
            }
            dao.setCollection(db,colName,collection);
        }
    }
}
