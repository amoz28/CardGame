package com.mdx.anarchistgame.controller;

import com.mdx.anarchistgame.service.CardDistribution;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MessageController {
    Map<Integer, String> registeredPlayers = new HashMap<>();

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CardDistribution cardDistribution;
    @MessageMapping("/register")
    public void register(String name){
//        Register user and send a notification that they've been registered
        System.out.println(name);
        HashMap response = new HashMap<>();
        response.put("type", "registration");
        response.put("success", true);
        response.put("user", name);
        registeredPlayers.put(registeredPlayers.size(), name);
        simpMessagingTemplate.convertAndSend("/socket-publisher/" + name, response);

        if (registeredPlayers.size() == 2) {
            // Generate deck
            var deck = cardDistribution.GenerateCardDeck(); //Abubakar

            Collections.shuffle(deck); //Abubakar

            var dealtCards = cardDistribution.dealCardsToPlayers();

            // deal cards to players
            for (int i = 0; i < registeredPlayers.size(); i++) {
                var player = registeredPlayers.get(i);
                var playerCards = dealtCards.get(i);
                var payload = new HashMap<>();
                payload.put("type", "player-hand");
                payload.put("success", true);
                payload.put("hand", playerCards);
                simpMessagingTemplate.convertAndSend("/socket-publisher/" + player, payload);
            }
            // Amos
            System.out.println(dealtCards);
        }
    }

    @MessageMapping("/play")
    public void playGame(HashMap<Object, Object> message){
        System.out.println(message);
        Object type = message.get("type");// Code to be executed if expression doesn't match any of the values
        if (type.equals("place-bid")) {// Do something
            System.out.println(message);
        } else if (type.equals("play-trick")) {
        }
//        simpMessagingTemplate.convertAndSend("/socket-publisher/", "Reddddddd");
    }
}
