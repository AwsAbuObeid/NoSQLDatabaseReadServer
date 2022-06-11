package com.nosqldb.readserver.controller;

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
    Logger logger= LoggerFactory.getLogger(ReadController.class);

    @PostMapping
    @RequestMapping(value="/query",consumes="application/json")
    public ResponseEntity query(HttpSession session,@RequestBody ObjectNode query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String DB = (String) session.getAttribute("DBname");
        if (DB ==null)
            return new ResponseEntity(headers,HttpStatus.UNAUTHORIZED);

        ObjectNode ret;
        try { ret = readHandler.executeQuery(DB,query); }
        catch (IOException e){
            e.printStackTrace();
            return new ResponseEntity(headers,HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(ret,headers,HttpStatus.OK);
    }

    @GetMapping
    @RequestMapping(value="/authenticate")
    public ResponseEntity query(HttpSession session)  {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectNode status=new ObjectMapper().createObjectNode();

        if(session.getAttribute("DBname") ==null){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String DB= apIkeyRecord.startSession((String) auth.getPrincipal());
            session.setAttribute("DBname",DB);
            status.put("status", "SESSION STARTED");
            status.put("database", DB);
            logger.info("starting new session on :"+DB);
            return new ResponseEntity(status, headers, HttpStatus.OK);
        }
        status.put("status", "SESSION ALREADY STARTED");
        return new ResponseEntity(status,headers,HttpStatus.OK);
    }
    @Autowired
    HttpSessionConfig httpSessionConfig;

    @GetMapping
    @RequestMapping(value="/load")
    public int yep(){
        return httpSessionConfig.getNumberOfSessions();
    }

}