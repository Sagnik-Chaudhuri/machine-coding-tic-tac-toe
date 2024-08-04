package org.example.controller;

import org.example.Main;
import org.example.exception.UserException;
import org.example.model.Game;
import org.example.model.Player;
import org.example.model.PlayerType;
import org.example.service.GameService;
import org.example.util.LoggerConfig;

import java.util.Objects;
import java.util.logging.Logger;

public class GameController {
    private final GameService gameService;
    private static final Logger logger = Logger.getLogger(GameController.class.getName());

    static {
        LoggerConfig.configureLogger(logger);
    }

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    public Player conductTossToDecideWhoGoesFirst(Player player1, Player player2) {
        if (Objects.isNull(player1)) {
            throw new UserException("Player 1 details is empty");
        } else if(Objects.isNull(player2)) {
            throw new UserException("Player 2 details is empty");
        }
        try {
            return gameService.conductTossToDecideWhoGoesFirst(player1, player2);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Player addPlayer(String name, PlayerType playerType) {
        if (Objects.isNull(name) || name.isEmpty() || Objects.isNull(playerType)) {
            throw new IllegalArgumentException("Player name is empty");
        }
        try {
            return gameService.addPlayer(name, playerType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Game initialiseGame(Player player1, Player player2, int numberOfCellsPerRow, Player tossWinner) {
        if (numberOfCellsPerRow == 0) {
            throw new IllegalArgumentException("numberOfCellsPerRow cannot be 0");
        }
        try {
            return gameService.initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Game startGameFromId(String gameId) {
        if (Objects.isNull(gameId) || gameId.isEmpty()) {
            throw new UserException("game id cannot be empty");
        }
        try {
            return gameService.startGameFromId(gameId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
