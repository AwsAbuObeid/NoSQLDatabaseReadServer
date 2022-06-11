package com.nosqldb.readserver.requesthandlers.queryutils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public interface QueryValidator {

    String isValidIndexQuery(String DB, String collection, JsonNode query) throws IOException;

    boolean isValidFormat(JsonNode query) throws IOException;

    boolean resourceExists(String DB, String collection) throws IOException;

}