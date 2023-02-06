package com.mdx.anarchistgame.dto;

public enum Suit {
  SPADES("SPADES"),
  HEARTS("HEARTS"),
  DIAMONDS("DIAMONDS"),
  CLUBS("CLUBS");

  private final String name;

  Suit(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
