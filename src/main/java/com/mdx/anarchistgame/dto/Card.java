package com.mdx.anarchistgame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Card {
    private String suit;
    private String value;
    private String content;
    private String state;
    private String facing;
}
