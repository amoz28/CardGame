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
    String gameType = "";
    int dealerId = 1;
    int dummyId = 0;
    int confirmPlayAgain = 0;

    int numberOfPlayersPlaying = 5;
    int numberOfRounds = 10;
    Boolean withDummy = false;
    Boolean isBombThrown = false;
    String anarchist = "";

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final CardDistribution cardDistribution;
    @MessageMapping("/register")
    public void register(HashMap<String, String> message){
        if (message.get("type").equals("set-number-of-players")) {
            int numberOfPlayers = Integer.parseInt(message.get("numberOfPlayersPlaying"));
            if (numberOfPlayers == 3) {
                withDummy = true;
                dummyId = registeredPlayers.size() + 1;
                String dummyBid = "dummyBid";
                registeredPlayers.put("Dummy", dummyId);
                cardDistribution.playersBids.put(dummyId, dummyBid);
            }
        } else {
            Boolean isDealer = false;
            // First player that registers is set as the dealer
            if (registeredPlayers.size() == 0) {
                isDealer = true;
                gameType = message.get("gameType");
            }
            String name = message.get("name");

            if (isDealer && gameType.equals("Anarchist Bomb")) {
                numberOfPlayersPlaying = 4;
                numberOfRounds = 13;
            }

//        Register user and send a notification that they've been registered
            HashMap response = new HashMap<>();
            response.put("type", "registration");
            response.put("success", true);
            response.put("user", name);
            response.put("isDealer", isDealer);
            registeredPlayers.put(name, registeredPlayers.size() + 1);
            simpMessagingTemplate.convertAndSend("/socket-publisher/" + name, response);

            if (registeredPlayers.size() == numberOfPlayersPlaying) {
                GenerateDeckAndDealCardsToPlayers();
            }
        }
    }

    private void GenerateDeckAndDealCardsToPlayers() {
        // Generate deck
        var deck = cardDistribution.GenerateCardDeck(gameType); //Abubakar

        Collections.shuffle(deck); //Abubakar
        var dealtCards = cardDistribution.dealCardsToPlayers(numberOfPlayersPlaying);
        var registeredPlayersObject = new HashMap<>();
        // deal cards to players
        for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
            var player = entry.getKey();
            var playerId = entry.getValue();
            var playerCards = dealtCards.get(playerId - 1);
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

    @MessageMapping("/play")
    public void playGame(HashMap<Object, Object> message){
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

            if (cardDistribution.playersBids.size() == numberOfPlayersPlaying) {
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
//            playedTrickNotification.put("isBombThrown", isBombThrown);

            // If dealer played the trick, play for the dummy as well
            if (playerId == dealerId && withDummy) {
                playCount++;
                int indexOfDummyTrick = cardDistribution.dealtCards.get(dummyId - 1).size() - 1;
                String dummyTrick = cardDistribution.dealtCards.get(dummyId - 1).get(indexOfDummyTrick);

                // Handle if the dummy's trick is a joker
                if (dummyTrick.equals("Joker")) {
                    isBombThrown = true;
                    dummyTrick = cardDistribution.undealtCards.remove(0);
//                    playedTrickNotification.put("isBombThrown", isBombThrown);
                    anarchist = "Dummy";
//                    playedTrickNotification.put("arnachist", anarchist);
                }
                cardDistribution.playedTricks.put(dummyId, dummyTrick);

                playedTrickNotification.put("dummy", "Dummy");

                // Remove the played trick from player's hand
                cardDistribution.dealtCards.get(dummyId - 1).remove(indexOfDummyTrick);
            }

            if (trick.equals("Joker") && !anarchist.equals("Dummy")) {
                isBombThrown = true;
                anarchist = (String) player;
//                playedTrickNotification.put("arnachist", player);
                // Remove the Joker from player's hand
                cardDistribution.dealtCards.get(playerId - 1).remove("Joker");
                // remove card from undealt cards list and set it as played trick
                trick = cardDistribution.undealtCards.remove(0);
            } else {
                int indexOfTrick = cardDistribution.dealtCards.get(playerId - 1).indexOf(trick);

                // Remove the played trick from player's hand
                cardDistribution.dealtCards.get(playerId - 1).remove(indexOfTrick);
            }

            // Add trick to list of played tricks
            cardDistribution.playedTricks.put(playerId, (String) trick);

            // Send notification that a player has played a trick
            simpMessagingTemplate.convertAndSend("/socket-publisher/play", playedTrickNotification);

            if (playCount == numberOfPlayersPlaying) {
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
                playedTricksResponse.put("isBombThrown", isBombThrown);
                playedTricksResponse.put("arnachist", anarchist);
                simpMessagingTemplate.convertAndSend("/socket-publisher/play", playedTricksResponse);
                
                // Add an undealt in the playedTricks list so it can be captured as well.
                int undealtCardsSize = cardDistribution.undealtCards.size();
                if (gameType.equals("Anarchy") && undealtCardsSize > 0) {
                    cardDistribution.playedTricks.put(6, cardDistribution.undealtCards.get(undealtCardsSize - 1));
                    
                    // remove card from undealt cards list
                    cardDistribution.undealtCards.remove(undealtCardsSize - 1);
                }
                cardDistribution.captureCards(trickCount, numberOfPlayersPlaying, gameType, isBombThrown);

                // Reset isBombThrown state
                isBombThrown = false;
                anarchist = "";

                // Change key of capturedCards from id to name
                var refinedCapturedCards = new HashMap<>();
                for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                    var regPlayer = entry.getKey();
                    var regPlayerId = entry.getValue();
                    var playerCapturedCards = cardDistribution.capturedCards.get(trickCount).get(regPlayerId);
                    refinedCapturedCards.put("round", trickCount);
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

                if (trickCount <= numberOfRounds) {
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
                    // Finished playing tricks, calculate scores
                    cardDistribution.calculateScores(gameType);
                    var endOfRoundPayload = new HashMap<>();
                    endOfRoundPayload.put("type", "end-of-round");
                    // Change key of capturedCards from id to name
                    refinedCapturedCards = new HashMap<>();
                    var scores = new HashMap<>();
                    var bids = new HashMap<>();
                    for (Map.Entry<String, Integer> entry : registeredPlayers.entrySet()) {
                        var regPlayer = entry.getKey();
                        var regPlayerId = entry.getValue();
                        var playerCapturedCards = new ArrayList<>();
                        for (Map.Entry<Integer, Map<Integer, List<Map<String, Object>>>> key: cardDistribution.capturedCards.entrySet()) {
                            playerCapturedCards.addAll(key.getValue().get(regPlayerId));
                        }

                        refinedCapturedCards.put(regPlayer, playerCapturedCards);

                        scores.put(regPlayer, cardDistribution.scores.get(regPlayerId));
                        bids.put(regPlayer, cardDistribution.playersBids.get(regPlayerId));
                    }

                    endOfRoundPayload.put("capturedCards", refinedCapturedCards);
                    endOfRoundPayload.put("scores", scores);
                    endOfRoundPayload.put("bids", bids);

                    simpMessagingTemplate.convertAndSend("/socket-publisher/play", endOfRoundPayload);

                    // Reset Game
                    trickCount = 1;
                    confirmPlayAgain = 0;
                    cardDistribution.resetGame();
                }
            }
        } else if (message.get("type").equals("play-again")) {
            confirmPlayAgain++;
            if (withDummy) {
                confirmPlayAgain++;
                cardDistribution.playersBids.put(dummyId, "dummyBid");
            }
            var payload = new HashMap<>();
            payload.put("type", "play-again-request");
            simpMessagingTemplate.convertAndSend("/socket-publisher/play", payload);
        } else if (message.get("type").equals("confirm-play-again")) {
            Boolean response = (Boolean) message.get("response");
            if (response) {
                confirmPlayAgain++;
            } else {
                // Remove player from registered players
                String player = (String) message.get("player");
                registeredPlayers.remove(player);
            }

            if (confirmPlayAgain == numberOfPlayersPlaying) {
                GenerateDeckAndDealCardsToPlayers();
            }
        } else if (message.get("type").equals("end-game")) {
            // Remove player from registered players
            String player = (String) message.get("player");
            registeredPlayers.remove(player);
        }
    }

    @EventListener
    private void handleSession(SessionDisconnectEvent event) {
        System.out.println(event);
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println(event);
    }
}
