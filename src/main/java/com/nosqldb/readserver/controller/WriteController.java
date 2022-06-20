package com.nosqldb.readserver.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.requesthandlers.WriteHandler;
import com.nosqldb.readserver.security.APIkeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * WriteController Class is responsible for receiving all the write requests
 * from the Main Database controller, it receives a Json object which contains
 * all the info to execute a write to the database.
 */
@RestController
public class WriteController {
    @Autowired
    private APIkeyRecord APIkeyRecord;
    @Autowired
    private WriteHandler writeHandler;
    Logger logger = LoggerFactory.getLogger(WriteController.class);

    @PostMapping
    @RequestMapping("/write")
    public ResponseEntity write(@RequestBody ObjectNode Body) {
        String key = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!APIkeyRecord.isControllerKey(key))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);

        try {
            writeHandler.executeWrite(Body);
            logger.info("Executed write command: " + Body.get("op").asText());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity("OK", HttpStatus.OK);
    }

    @PostMapping
    @RequestMapping("/addAPIKey")
    public ResponseEntity addAPIKey(@RequestBody ObjectNode Body) {
        String key = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!APIkeyRecord.isControllerKey(key))
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        try {
            APIkeyRecord.addKey(Body.get("key").asText(), Body.get("DB").asText());
            logger.info("Added new API key");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity("OK", HttpStatus.OK);
    }

}