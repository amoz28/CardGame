package com.mdx.anarchistgame.service;

import com.mdx.anarchistgame.dto.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.mdx.anarchistgame.dto.Color.*;

@Component
public class CardDistribution {

  public void resetGame() {
    deck = new ArrayList<>();
    dealtCards = new ArrayList<>();
    undealtCards = new ArrayList<>();
    playedTricks = new HashMap<>();
    playersBids = new HashMap<>();
    capturedCards = new HashMap<>();
    scores = new HashMap<>();
  }


  private List<Card> deckTemp;
  List<String> deck = new ArrayList<>();
  public List<List<String>> dealtCards = new ArrayList<>();
  public List<String> undealtCards = new ArrayList<>();
  public Map<Integer, String> playedTricks = new HashMap<>();

  public Map<Integer, String> playersBids = new HashMap<>();

  public Map<Integer, Map<Integer, List<Map<String, Object>>>> capturedCards = new HashMap<>();

  public Map<Integer, Integer> scores = new HashMap<>();

  public void run(String... args) throws Exception {

//    GenerateCardDeck(); //Abubakar

    Collections.shuffle(deck); //Abubakar

//    dealCardsToPlayers(); //Amos

    //TODO: Players Annouces their Bid
    playersAnnouncesBid();  // Amos

    for (int k = 0; k < 1; k += 1) {
      playTrick(); // Zeeshan

//      captureCards();
    }
    System.out.println(capturedCards);
//    calculateScores();
  }

  public void captureCards(int round, int numberOfPlayersPlaying, String gameType, Boolean bombThrown) {
    Map<String, List<Map<String, Object>>> newObj = new HashMap<>();
    Map<Integer, List<Map<String, Object>>> roundCapturedCards = new HashMap<>();
    for (Map.Entry<Integer, String> entry : playedTricks.entrySet()) {
      int player = entry.getKey();
      String trick = entry.getValue();
      String rank = trick.substring(0, trick.length() - 1);
      String suit = trick.substring(trick.length() - 1);
      System.out.println(suit);
      if (newObj.containsKey(suit)) {
        List<Map<String, Object>> tricks = newObj.get(suit);
        Map<String, Object> trickData = new HashMap<>();
        trickData.put("player", player);
        trickData.put("trickPlayed", rank);
//        trickData.put("suit", getFullSuit(suit));
        trickData.put("suit", suit);
        tricks.add(trickData);
      } else {
        List<Map<String, Object>> tricks = new ArrayList<>();
        Map<String, Object> trickData = new HashMap<>();
        trickData.put("player", player);
        trickData.put("trickPlayed", rank);
//        trickData.put("suit", getFullSuit(suit));
        trickData.put("suit", suit);
        tricks.add(trickData);
        newObj.put(suit, tricks);
      }
    }

    for (int i = 0; i < numberOfPlayersPlaying; i++) {
      roundCapturedCards.put(i + 1, new ArrayList<>());
    }

    System.out.println(" Cards played " + newObj);
    for (Map.Entry<String, List<Map<String, Object>>> entry : newObj.entrySet()) {
      List<Map<String, Object>> tricks = entry.getValue();

      sortTricks(tricks);

      // If bomb has been thrown, reverse the capturing order
      if (bombThrown) {
        Collections.reverse(tricks);
      }

    }
    List<Map<String, Object>> singleTons = new ArrayList<>();
    newObj.forEach((suit, tricks) -> {
      System.out.println("Suit = " + suit);
      System.out.println("Tricks = " + tricks);
      var highestTrickPlayer = Integer.valueOf(String.valueOf(tricks.get(0).get("player")));
      var highestTrick = String.valueOf(tricks.get(0).get("trickPlayed"));

      if (gameType.equals("Anarchy")) {
        if (highestTrickPlayer == 6) {
          if (tricks.size() == 1) {
            // Undealt card is not captured, so push it back to undealt cards list
            undealtCards.add(tricks.get(0).get("trickPlayed") + suit);
          } else {
            // undealt card captures the suit, so award to next highest player
            highestTrickPlayer = Integer.valueOf(String.valueOf(tricks.get(1).get("player")));
            highestTrick = String.valueOf(tricks.get(1).get("trickPlayed"));

            roundCapturedCards.get(highestTrickPlayer).addAll(tricks);
          }
        } else {
          roundCapturedCards.get(highestTrickPlayer).addAll(tricks);
        }
      } else {
        // Anarchist bomb
        if (tricks.size() == 1) {
          // Card is a singleton(only one in its suit)
          singleTons.add(tricks.get(0));
        } else {
          roundCapturedCards.get(highestTrickPlayer).addAll(tricks);
        }
      }
      System.out.println("Player " + highestTrickPlayer + " captures " + suit + " Suit Played with a " + highestTrick);
    });
    // Deal with singletons
    if (singleTons.size() > 0) {
      if (singleTons.size() == 1) {
        // If there's only one singleton, let it capture itself
        roundCapturedCards.get(singleTons.get(0).get("player")).add(singleTons.get(0));
      } else {
        sortTricks(singleTons);

        // If bomb has been thrown, reverse the capturing order
        if (bombThrown) {
          Collections.reverse(singleTons);
        }

        // If the highest singletons are equal, each singleton captures itself
        if (getRank(String.valueOf(singleTons.get(0).get("trickPlayed"))) == getRank(String.valueOf(singleTons.get(1).get("trickPlayed")))) {
          singleTons.forEach(singleTon -> {
            var singletonPlayer = Integer.valueOf(String.valueOf(singleTon.get("player")));
            roundCapturedCards.get(singletonPlayer).add(singleTon);
          });
        } else {
          var highestSingletonPlayer = Integer.valueOf(String.valueOf(singleTons.get(0).get("player")));
          roundCapturedCards.get(highestSingletonPlayer).addAll(singleTons);
        }
      }
    }

    // Add roundCapturedCards to the map of capturedCards
    capturedCards.put(round, roundCapturedCards);
  }

