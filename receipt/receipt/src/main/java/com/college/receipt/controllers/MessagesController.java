package com.college.receipt.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class MessagesController {

    private static final Logger logger = LoggerFactory.getLogger(MessagesController.class);

    @GetMapping("/message")
    public ResponseEntity<List<String>> messges(){
        logger.info("Запрос на показ сообщений");
        return ResponseEntity.ok(Arrays.asList("biba", "boba"));
    }
}