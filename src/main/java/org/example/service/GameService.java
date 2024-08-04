package org.example.service;

import org.example.model.*;

import java.io.IOException;
import java.util.List;

public interface GameService {
    Game initialiseGame(Player player1, Player player2, int numberOfCellsPerRow, Player tossWinner);
    Player addPlayer(String playerName, PlayerType playerType);
    Player conductTossToDecideWhoGoesFirst(Player player1, Player player2);
    Game startGameFromId(String gameId) throws IOException;
    Game getGameFromId(String gameId);
    void printBoard(Game game);

}
