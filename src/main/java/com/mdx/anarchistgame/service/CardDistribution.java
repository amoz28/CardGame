package com.mdx.anarchistgame.service;

import com.mdx.anarchistgame.dto.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.mdx.anarchistgame.dto.Color.*;

@Component
public class CardDistribution implements CommandLineRunner {


  private List<Card> deckTemp;
  List<String> deck = new ArrayList<>();
  List<List<String>> dealtCards = new ArrayList<>();
  List<String> undealtCards = new ArrayList<>();
  Map<Integer, String> playedTricks = new HashMap<>();

  Map<Integer, String> playersBids = new HashMap<>();

  Map<Integer, List<Map<String, Object>>> capturedCards = new HashMap<>();

  Map<Integer, Integer> scores = new HashMap<>();

  @Override
  public void run(String... args) throws Exception {

    GenerateCardDeck(); //Abubakar

    Collections.shuffle(deck); //Abubakar

    dealCardsToPlayers(); //Amos

    //TODO: Players Annouces their Bid
    playersAnnouncesBid();  // Amos

    for (int k = 0; k < 1; k += 1) {
      playTrick(); // Zeeshan

      Map<String, List<Map<String, Object>>> newObj = new HashMap<>();
      for (Map.Entry<Integer, String> entry : playedTricks.entrySet()) {
        int player = entry.getKey();
        String trick = entry.getValue();
        String[] suit = trick.split("");
        System.out.println(suit[1]);
        if (newObj.containsKey(suit[1])) {
          List<Map<String, Object>> tricks = newObj.get(suit[1]);
          Map<String, Object> trickData = new HashMap<>();
          trickData.put("player", player);
          trickData.put("trickPlayed", suit[0]);
          trickData.put("suit", getFullSuit(suit[1]));
          tricks.add(trickData);
        } else {
          List<Map<String, Object>> tricks = new ArrayList<>();
          Map<String, Object> trickData = new HashMap<>();
          trickData.put("player", player);
          trickData.put("trickPlayed", suit[0]);
          trickData.put("suit", getFullSuit(suit[1]));
          tricks.add(trickData);
          newObj.put(suit[1], tricks);
        }
      }

      for (int i = 0; i < 5; i++) {
        capturedCards.put(i + 1, new ArrayList<>());
      }
      System.out.println(" Cards played " + newObj);
      for (Map.Entry<String, List<Map<String, Object>>> entry : newObj.entrySet()) {
        List<Map<String, Object>> tricks = entry.getValue();

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
      newObj.forEach((suit, tricks) -> {
        System.out.println("Suit = " + suit);
        System.out.println("Tricks = " + tricks);
        var highestTrickPlayer = Integer.valueOf(String.valueOf(tricks.get(0).get("player")));
        var highestTrick = String.valueOf(tricks.get(0).get("trickPlayed"));
        capturedCards.get(highestTrickPlayer).addAll(tricks);
        System.out.println("Player " + highestTrickPlayer + " captures " + suit + " Suit Played with a " + highestTrick);
      });
    }
    System.out.println(capturedCards);
    calculateScores();
  }

  private void calculateScores() {
    int winner = 1;
    int maxScore = 0;
    for (int i = 1; i < 6; i += 1) {
      int total = 0;
      var playerCapturedCards = capturedCards.get(i);
      var playerBid = playersBids.get(i);
      if (playerBid == Bid.NO_SUIT.name()) {
        total = playerCapturedCards.size();
        if (total > maxScore) {
          maxScore = total;
          winner = i;
        }
        scores.put(i, total);
      } else if (playerBid == Bid.MISERE.name()) {
        for (var card : playerCapturedCards) {
          if (getRank((String) card.get("trickPlayed")) < 10) {
            total += 1;
          }
        }
        if (total > maxScore) {
          maxScore = total;
          winner = i;
        }
        scores.put(i, total);
      } else {
        for (var card : playerCapturedCards) {
          if (card.get("suit").equals(playerBid)) {
            total += 2;
          }
        }
        if (total > maxScore) {
          maxScore = total;
          winner = i;
        }
        scores.put(i, total);
      }
    }
    System.out.println(scores);
    if (maxScore == 0) {
      System.out.println(BLUE+"This round is not won by any player");
    } else{
      System.out.println(BLUE+"This round is won by player " + winner + " with " + maxScore + " points");
    }
    System.out.println(RESET+"------------------------------------------------------------------------");

    Scanner keyboard = new Scanner(System.in);
    System.out.println("Play again? y/n");
    String playAgainResponse = keyboard.nextLine();

    while (!playAgainResponse.equals("y") && !playAgainResponse.equals("n")) {
      System.out.println(RED+"Invalid response. Read the prompt again");
      System.out.println(RESET+"Play again? y/n");
      playAgainResponse = keyboard.nextLine();
    }

    if (playAgainResponse.equals("y")) {
      try {
        run();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

  }

  private static String getFullSuit(String value) {
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

  private static Integer getRank(String value) {
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

  private void dealCardsToPlayers() {
    int remainingCards = deck.size() % 5;
    for (int i = 0; i < 5; i++) {
      dealtCards.add(new ArrayList<>());
    }

    for (int i = 0; i < deck.size(); i++) {
      if (i < deck.size() - remainingCards) {
        int personIndex = i % 5;
        dealtCards.get(personIndex).add(deck.get(i));
      } else {
        undealtCards.add(deck.get(i));
      }
    }
  }

  private void GenerateCardDeck() {
    for (Suit suit : Suit.values()) {
      for (Rank rank : Rank.values()) {
        deck.add(String.valueOf(rank.getName().charAt(0)  +""+ suit.getName().charAt(0)));
      }
    }
  }
}
