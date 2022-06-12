package com.nosqldb.readserver.database.fileservice;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * FileServiceImpl is an implementation of the FileService interface,
 * it handles all the interaction with the files, and holds the directory
 * of the data.
 */

@Component
public class FileServiceImpl implements FileService {
    public static final String DATA_PATH = "Data/";

    private FileServiceImpl() {
    }

    @Override
    public File getCollectionFile(String DB, String colName) {
        new File(DATA_PATH + DB).mkdirs();
        return Paths.get(DATA_PATH + DB + "/" + colName + ".json").toFile();
    }

    @Override
    public File getSchemaFile(String DB) {
        new File(DATA_PATH + DB).mkdirs();
        return Paths.get(DATA_PATH + DB + "/schema.json").toFile();
    }

    @Override
    public void deleteCollectionFile(String DB, String colName) {
        new File(DATA_PATH + DB).mkdirs();
        new File(DATA_PATH + DB + "/" + colName + ".json").delete();
    }

    @Override
    public void deleteDB(String DB) throws IOException {
        FileUtils.deleteDirectory(new File(DATA_PATH + DB));
    }
}
