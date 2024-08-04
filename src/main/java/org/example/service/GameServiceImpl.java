package org.example.service;

import org.example.exception.UserException;
import org.example.model.*;
import org.example.repository.GameRepository;
import org.example.repository.GameRepositoryImpl;
import org.example.util.LoggerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    private static final Logger logger = Logger.getLogger(GameServiceImpl.class.getName());

    static {
        LoggerConfig.configureLogger(logger);
    }

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * @param player1
     * @param player2
     * @param numberOfCellsPerRow
     * @return Game object
     */
    @Override
    public Game initialiseGame(Player player1, Player player2, int numberOfCellsPerRow, Player tossWinner) {
        tossWinner.setMarkCellValueAs(CellValue.X);
        return gameRepository.initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);
    }

    /**
     * @param playerName
     * @param playerType
     * @return Created Player object
     */
    @Override
    public Player addPlayer(String playerName, PlayerType playerType) {
        Player player = Player.builder()
                .name(playerName)
                .playerType(playerType)
                .build();
        return gameRepository.addPlayer(player);
    }

    /**
     * @param player1
     * @param player2
     * @return conducts toss and returns Player object who wins the toss
     */
    @Override
    public Player conductTossToDecideWhoGoesFirst(Player player1, Player player2) {
        Random random = new Random();
        boolean nextBoolean = random.nextBoolean();
        if (nextBoolean == true) {
            logger.log(Level.INFO, "Player 1: "+ player1.getName() + " has won the toss: " + player1);
            return player1;
        } else {
            logger.log(Level.INFO, "Player 2: "+ player2.getName() + " has won the toss: " + player2);
            return player2;
        }
    }


    /**
     * Main Business Logic of the game resides here
     * Runs an infinite loop till game state remains ongoing
     * Checks who is the current player, and accordingly creates his move, plays it and stores it in game object
     * @param gameId
     * @return game object after the game ends
     */
    @Override
    public Game startGameFromId(String gameId) throws IOException {
        logger.log(Level.INFO, "Game started");
        Game game = getGameFromId(gameId);

        while(game.getGameState() == GameState.ONGOING) {
            printBoard(game);
            int currentPlayerIndex = game.getCurrentPlayerIndex();
            Player currentPlayer = game.getPlayers().get(currentPlayerIndex);
            System.out.println("Player: " + currentPlayer.getName() + "'s move with symbol: " + currentPlayer.getMarkCellValueAs());

            String currentPlayerPosition = getCurrentPlayerPosition(currentPlayer, game);

            int currentPlayerPositionInt = Integer.parseInt(currentPlayerPosition);
            int numberOfCellsPerRow = game.getBoard().getNumberOfCellsPerRow();
            if (currentPlayerPositionInt > numberOfCellsPerRow * numberOfCellsPerRow || currentPlayerPositionInt < 1) {
                throw new IllegalArgumentException("Input Position entered is invalid");
            }

            int[] rowAndCol = getEquivalentRowAndColumnFromPositionValue(currentPlayerPosition, game.getBoard().getNumberOfCellsPerRow());
            int row = rowAndCol[0];
            int col = rowAndCol[1];
            Board board = game.getBoard();
            if (board.getCellValues()[row][col] != CellValue.EMPTY) {
                throw new IllegalArgumentException("Input position entered is already captured");
            }

            Move move = gameRepository.createMove(currentPlayer, row, col);
            gameRepository.updateGameBoardWithMove(move, board);
            gameRepository.addMoveToMovesList(gameId, move);

            System.out.println("Player " + currentPlayer.getName() + " filled the position: " + currentPlayerPosition);

            boolean isWinnerDetermined = determineWinner(board, move);
            if (isWinnerDetermined) {
                switch (currentPlayer.getPlayerType()) {
                    case COMPUTER:
                        game.setGameState(GameState.WINNER_PLAYER_COMPUTER);
                        game.setWinner(currentPlayer);
                        printBoard(game);
                        return game;
                    case HUMAN:
                        game.setGameState(GameState.WINNER_PLAYER_HUMAN);
                        game.setWinner(currentPlayer);
                        printBoard(game);
                        return game;
                    default:
                        game.setGameState(GameState.ONGOING);
                }
            }

            if (game.getMoves().size() == board.getNumberOfCellsPerRow()*board.getNumberOfCellsPerRow()) {
                game.setGameState(GameState.DRAW);
                return game;
            }

            flipTurns(game);

        }
        return game;

    }

    /**
     * @param board
     * @param move
     * @return boolean: true if a winner has been determined post playing the current move, else false
     */

    private boolean determineWinner(Board board, Move move) {
        int currentRow = move.getRowValue();
        int currentCol = move.getColumnValue();
        CellValue cellValue = move.getPlayer().getMarkCellValueAs();

        // match rowWise
        int matchingCellValuesCount = 0;
        for(int index = 0; index < board.getNumberOfCellsPerRow(); index++) {
            if (Objects.equals(cellValue, board.getCellValues()[currentRow][index])) {
                matchingCellValuesCount ++;
            }
        }
        if (matchingCellValuesCount == board.getNumberOfCellsPerRow()) {
            return true;
        }

        // match columnWise
        matchingCellValuesCount = 0;
        for(int index = 0; index < board.getNumberOfCellsPerRow(); index++) {
            if (Objects.equals(cellValue, board.getCellValues()[index][currentCol])) {
                matchingCellValuesCount ++;
            }
        }
        if (matchingCellValuesCount == board.getNumberOfCellsPerRow()) {
            return true;
        }

        // match diagonalWise
        matchingCellValuesCount = 0;
        for(int index = 0; index < board.getNumberOfCellsPerRow(); index++) {
            if (Objects.equals(cellValue, board.getCellValues()[index][index])) {
                matchingCellValuesCount ++;
            }
        }
        if (matchingCellValuesCount == board.getNumberOfCellsPerRow()) {
            return true;
        }

        // match antiDiagonalWise
        matchingCellValuesCount = 0;
        for(int index = 0; index < board.getNumberOfCellsPerRow(); index++) {
            if (Objects.equals(cellValue, board.getCellValues()[index][board.getNumberOfCellsPerRow() - 1 - index])) {
                matchingCellValuesCount ++;
            }
        }
        if (matchingCellValuesCount == board.getNumberOfCellsPerRow()) {
            return true;
        }

        return false;
    }

    /**
     * @param currentPlayer
     * @param game
     * @return get player position as a string representing the position value, for example, "1", "5" are valid positions where a move can be made
     * @throws IOException
     */
    public String getCurrentPlayerPosition(Player currentPlayer, Game game) throws IOException {
        return currentPlayer.getPlayerType() == PlayerType.HUMAN
                ? getCurrentPlayerPositionForHuman()
                : getCurrentPlayerPositionForComputer(game.getBoard(), currentPlayer);
    }

    private String getCurrentPlayerPositionForHuman() throws IOException {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        logger.log(Level.INFO, "Please Enter a position from 1-9");
        return input.readLine();
    }

    /**
     * Checks for potential winning move, potential blocking move, capture centre if available, capture any empty corner
     * Uses a simple strategy to determine the desirable position
     * @param board
     * @param currentPlayer
     * @return get the most desirable position that should be captured by the player computer, in order for it to win
     */
    private String getCurrentPlayerPositionForComputer(Board board, Player currentPlayer) {
        int numberOfCellsPerRow = board.getNumberOfCellsPerRow();
        CellValue[][] cellValues = board.getCellValues();

        // Create a dummy opponent player for the move checking
        Player opponentPlayer = Player.builder().markCellValueAs(currentPlayer.getMarkCellValueAs() == CellValue.O ? CellValue.X : CellValue.O).build();

        // 1. Check for a winning move
        String winningMove = findWinningMove(board, currentPlayer);
        if (winningMove != null) {
            return winningMove;
        }

        // 2. Check for a blocking move
        String blockingMove = findWinningMove(board, opponentPlayer);
        if (blockingMove != null) {
            return blockingMove;
        }

        // 3. Take the center if available
        int center = numberOfCellsPerRow / 2;
        if (cellValues[center][center] == CellValue.EMPTY) {
            return String.valueOf(getEquivalentPositionValueFromRowAndColumn(center, center, numberOfCellsPerRow));
        }

        // 4. Take any available corner
        int[][] corners = {
                {0, 0},
                {0, numberOfCellsPerRow - 1},
                {numberOfCellsPerRow - 1, 0},
                {numberOfCellsPerRow - 1, numberOfCellsPerRow - 1}
        };
        for (int[] corner : corners) {
            if (cellValues[corner[0]][corner[1]] == CellValue.EMPTY) {
                return String.valueOf(getEquivalentPositionValueFromRowAndColumn(corner[0], corner[1], numberOfCellsPerRow));
            }
        }

        // 5. Take any available side
        for (int i = 0; i < numberOfCellsPerRow; i++) {
            for (int j = 0; j < numberOfCellsPerRow; j++) {
                if (cellValues[i][j] == CellValue.EMPTY) {
                    return String.valueOf(getEquivalentPositionValueFromRowAndColumn(i, j, numberOfCellsPerRow));
                }
            }
        }

        return null; // Should never reach here if there are empty cells
    }

    /**
     * Finds winning move for a setup
     * @param board
     * @param player
     * @return
     */

    private String findWinningMove(Board board, Player player) {
        int numberOfCellsPerRow = board.getNumberOfCellsPerRow();
        CellValue[][] cellValues = board.getCellValues();

        for (int i = 0; i < numberOfCellsPerRow; i++) {
            for (int j = 0; j < numberOfCellsPerRow; j++) {
                if (cellValues[i][j] == CellValue.EMPTY) {
                    cellValues[i][j] = player.getMarkCellValueAs(); // Try the move
                    Move move = Move.builder().rowValue(i).columnValue(j).player(player).build();
                    if (determineWinner(board, move)) {
                        cellValues[i][j] = CellValue.EMPTY; // Reset to original state
                        return String.valueOf(getEquivalentPositionValueFromRowAndColumn(i, j, numberOfCellsPerRow));
                    }
                    cellValues[i][j] = CellValue.EMPTY; // Reset to original state
                }
            }
        }

        return null;
    }


    private void flipTurns(Game game) {
        int currentPlayerIndex = game.getCurrentPlayerIndex();
        currentPlayerIndex ++;
        game.setCurrentPlayerIndex(currentPlayerIndex % game.getPlayers().size());
    }

    /**
     * @param gameId
     * @return
     */
    @Override
    public Game getGameFromId(String gameId) {
        return gameRepository.getGameFromId(gameId);
    }

    /**
     * @param game
     */
    @Override
    public void printBoard(Game game) {
        Board board = gameRepository.getBoardFromGame(game);
        for(int i = 0; i < board.getNumberOfCellsPerRow(); i++) {
            for(int j = 0; j < board.getNumberOfCellsPerRow(); j++) {
                String cellStringValueFromCellValue = getCellStringValueFromCellValue(board.getCellValues()[i][j]);
                if (Objects.equals(cellStringValueFromCellValue, " ")) {
                    cellStringValueFromCellValue = String.valueOf(getEquivalentPositionValueFromRowAndColumn(i, j, board.getNumberOfCellsPerRow()));
                }
                System.out.print("| " + cellStringValueFromCellValue + " |");
            }
            System.out.println("\n----------------");
        }
    }

    private int getEquivalentPositionValueFromRowAndColumn(int row, int col, int numberOfCellsPerRow) {
        return row*numberOfCellsPerRow + col + 1;
    }

    private int[] getEquivalentRowAndColumnFromPositionValue(String position, int numberOfCellsPerRow) {
        int row = (Integer.parseInt(position) - 1) / numberOfCellsPerRow;
        int col = (Integer.parseInt(position) - 1) % numberOfCellsPerRow;
        return new int[]{row, col};
    }

    private String getCellStringValueFromCellValue(CellValue cellValue) {
        switch (cellValue) {
            case O:
                return "O";
            case X:
                return "X";
            case EMPTY:
                return " ";
        }
        return null;
    }
}
