package org.example.repository;

import org.example.model.*;

import java.util.List;

public interface GameRepository {
    Game initialiseGame(Player player1, Player player2, int numberOfCellsPerRow, Player tossWinner);
    Player addPlayer(Player player);
    Game getGameFromId(String gameId);
    Board getBoardFromGame(Game game);
    Move createMove(Player player, int rowValue, int columnValue);
    boolean addMoveToMovesList(String gameId, Move move);
    boolean updateGameBoardWithMove(Move move, Board board);
}
