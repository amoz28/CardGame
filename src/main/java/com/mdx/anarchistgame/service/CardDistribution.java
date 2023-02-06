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

  private List<Card> deckTemp;
  List<String> deck = new ArrayList<>();
  List<List<String>> dealtCards = new ArrayList<>();
  List<String> undealtCards = new ArrayList<>();
  Map<Integer, String> playedTricks = new HashMap<>();

  Map<Integer, String> playerBid = new HashMap<>();

  @Override
  public void run(String... args) throws Exception {

    GenerateCardDeck();

    Collections.shuffle(deck);

    dealCardsToPlayers();

    //TODO: Players Annouces their Bid
    playersAnnouncesBid();
    playTrick();

    Map<String, List<Map<String, Object>>> newObj = new HashMap<>();
    for (Map.Entry<Integer, String> entry : playedTricks.entrySet()) {
      int player = entry.getKey();
      String trick = entry.getValue();
      String[] suit = trick.split(" of ");
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
          Integer name1 = Integer.valueOf(String.valueOf(m1.get("trickPlayed")));
          Integer name2 = Integer.valueOf(String.valueOf(m2.get("trickPlayed")));
          return name2.compareTo(name1);
        }
      });

//      tricks.sort((a, b) -> (int) b.get("trickPlayed") - (int) a.get("trickPlayed"));
    }

    System.out.println(newObj);


//    Map<Suit, Map<String,String>> suitsPlayed = new HashMap<>();
//    playedTricks.forEach((player, playedTrick) -> {
//      System.out.println("Player: " + player + " Played trick: " + playedTrick);
//      var tricksArray = playedTrick.split(" of ");
//      Map<String, String> players = new HashMap<>();
//      players.put(String.valueOf(player), tricksArray[0]);
////      System.out.println("Test "+tricksArray[0] +" == "+tricksArray[1]);
//      suitsPlayed.put(Suit.valueOf(tricksArray[1]), players);
//    });
////    for (String playedTrick : playedTricks.values()) {
////      Map<String, String> player = new HashMap<>();
////      var tricksArray = playedTrick.split(" of ");
////      System.out.println("Test "+tricksArray[0] +" == "+tricksArray[1]);
//////      suitsPlayed.put(Suit.valueOf(tricksArray[1]), tricksArray[0]);
//////      suitsPlayed.pu
////    }
//    System.out.println("Players "+suitsPlayed);
  }

  private void playersAnnouncesBid() {
    for (int i = 0; i < 5; i++) {
      Scanner keyboard = new Scanner(System.in);
      System.out.println("Player "+ (i+1) +" Examin your card and Place a bid");
      System.out.println("Enter 1 for : SPADES, 2 for :CLUBS, 3 for :DIAMONDS, 4 for :HEARTS, 5 for NO-SUIT, 6 for :MISERE");
      int myint = keyboard.nextInt();
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
    }
  }

  private void playTrick() {
    for (int i = 0; i < 5; i++) {
      System.out.println("Val "+ dealtCards.get(i));
      Scanner keyboard = new Scanner(System.in);
      System.out.println("Player "+ (i+1) +" Play a trick  from range cards available starting from 0");
      int myint = keyboard.nextInt();
      System.out.println("Card Selected "+ dealtCards.get(i).get(myint));
//      Add the card selected to the list of played tricks
      playedTricks.put((i+1), dealtCards.get(i).get(myint));
//      Remove the played trick
      dealtCards.get(i).remove(myint);
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
        deck.add(rank.getName() + " of " + suit.getName());
      }
    }
  }
}
