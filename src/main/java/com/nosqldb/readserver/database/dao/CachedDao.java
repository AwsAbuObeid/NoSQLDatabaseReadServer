package com.nosqldb.readserver.database.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;


/**
 *  CachedDao is an implementation of the DocumentDao interface, it represents
 *  the Proxy part of the proxy pattern, it uses the spring cache annotations
 *  which in turn use Ehcache to cache the output of the functions in memory,
 *  the annotations are set up so that writes evict the cache.
 */
@Primary
@Repository
public class CachedDao implements DocumentDao {
    @Autowired
    private FileDao dao;

    @Override
    @Cacheable(value = "collections", key = "{#DB, #colName}")
    public HashMap<String, ObjectNode> getDocuments(String DB, String colName) throws IOException {
        return dao.getDocuments( DB, colName);
    }

    @Cacheable(value = "DBInfo")
    public ObjectNode getSchema(String DB) throws IOException {
        return dao.getSchema(DB);
    }

    @CacheEvict(value = "collections", key = "{#DB, #colName}")
    public void addDocument(String DB, String colName, ObjectNode document) throws IOException {
        dao.addDocument(DB,colName,document);
    }

    @CacheEvict(value = "collections", key = "{#DB, #colName}")
    public void deleteDocument(String DB, String colName, String doc_ID) throws IOException {
        dao.deleteDocument( DB, colName, doc_ID);
    }

    @CacheEvict(value = "DBInfo",allEntries = true)
    public void setSchema(String DB, ObjectNode schema) throws IOException {
        dao.setSchema(DB, schema);
    }

    @Caching(evict = {
            @CacheEvict(value = "DBInfo",allEntries = true),
            @CacheEvict(value = "collections", key = "{#DB, #colName}")
    })
    public void deleteCollection(String DB, String colName) throws IOException {
        dao.deleteCollection( DB, colName);
    }

    @Caching(evict = {
            @CacheEvict(value = "DBInfo",allEntries = true),
            @CacheEvict(value = "collections", allEntries = true)
    })
    public void deleteDatabase(String DB) throws IOException {
        dao.deleteDatabase(DB);
    }

    @Caching(evict = {
            @CacheEvict(value = "DBInfo",allEntries = true),
            @CacheEvict(value = "collections", key = "{#DB, #colName}")
    })
    public void setCollection(String DB, String colName, ArrayNode collection) throws IOException {
        dao.setCollection(DB, colName, collection);
    }

    @CacheEvict(value = "DBInfo",allEntries = true)
    public void addCollection(String db, String collection, JsonNode schema) throws IOException {
        dao.addCollection(db, collection, schema);
    }
}
