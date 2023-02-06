package com.mdx.anarchistgame.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
    private String name;
    private String roundScore;
    private String roundsWon;
    private String bid;
    private String type;
}
