package com.nosqldb.readserver.database.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.database.fileservice.FileService;
import com.nosqldb.readserver.database.fileservice.FileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 *  FileDao is an implementation of the DocumentDao interface, it represents
 *  the RealSubject part of the proxy pattern, it uses a file service to access
 *  the DBs files and do the read and write operations.
 */
@Repository
public class FileDao implements DocumentDao {

    private final ObjectMapper mapper;

    @Autowired
    private FileService fileService;

    Logger logger= LoggerFactory.getLogger(FileDao.class);

    public FileDao() {
        mapper = new ObjectMapper();
    }

    public HashMap<String, ObjectNode> getDocuments(String DB, String colName) throws IOException {
        logger.info("Opening File: " +DB+"/"+colName + ".json");

        ArrayNode collection = (ArrayNode) mapper.readTree(fileService.getCollectionFile(DB, colName));
        return toHashmap(collection);
    }

    private HashMap<String, ObjectNode> toHashmap(ArrayNode collection) {
        HashMap<String, ObjectNode> ret = new HashMap<>();
        for (JsonNode j : collection)
            ret.put(j.get("_id").asText(), (ObjectNode) j);
        return ret;
    }

    public ObjectNode getSchema(String DB) throws IOException {
        logger.info("Opening File: " +DB + "/schema.json");
        try {
            return (ObjectNode) mapper.readTree(fileService.getSchemaFile(DB));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public void addDocument(String DB, String colName, ObjectNode document) throws IOException {
        logger.info("Writing To File: "+DB+"/"+colName + ".json");
        File collectionFile=fileService.getCollectionFile(DB, colName);
        ArrayNode coll = (ArrayNode) mapper.readTree(fileService.getCollectionFile(DB, colName));
        coll.add(document);
        mapper.writeValue(collectionFile, coll);

    }
    public void setCollection(String DB,String colName,ArrayNode collection) throws IOException{
        logger.info("Writing To File: "+DB+"/"+colName + ".json");
        mapper.writeValue(fileService.getCollectionFile(DB,colName),collection);
    }

    public void addCollection(String DB, String colName, JsonNode schema) throws IOException {
        logger.info("Writing To File: "+DB+"/"+colName + ".json");
        mapper.writeValue(fileService.getCollectionFile(DB,colName),mapper.createArrayNode());
        ObjectNode DBschema = getSchema(DB);
        DBschema.set(colName, schema);
        setSchema(DB, DBschema);
    }

    public void deleteDocument(String DB, String colName, String doc_ID) throws IOException {
        File collectionFile=fileService.getCollectionFile(DB, colName);
        ArrayNode coll = (ArrayNode) mapper.readTree(collectionFile);
        for (int i = 0; i < coll.size(); i++)
            if (coll.get(i).get("_id").asText().equals(doc_ID))
                coll.remove(i);
            mapper.writeValue(collectionFile, coll);
    }

    public void setSchema(String DB, ObjectNode schema) throws IOException {
        logger.info("Writing To File: " +DB + "/schema.json");
        mapper.writeValue(fileService.getSchemaFile(DB), schema);
    }

    public void deleteCollection(String DB, String colName) throws IOException {
        ObjectNode DBschema = getSchema(DB);
        DBschema.remove(colName);
        setSchema(DB, DBschema);
        fileService.deleteCollectionFile(DB, colName);
    }
    public void deleteDatabase(String DB) throws IOException {
        fileService.deleteDB(DB);
    }

}
