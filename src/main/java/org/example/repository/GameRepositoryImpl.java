package org.example.repository;

import org.example.exception.UserException;
import org.example.model.*;
import org.example.util.LoggerConfig;

import java.util.*;
import java.util.logging.Logger;

public class GameRepositoryImpl implements GameRepository {
    private final Map<String, Player> playerMap = new HashMap<>();
    private final Map<String, Game> gameMap = new HashMap<>();

    private static final Logger logger = Logger.getLogger(GameRepositoryImpl.class.getName());

    static {
        LoggerConfig.configureLogger(logger);
    }

    /**
     * @param player1
     * @param player2
     * @param numberOfCellsPerRow
     * @return returns a created Game object
     */
    @Override
    public Game initialiseGame(Player player1, Player player2, int numberOfCellsPerRow, Player tossWinner) {
        Board board = initialiseBoard(numberOfCellsPerRow);
        Game game = Game.builder()
                .id(UUID.randomUUID().toString())
                .gameState(GameState.ONGOING)
                .moves(new ArrayList<>())
                .players(new ArrayList<>(Arrays.asList(player1, player2)))
                .board(board)
                .build();
        gameMap.put(game.getId(), game);

        int currentPlayerIndex = getCurrentPlayerIndexFromTossWinner(tossWinner, game.getId());
        game.setCurrentPlayerIndex(currentPlayerIndex);


        for(int index = 0; index < game.getPlayers().size(); index ++) {
            if (currentPlayerIndex != index) {
                game.getPlayers().get(index).setMarkCellValueAs(CellValue.O);
            }
        }


        return game;
    }

    private int getCurrentPlayerIndexFromTossWinner(Player tossWinner, String gameId) {
        Game game = getGameFromId(gameId);
        for(int index = 0; index < game.getPlayers().size(); index++) {
            if (Objects.equals(tossWinner.getId(), game.getPlayers().get(index).getId())) {
                return index;
            }
        }
        return 0;
    }

    private Board initialiseBoard(int numberOfCellsPerRow) {
        CellValue[][] cellValues = new CellValue[numberOfCellsPerRow][numberOfCellsPerRow];
        for(int i = 0; i < numberOfCellsPerRow; i++) {
            Arrays.fill(cellValues[i], CellValue.EMPTY);
        }

        return Board.builder()
                .cellValues(cellValues)
                .numberOfCellsPerRow(numberOfCellsPerRow)
                .build();
    }


    /**
     * @param player
     * @return a player object
     */
    @Override
    public Player addPlayer(Player player) {
        String playerId = UUID.randomUUID().toString();
        player.setId(playerId);
        playerMap.put(playerId, player);
        return player;
    }

    /**
     * @param gameId
     * @return game object with given gameId
     */
    @Override
    public Game getGameFromId(String gameId) {
        Game game = gameMap.getOrDefault(gameId, null);
        if (Objects.isNull(game)) {
            throw new UserException("Game id not found: " + gameId);
        }
        return game;
    }


    @Override
    public Board getBoardFromGame(Game game) {
        return game.getBoard();
    }

    /**
     * @param player
     * @param rowValue
     * @param columnValue
     * @return a Move object
     */
    @Override
    public Move createMove(Player player, int rowValue, int columnValue) {
        return Move.builder()
                .rowValue(rowValue)
                .columnValue(columnValue)
                .player(player)
                .build();
    }

    /**
     * @param gameId
     * @return true(boolean) corresponding to true if move is added to Game's list of moves
     */
    @Override
    public boolean addMoveToMovesList(String gameId, Move move) {
        Game game = getGameFromId(gameId);
        List<Move> gameMoves = game.getMoves();
        gameMoves.add(move);
        return true;
    }

    @Override
    public boolean updateGameBoardWithMove(Move move, Board board) {
        int row = move.getRowValue();
        int col = move.getColumnValue();
        Player player = move.getPlayer();
        CellValue markCellValueAs = player.getMarkCellValueAs();
        board.getCellValues()[row][col] = markCellValueAs;
        return true;
    }
}
