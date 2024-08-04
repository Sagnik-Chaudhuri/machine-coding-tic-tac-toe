package org.example;

import org.example.controller.GameController;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.PlayerType;
import org.example.repository.GameRepository;
import org.example.repository.GameRepositoryImpl;
import org.example.service.GameService;
import org.example.service.GameServiceImpl;
import org.example.util.LoggerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LoggerConfig.configureLogger(logger);
        logger.log(Level.INFO,"Game started");

        try {
            runGame();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading input: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Error: " + e.getMessage());
        }
    }

    private static void runGame() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        GameRepository gameRepository = new GameRepositoryImpl();
        GameService gameService = new GameServiceImpl(gameRepository);
        GameController gameController = new GameController(gameService);

        logger.log(Level.INFO,"Enter player name");
        String playerName1 = input.readLine();
        Player player1 = gameController.addPlayer(playerName1, PlayerType.HUMAN);

        String playerName2 = "Computer";
        Player player2 = gameController.addPlayer(playerName2, PlayerType.COMPUTER);
//        Player player2 = gameController.addPlayer(playerName2, PlayerType.HUMAN);

        logger.log(Level.INFO,"Enter number of cells per row/column");
        int numberOfCellsPerRow = Integer.parseInt(input.readLine());

        Player tossWinner = gameController.conductTossToDecideWhoGoesFirst(player1, player2);
        logger.log(Level.INFO,"Player name who won the toss is: " + tossWinner.getName());


        Game gameObj = gameController.initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);
        String gameId = gameObj.getId();
        logger.log(Level.INFO, gameObj.toString());

        gameObj = gameController.startGameFromId(gameId);
        logger.log(Level.INFO,"Game State: " + gameObj.getGameState() + "\ngame result: " + gameObj);
    }
}


