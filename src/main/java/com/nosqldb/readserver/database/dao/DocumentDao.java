package com.nosqldb.readserver.database.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;


/**
 * DocumentDao interface is the subject of the proxy pattern that is used for caching,
 * its implemented by two classes FileDao and CachedDao
 */
public interface DocumentDao {
    ObjectNode getCollection(String DB, String colName) throws IOException;

    Hashtable<JsonNode, List<String>> getIndexTable(String DB, String colName, String indexName) throws IOException;

    void setCollection(String DB, String colName, ObjectNode collection) throws IOException;

    void deleteCollection(String DB, String colName) throws IOException;

    void addCollection(String db, String collection, JsonNode schema) throws IOException;

    ObjectNode getSchema(String DB) throws IOException;

    void setSchema(String DB, ObjectNode schema) throws IOException;

    void addDocument(String DB, String colName, ObjectNode document) throws IOException;

    void deleteDocument(String DB, String colName, String doc_ID) throws IOException;

    void deleteDatabase(String DB) throws IOException;

}
