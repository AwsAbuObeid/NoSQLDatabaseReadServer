package com.nosqldb.readserver.database.fileservice;

import java.io.File;
import java.io.IOException;

public interface FileService {
    File getCollectionFile(String DB, String colName);

    File getSchemaFile(String DB);

    void deleteDB(String DB) throws IOException;

    File getIndexFile(String db, String colName, String indexName);
}