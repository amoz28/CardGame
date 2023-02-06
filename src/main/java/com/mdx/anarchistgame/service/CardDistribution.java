package com.mdx.anarchistgame.service;

import com.mdx.anarchistgame.dto.Bid;
import com.mdx.anarchistgame.dto.Card;
import com.mdx.anarchistgame.dto.Rank;
import com.mdx.anarchistgame.dto.Suit;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CardDistribution implements CommandLineRunner {

  public static final String RESET = "\u001B[0m";
  public static final String BLACK = "\u001B[30m";
  public static final String RED = "\u001B[31m";
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String BLUE = "\u001B[34m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";
  public static final String WHITE = "\u001B[37m";
  private List<Card> deckTemp;
  List<String> deck = new ArrayList<>();
  List<List<String>> dealtCards = new ArrayList<>();
  List<String> undealtCards = new ArrayList<>();
  Map<Integer, String> playedTricks = new HashMap<>();

  Map<Integer, String> playerBid = new HashMap<>();

  @Override
  public void run(String... args) throws Exception {

    GenerateCardDeck(); //Abubakar

    Collections.shuffle(deck); //Abubakar

    dealCardsToPlayers(); //Amos

    //TODO: Players Annouces their Bid
    playersAnnouncesBid();  // Amos
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
        tricks.add(trickData);
      } else {
        List<Map<String, Object>> tricks = new ArrayList<>();
        Map<String, Object> trickData = new HashMap<>();
        trickData.put("player", player);
        trickData.put("trickPlayed", suit[0]);
        tricks.add(trickData);
        newObj.put(suit[1], tricks);
      }
    }

    Map<Integer, List<Map<String, Object>>> capturedCards = new HashMap<>();

    for (int i = 0; i < 5; i++) {
      capturedCards.put(i + 1, new ArrayList<>());
    }
    System.out.println(" Cards played "+newObj);
    for (Map.Entry<String, List<Map<String, Object>>> entry : newObj.entrySet()) {
      List<Map<String, Object>> tricks = entry.getValue();
//      List<Map<String, Object>> tricks = entry.getValue();

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
      System.out.println("Suit = "+suit);
      System.out.println("Tricks = "+tricks);
      var highestTrickPlayer = Integer.valueOf(String.valueOf(tricks.get(0).get("player")));
      var highestTrick = String.valueOf(tricks.get(0).get("trickPlayed"));
      System.out.println("Player "+highestTrickPlayer+  " captures "+suit+" Suit Played with a "+ highestTrick);
    });
    System.out.println(newObj);
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
        System.out.println("Player "+ (i+1) +" Examin your hand and Place a bid");
        System.out.println("HAND: "+dealtCards.get(i));
        System.out.println("Enter 1 for : SPADES, 2 for :CLUBS, 3 for :DIAMONDS, 4 for :HEARTS, 5 for NO-SUIT, 6 for :MISERE");
        myint = keyboard.nextInt();

        System.out.println(RESET+" Player "+ (i+1) +" Play a trick  from range cards available starting from 0");
//        cardSelected = keyboard.nextLine();
//        indexOfTrick = dealtCards.get(i).indexOf(cardSelected);
        if(myint < 1 || myint >5 )
          System.out.println(RED+"==== Your BID is INVALID ====");
        System.out.println(RESET+"====================================================================");

      }while(myint < 1 || myint >5);
      
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
      playerBid.put(i + 1, bid);
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
        System.out.println(GREEN+" Player "+ (i+1) +" Play a trick  from range cards available starting from 0");
        cardSelected = keyboard.nextLine();
        indexOfTrick = dealtCards.get(i).indexOf(cardSelected);
        if(indexOfTrick<0)
          System.out.println(RED+"==== Your trick is INVALID, please select from your available deck ====");
      }while(indexOfTrick < 0);
      System.out.println(RESET+"Card Selected "+ dealtCards.get(i).indexOf(cardSelected));
//      Add the card selected to the list of played tricks
      playedTricks.put((i+1), dealtCards.get(i).get(indexOfTrick));
//      Remove the played trick
      dealtCards.get(i).remove(indexOfTrick);
      System.out.println("Card Selected "+ dealtCards.get(i));
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
