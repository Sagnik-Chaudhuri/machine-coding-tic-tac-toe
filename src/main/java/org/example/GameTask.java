package org.example;

import org.example.controller.GameController;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.PlayerType;
import org.example.service.GameService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameTask implements Runnable {
    private static final Logger logger = Logger.getLogger(GameTask.class.getName());
    private final int gameNumber;
    private final GameService gameService;

    public GameTask(int gameNumber, GameService gameService) {
        this.gameNumber = gameNumber;
        this.gameService = gameService;
    }

    @Override
    public void run() {
        try {
            GameController gameController = new GameController(gameService);

            logger.log(Level.INFO, "Starting game " + gameNumber);

            // Simulated inputs
            String playerName1 = "Player" + gameNumber;
            Player player1 = gameController.addPlayer(playerName1, PlayerType.HUMAN);

            String playerName2 = "Computer" + gameNumber;
            Player player2 = gameController.addPlayer(playerName2, PlayerType.COMPUTER);

            int numberOfCellsPerRow = 3;

            Player tossWinner = gameController.conductTossToDecideWhoGoesFirst(player1, player2);
            logger.log(Level.INFO, "Player who won the toss in game " + gameNumber + " is: " + tossWinner.getName());

            Game gameObj = gameController.initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);
            String gameId = gameObj.getId();
            logger.log(Level.INFO, "Game " + gameNumber + " initialized: " + gameObj);

            gameObj = gameController.startGameFromId(gameId);
            logger.log(Level.INFO, "Game State for game " + gameNumber + ": " + gameObj.getGameState() + "\nGame result: " + gameObj);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in game execution: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
