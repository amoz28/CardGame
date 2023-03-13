package com.mdx.anarchistgame.controller;

import com.mdx.anarchistgame.service.CardDistribution;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class MessageController {
    Map<String, Integer> registeredPlayers = new HashMap<>();
    int trickCount = 1;
    int playCount = 0;

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
        registeredPlayers.put(name, registeredPlayers.size() + 1);
        simpMessagingTemplate.convertAndSend("/socket-publisher/" + name, response);

        if (registeredPlayers.size() == 2) {
            // Generate deck
            var deck = cardDistribution.GenerateCardDeck(); //Abubakar

            Collections.shuffle(deck); //Abubakar

            var dealtCards = cardDistribution.dealCardsToPlayers();
            var registeredPlayersObject = new HashMap<>();
            // deal cards to players
            for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                var player = entry.getKey();
                var playerId = entry.getValue();
                var playerCards = dealtCards.get(playerId);
                var payload = new HashMap<>();
                payload.put("type", "player-hand");
                payload.put("success", true);
                payload.put("hand", playerCards);
                payload.put("undealtCard", cardDistribution.undealtCards.get(cardDistribution.undealtCards.size() - 1));
                payload.put("registeredPlayers", registeredPlayers.keySet());
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
            var player = message.get("player");
            int playerId = registeredPlayers.get(player);
            var bid = message.get("bid");
            cardDistribution.playersBids.put(playerId, (String) bid);

            // Acknowledge bid placement
            var payload = new HashMap<>();
            payload.put("type", "bid-placed");
            payload.put("success", true);
            simpMessagingTemplate.convertAndSend("/socket-publisher/" + player, payload);

            if (cardDistribution.playersBids.size() == 2) {
                payload = new HashMap<>();
                payload.put("type", "all-bids-placed");
                simpMessagingTemplate.convertAndSend("/socket-publisher/play", payload);
            }
        } else if (type.equals("play-trick")) {
            playCount++;
            var player = message.get("player");
            int playerId = registeredPlayers.get(player);
            var trick = message.get("trick");

            var playedTrickNotification = new HashMap<>();
            playedTrickNotification.put("type", "played-trick");
            playedTrickNotification.put("player", player);

            // Send notification that a player has played a trick
            simpMessagingTemplate.convertAndSend("/socket-publisher/play", playedTrickNotification);

            // Add trick to list of played tricks
            cardDistribution.playedTricks.put(playerId, (String) trick);

            int indexOfTrick = cardDistribution.dealtCards.get(playerId).indexOf(trick);

            // Remove the played trick from player's hand
            cardDistribution.dealtCards.get(playerId).remove(indexOfTrick);

            if (playCount == 2) {
                var playedTricksResponse = new HashMap<>();
                var playedTricksObject = new HashMap<>();
                playedTricksResponse.put("type", "reveal-cards");
                playedTricksResponse.put("success", true);
                for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                    var regPlayer = entry.getKey();
                    var regPlayerId = entry.getValue();
                    playedTricksObject.put(regPlayer, cardDistribution.playedTricks.get(regPlayerId));
                }
                playedTricksResponse.put("playedTricks", playedTricksObject);
                simpMessagingTemplate.convertAndSend("/socket-publisher/play", playedTricksResponse);
                
                // Add an undealt in the playedTricks list so it can be captured as well.
                int undealtCardsSize = cardDistribution.undealtCards.size();
                if (undealtCardsSize > 0) {
                    cardDistribution.playedTricks.put(6, cardDistribution.undealtCards.get(undealtCardsSize - 1));
                    
                    // remove card from undealt cards list
                    cardDistribution.undealtCards.remove(undealtCardsSize - 1);
                }
                cardDistribution.captureCards();

                // Change key of capturedCards from id to name
                var refinedCapturedCards = new HashMap<>();
                for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                    var regPlayer = entry.getKey();
                    var regPlayerId = entry.getValue();
                    var playerCapturedCards = cardDistribution.capturedCards.get(regPlayerId);
                    refinedCapturedCards.put(regPlayer, playerCapturedCards);
                }

                // Send result of capturing to the players
                var captureCardsResponse = new HashMap<>();
                captureCardsResponse.put("type", "capture-cards");
                captureCardsResponse.put("success", true);
                captureCardsResponse.put("capturedCards", refinedCapturedCards);
                simpMessagingTemplate.convertAndSend("/socket-publisher/play", captureCardsResponse);

                trickCount += 1;
                playCount = 0;
                cardDistribution.playedTricks = new HashMap<>();

                if (trickCount <= 10) {
                    // Play next round/trick
                    var nextRoundPayload = new HashMap<>();
                    nextRoundPayload.put("type", "next-round");
                    nextRoundPayload.put("trickCount", trickCount);
                    undealtCardsSize = cardDistribution.undealtCards.size();
                    if (undealtCardsSize > 0) {
                        nextRoundPayload.put("undealtCard", cardDistribution.undealtCards.get(undealtCardsSize - 1));
                    }

                    simpMessagingTemplate.convertAndSend("/socket-publisher/play", nextRoundPayload);
                } else {
                    //Finished playing tricks, calculate scores
                    cardDistribution.calculateScores();
                    var endOfRoundPayload = new HashMap<>();
                    endOfRoundPayload.put("type", "end-of-round");
                    // Change key of capturedCards from id to name
                    refinedCapturedCards = new HashMap<>();
                    var scores = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                        var regPlayer = entry.getKey();
                        var regPlayerId = entry.getValue();
                        var playerCapturedCards = cardDistribution.capturedCards.get(regPlayerId);
                        refinedCapturedCards.put(regPlayer, playerCapturedCards);
                        scores.put(regPlayer, cardDistribution.scores.get(regPlayerId));
                    }

                    endOfRoundPayload.put("capturedCards", refinedCapturedCards);
                    endOfRoundPayload.put("scores", scores);
                }




//                cardDistribution.calculateScores();

//                simpMessagingTemplate.convertAndSend("/socket-publisher/play", );
            }
        }
//        simpMessagingTemplate.convertAndSend("/socket-publisher/", "Reddddddd");
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println(event);
    }
}
