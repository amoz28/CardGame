package com.mdx.anarchistgame.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/register")
    public void register(String name){
        simpMessagingTemplate.convertAndSend("/socket-publisher/" + name, "Registered!!!");
    }
}
