package com.nosqldb.readserver.database.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.database.fileservice.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * FileDao is an implementation of the DocumentDao interface, it represents
 * the RealSubject part of the proxy pattern, it uses a file service to access
 * the DBs files and do the read and write operations.
 */
@Repository
public class FileDao implements DocumentDao {

    private final ObjectMapper mapper;

    @Autowired
    private FileService fileService;

    Logger logger = LoggerFactory.getLogger(FileDao.class);

    private FileDao() {
        mapper = new ObjectMapper();
    }

    @Override
    public ObjectNode getCollection(String DB, String colName) throws IOException {
        logger.info("Opening File: " + DB + "/" + colName + ".json");

        return (ObjectNode) mapper.readTree(fileService.getCollectionFile(DB, colName));
    }

    @Override
    public Hashtable<JsonNode, List<String>> getIndexTable(String DB, String colName, String indexName) throws IOException {
        logger.info("Opening File: " + DB + "/" + colName + ".index");

        Hashtable<JsonNode, List<String>> ret = new Hashtable<>();
        File indexFile = fileService.getIndexFile(DB, colName, indexName);
        ArrayNode indexTable = (ArrayNode) mapper.readTree(indexFile);
        for (JsonNode i : indexTable) {
            List<String> objects = new ArrayList<>();
            for (JsonNode j : i.get("objects"))
                objects.add(j.asText());
            ret.put(i.get("value"), objects);
        }
        return ret;
    }

    @Override
    public ObjectNode getSchema(String DB) throws IOException {
        logger.info("Opening File: " + DB + "/schema.json");
        try {
            return (ObjectNode) mapper.readTree(fileService.getSchemaFile(DB));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public void addDocument(String DB, String colName, ObjectNode document) throws IOException {
        logger.info("Writing To File: " + DB + "/" + colName + ".json");
        File collectionFile = fileService.getCollectionFile(DB, colName);
        ObjectNode coll = (ObjectNode) mapper.readTree(fileService.getCollectionFile(DB, colName));
        coll.set(document.get("_id").asText(), document);
        mapper.writeValue(collectionFile, coll);

        for (JsonNode indexName : getSchema(DB).get(colName).get("index")) {
            if (!document.has(indexName.asText()))
                continue;
            JsonNode indexValue = document.get(indexName.asText());
            File indexFile = fileService.getIndexFile(DB, colName, indexName.asText());
            ArrayNode indexTable = (ArrayNode) mapper.readTree(indexFile);

            indexDocument(indexTable, indexValue, document);

            mapper.writeValue(indexFile, indexTable);
        }
    }

    private void indexDocument(ArrayNode indexTable, JsonNode index, ObjectNode document) {
        boolean added = false;
        for (JsonNode i : indexTable)
            if (i.get("value").equals(index)) {
                ((ArrayNode) i.get("objects")).add(document.get("_id"));
                added = true;
            }
        if (!added) {
            ObjectNode row = mapper.createObjectNode();
            row.set("value", index);
            row.set("objects", mapper.createArrayNode().add(document.get("_id")));
            indexTable.add(row);
        }
    }

    @Override
    public void setCollection(String DB, String colName, ObjectNode collection) throws IOException {
        logger.info("Writing To File: " + DB + "/" + colName + ".json");
        mapper.writeValue(fileService.getCollectionFile(DB, colName), collection);
    }

    @Override
    public void addCollection(String DB, String colName, JsonNode schema) throws IOException {
        logger.info("Writing To File: " + DB + "/" + colName + ".json");
        mapper.writeValue(fileService.getCollectionFile(DB, colName), mapper.createObjectNode());
        for (JsonNode indexName : schema.get("index"))
            mapper.writeValue(fileService.getIndexFile(DB, colName, indexName.asText()), mapper.createArrayNode());
        ObjectNode DBschema = getSchema(DB);
        DBschema.set(colName, schema);
        setSchema(DB, DBschema);
    }

    @Override
    public void deleteDocument(String DB, String colName, String doc_ID) throws IOException {
        File collectionFile = fileService.getCollectionFile(DB, colName);
        ObjectNode coll = (ObjectNode) mapper.readTree(collectionFile);
        coll.remove(doc_ID);
        mapper.writeValue(collectionFile, coll);
        ObjectNode schema = getSchema(DB);

        for (JsonNode indexName : getSchema(DB).get(colName).get("index")) {
            JsonNode indexValue = coll.get(doc_ID).get(schema.get(colName).get("index").get(0).asText());
            if (indexValue == null) continue;
            File indexFile = fileService.getIndexFile(DB, colName, indexName.asText());
            ArrayNode indexTable = (ArrayNode) mapper.readTree(indexFile);

            deleteIndexedDocument(indexTable, doc_ID, indexValue);
            mapper.writeValue(indexFile, indexTable);
        }
    }

    private void deleteIndexedDocument(ArrayNode indexTable, String doc_ID, JsonNode index) {
        for (JsonNode i : indexTable) {
            if (i.get("value").equals(index)) {
                JsonNode objects = i.get("objects");
                ((ObjectNode) i).remove("objects");
                ArrayNode newRow = mapper.createArrayNode();
                for (JsonNode j : objects)
                    if (!j.asText().equals(doc_ID))
                        newRow.add(j);
                ((ObjectNode) i).set("objects", newRow);
            }
        }
    }

    @Override
    public void setSchema(String DB, ObjectNode schema) throws IOException {
        logger.info("Writing To File: " + DB + "/schema.json");
        mapper.writeValue(fileService.getSchemaFile(DB), schema);
    }

    @Override
    public void deleteCollection(String DB, String colName) throws IOException {
        ObjectNode DBschema = getSchema(DB);
        if (DBschema.get(colName).has("index"))
            for (JsonNode indexName : DBschema.get(colName).get("index"))
                fileService.getIndexFile(DB, colName, indexName.asText()).delete();
        DBschema.remove(colName);
        setSchema(DB, DBschema);
        fileService.getCollectionFile(DB, colName).delete();
    }

    @Override
    public void deleteDatabase(String DB) throws IOException {
        fileService.deleteDB(DB);
    }

}