  private void sortTricks(List<Map<String, Object>> tricks) {
    Collections.sort(tricks, new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> m1, Map<String, Object> m2) {
        int rank = 0;
        String value = "";
        var m1Rank = getRank((String) m1.get("trickPlayed"));
        var m2Rank = getRank((String) m2.get("trickPlayed"));
        return m2Rank.compareTo(m1Rank);
      }
    });
  }

  private List<Map<String, Object>> getPlayerCapturedCards(int playerId) {
    List<Map<String, Object>> playerCapturedCards = new ArrayList<>();
    for (Map.Entry<Integer, Map<Integer, List<Map<String, Object>>>> key: capturedCards.entrySet()) {
      System.out.println(key.getValue().get(playerId));
      playerCapturedCards.addAll(key.getValue().get(playerId));
    }

    return playerCapturedCards;
  }


  public void calculateScores(String gameType) {
    int winner = 1;
    int maxScore = 0;
    int playersCount = 5;

    if (gameType.equals("Anarchist Bomb")) {
      playersCount = 4;
    }
    for (int i = 1; i < playersCount + 1; i += 1) {
      int total = 0;
      var playerCapturedCards = getPlayerCapturedCards(i);
      var playerBid = playersBids.get(i).toUpperCase();
      if (playerBid.equals(Bid.NO_SUIT.name()) || playerBid.equals(Bid.CARDS.name())) {
        total = playerCapturedCards.size();
      } else if (playerBid.equals(Bid.MISERE.name())) {
        int totalCapturedCards = playerCapturedCards.size();
        int difference = 10 - totalCapturedCards;
        if (difference > 0) {
          total = difference;
        }
      } else if (playerBid.equals(Bid.RED.name())) {
        for (var card : playerCapturedCards) {
          if (card.get("suit").equals("H") || card.get("suit").equals("D")) {
            total += 2;
          }
        }
      } else if (playerBid.equals(Bid.BLACK.name())) {
        for (var card : playerCapturedCards) {
          if (card.get("suit").equals("C") || card.get("suit").equals("S")) {
            total += 2;
          }
        }
      } else if (playerBid.equals(Bid.BEST.name())) {
        HashMap<String, Integer> cardCount = new HashMap<>();

        // Count how many of each card have been captured
        for (var card : playerCapturedCards) {
          String suit = (String) card.get("suit");
          if (cardCount.containsKey(suit)) {
            int currentCount = cardCount.get(suit);
            cardCount.put(suit, currentCount + 1);
          } else {
            cardCount.put(suit, 1);
          }
        }

        // Get which has the highest count
        int highestValue = 0;
        for (int value : cardCount.values()) {
          if (value > highestValue) {
            highestValue = value;
          }
        }

        total = 3 * highestValue;

      } else if (playerBid.equals(Bid.OVERS.name())) {
        int totalCapturedCards = playerCapturedCards.size();
        int difference = totalCapturedCards - 13;
        if (difference > 0) {
          total = difference * 5;
        }
      } else if (playerBid.equals(Bid.UNDERS.name())) {
        int totalCapturedCards = playerCapturedCards.size();
        int difference = 13 - totalCapturedCards;
        if (difference > 0) {
          total = difference * 5;
        }
      }
      else {
        int pointsPerCapturedCard = 2;
        if (gameType.equals("Anarchist Bomb")) {
          pointsPerCapturedCard = 4;
        }
        for (var card : playerCapturedCards) {
          if (getFullSuit((String) card.get("suit")).equals(playerBid)) {
            total += pointsPerCapturedCard;
          }
        }
      }
      // Store scores and compute winner
      if (total > maxScore) {
        maxScore = total;
        winner = i;
      }
      scores.put(i, total);
    }

//    Scanner keyboard = new Scanner(System.in);
//    System.out.println("Play again? y/n");
//    String playAgainResponse = keyboard.nextLine();
//
//    while (!playAgainResponse.equals("y") && !playAgainResponse.equals("n")) {
//      System.out.println(RED+"Invalid response. Read the prompt again");
//      System.out.println(RESET+"Play again? y/n");
//      playAgainResponse = keyboard.nextLine();
//    }
//
//    if (playAgainResponse.equals("y")) {
//      try {
//        run();
//      } catch (Exception e) {
//        throw new RuntimeException(e);
//      }}

  }

  public static String getFullSuit(String value) {
    switch (value) {
      case "S":
        return Bid.SPADES.name();
      case "H":
        return Bid.HEARTS.name();
      case "D":
        return Bid.DIAMONDS.name();
      case "C":
        return Bid.CLUBS.name();
    }
    return null;
  }

  public static Integer getRank(String value) {
    int rank;
    switch(value) {
      case "J" :
        rank = 11;
        break;
      case "Q" :
        rank = 12;
        break;
      case "K" :
        rank = 13;
        break;
      case "A" :
        rank = 14;
        break;
      default :
        rank = Integer.valueOf(value);
        break;
    }
    return rank;
  }

  private void playersAnnouncesBid() {
    for (int i = 0; i < 5; i++) {
      Scanner keyboard = new Scanner(System.in);
      int myint = -1;
      do {
        System.out.println("Player "+ (i+1) +" Examine your hand and Place a bid");
        System.out.println("HAND: "+dealtCards.get(i));
        System.out.println("Enter 1 for : SPADES, 2 for :CLUBS, 3 for :DIAMONDS, 4 for :HEARTS, 5 for NO-SUIT, 6 for :MISERE");
        myint = keyboard.nextInt();

        System.out.println(RESET+" Player "+ (i+1) +" Play a trick  from range cards available starting from 0");
        if(myint < 1 || myint > 6)
          System.out.println(RED+"==== Your BID is INVALID ====");
        System.out.println(RESET+"====================================================================");

      }while(myint < 1 || myint > 6);
      
      String bid="";
      switch (myint) {
        case (1) :
          bid = Bid.SPADES.name();
          break;
        case(2) :
          bid = Bid.CLUBS.name();
          break;
        case(3) :
          bid = Bid.DIAMONDS.name();
          break;
        case(4) :
          bid = Bid.HEARTS.name();
          break;
        case(5) :
          bid = Bid.NO_SUIT.name();
          break;
        case(6) :
          bid = Bid.MISERE.name();
          break;
        default :
          bid = "Invalid Bid";

      }
      playersBids.put(i + 1, bid);
      System.out.println(BLUE+"Player "+(i+1) + " Bids "+bid);
      System.out.println(RESET+"====================================================================");
    }
  }

  private void playTrick() {
    for (int i = 0; i < 5; i++) {
      System.out.println(GREEN+"HAND: "+ dealtCards.get(i));
      Scanner keyboard = new Scanner(System.in);
      var indexOfTrick =-1;
      String cardSelected = "";

      do {
        System.out.println(GREEN+" Player "+ (i+1) +" Play a trick from your hand");
        cardSelected = keyboard.nextLine();
        indexOfTrick = dealtCards.get(i).indexOf(cardSelected.toUpperCase());
        if(indexOfTrick<0)
          System.out.println(RED+"==== Your trick is INVALID, please select from your available deck ====");
          System.out.println(GREEN+"HAND: "+ dealtCards.get(i));
      }while(indexOfTrick < 0);
      System.out.println(RESET+"Card Selected "+ dealtCards.get(i).get(indexOfTrick));
//      Add the card selected to the list of played tricks
      playedTricks.put((i+1), dealtCards.get(i).get(indexOfTrick));
//      Remove the played trick
      dealtCards.get(i).remove(indexOfTrick);
      System.out.println("Cards Left: "+ dealtCards.get(i));
    }
    System.out.println("Played tricks: "+playedTricks);
  }

  public List<List<String>> dealCardsToPlayers(int numberOfPlayersPlaying) {
    int remainingCards = deck.size() % numberOfPlayersPlaying;
    for (int i = 0; i < numberOfPlayersPlaying; i++) {
      dealtCards.add(new ArrayList<>());
    }

    for (int i = 0; i < deck.size(); i++) {
      if (i < deck.size() - remainingCards) {
        int personIndex = i % numberOfPlayersPlaying;
        dealtCards.get(personIndex).add(deck.get(i));
      } else {
        undealtCards.add(deck.get(i));
      }
    }

//    dealtCards.get(1).add("Joker");

    // Handle when the joker is the odd card
    if (undealtCards.get(0).equals("Joker")) {
      // Remove last card dealt to dealer and replace it with the joker
      List<String> dealerDealtCards = dealtCards.get(0);
      String lastDealtCard = dealerDealtCards.remove(dealerDealtCards.size() - 1);
      String joker = undealtCards.remove(0);

      dealtCards.get(0).add(joker);
      undealtCards.add(lastDealtCard);
    }

    return dealtCards;
  }

  public List<String> GenerateCardDeck(String gameType) {
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        var rankId = rank.getName() == "10" ? rank.getName() : rank.getName().charAt(0);
        deck.add(String.valueOf(rankId  +""+ suit.getName().charAt(0)));
      }
    }

    if (gameType.equals("Anarchist Bomb")) {
      // Add joker to the deck
      deck.add("Joker");
    }

    return deck;
  }
}
