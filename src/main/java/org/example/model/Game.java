package org.example.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class Game {
    String id;
    List<Player> players;
    List<Move> moves;
    Board board;
    GameState gameState;
    int currentPlayerIndex;
    Player winner;
}
