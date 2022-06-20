package com.nosqldb.readserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nosqldb.readserver.config.HttpSessionConfig;
import com.nosqldb.readserver.requesthandlers.ReadHandler;
import com.nosqldb.readserver.security.APIkeyRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ReadController Class is responsible for handling all the read requests,
 * the user must first authenticate and start the session, after that
 * he can use the server to read from his designated database.
 */
@RestController
public class ReadController {
    @Autowired
    private APIkeyRecord apIkeyRecord;
    @Autowired
    private ReadHandler readHandler;
    Logger logger = LoggerFactory.getLogger(ReadController.class);

    @GetMapping
    @RequestMapping(value = "/query/{collection}")
    public ResponseEntity getAll(HttpSession session, @PathVariable String collection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String DB = (String) session.getAttribute("DBname");
        if (DB == null)
            return new ResponseEntity(headers, HttpStatus.FORBIDDEN);

        JsonNode ret;
        try {
            ret = readHandler.getAll(DB, collection);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

    @PostMapping
    @RequestMapping(value = "/indexQuery/{collection}", consumes = "application/json")
    public ResponseEntity indexQuery(HttpSession session, @RequestBody ObjectNode index, @PathVariable String collection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String DB = (String) session.getAttribute("DBname");
        if (DB == null)
            return new ResponseEntity(headers, HttpStatus.FORBIDDEN);

        JsonNode ret;
        try {
            ret = readHandler.indexQuery(DB, collection, index);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

    @GetMapping
    @RequestMapping(value = "/idQuery/{collection}/{id}")
    public ResponseEntity idQuery(HttpSession session, @PathVariable String id, @PathVariable String collection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String DB = (String) session.getAttribute("DBname");
        if (DB == null)
            return new ResponseEntity(headers, HttpStatus.FORBIDDEN);

        JsonNode ret;
        try {
            ret = readHandler.idQuery(DB, collection, id);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

    @PostMapping
    @RequestMapping(value = "/searchQuery/{collection}", consumes = "application/json")
    public ResponseEntity searchQuery(HttpSession session, @RequestBody ObjectNode index, @PathVariable String collection) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String DB = (String) session.getAttribute("DBname");
        if (DB == null)
            return new ResponseEntity(headers, HttpStatus.FORBIDDEN);

        JsonNode ret;
        try {
            ret = readHandler.searchQuery(DB, collection, index);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity(headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(ret, headers, HttpStatus.OK);
    }

    @GetMapping
    @RequestMapping(value = "/authenticate")
    public ResponseEntity query(HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectNode status = new ObjectMapper().createObjectNode();

        if (session.getAttribute("DBname") == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String DB = apIkeyRecord.startSession((String) auth.getPrincipal());
            session.setAttribute("DBname", DB);
            status.put("status", "SESSION STARTED");
            status.put("database", DB);
            logger.info("starting new session on :" + DB);
            return new ResponseEntity(status, headers, HttpStatus.OK);
        }
        status.put("status", "SESSION ALREADY STARTED");
        return new ResponseEntity(status, headers, HttpStatus.OK);
    }

    @Autowired
    HttpSessionConfig httpSessionConfig;

    @GetMapping
    @RequestMapping(value = "/load")
    public int getLoad() {
        return httpSessionConfig.getNumberOfSessions();
    }

}