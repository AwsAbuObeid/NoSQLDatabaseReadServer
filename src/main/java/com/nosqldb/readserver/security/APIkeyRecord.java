package com.nosqldb.readserver.security;

import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * The APIkeyRecord class keeps a log of all the available API keys and the associated
 * database that the server received from the main controller, it also deletes the
 * Key after a single use.
 */
@Service
public class APIkeyRecord {
    private static final String Controller_API_key="Controller_API_key";
    private static final HashMap<String,String> securityKeys=fillKeys();
    private static HashMap<String, String> fillKeys() {
        HashMap<String, String> x = new HashMap<>();
        x.put("123456789","StudentDB");
        x.put("234567891","StudentDB");
        x.put("345678912","StudentDB");
        return x;
    }

    public void addKey(String securityKey,String DB){
        securityKeys.put(securityKey, DB);
    }

    public boolean isControllerKey(String securityKey){
        return Controller_API_key.equals(securityKey);
    }

    public boolean isValidKey(String securityKey) {
        return securityKeys.containsKey(securityKey)||Controller_API_key.equals(securityKey);
    }

    public String startSession(String securityKey) {

        String DB=securityKeys.get(securityKey);
        securityKeys.remove(securityKey);
        return DB;
    }
}
