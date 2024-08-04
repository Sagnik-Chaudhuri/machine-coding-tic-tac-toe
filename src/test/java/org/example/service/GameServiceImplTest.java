package org.example.service;

import org.example.model.*;
import org.example.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceImplTest {
    GameServiceImpl gameService;
    GameRepository mockRepo;

    @BeforeEach
    void setUp() {
        mockRepo = Mockito.mock(GameRepository.class);
        gameService = new GameServiceImpl(mockRepo);
    }

    @Test
    void initialiseGame() {
        Player player1 = Player.builder().name("Player1").playerType(PlayerType.HUMAN).build();
        Player player2 = Player.builder().name("Player2").playerType(PlayerType.COMPUTER).build();
        Player tossWinner = player1;
        int numberOfCellsPerRow = 3;

        Game mockGame = Game.builder().id(UUID.randomUUID().toString()).gameState(GameState.ONGOING)
                .players(Arrays.asList(player1, player2)).board(Board.builder().build()).build();
        when(mockRepo.initialiseGame(any(Player.class), any(Player.class), anyInt(), any(Player.class)))
                .thenReturn(mockGame);

        Game game = gameService.initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);

        assertNotNull(game);
        assertEquals(GameState.ONGOING, game.getGameState());
        verify(mockRepo, times(1)).initialiseGame(player1, player2, numberOfCellsPerRow, tossWinner);
    }

    @Test
    void addPlayer() {
        Player player = Player.builder().name("Player1").playerType(PlayerType.HUMAN).build();
        when(mockRepo.addPlayer(any(Player.class))).thenReturn(player);

        Player addedPlayer = gameService.addPlayer("Player1", PlayerType.HUMAN);

        assertNotNull(addedPlayer);
        assertEquals("Player1", addedPlayer.getName());
        verify(mockRepo, times(1)).addPlayer(any(Player.class));
    }

    @Test
    void conductTossToDecideWhoGoesFirst() {
        Player player1 = Player.builder().name("Player1").playerType(PlayerType.HUMAN).build();
        Player player2 = Player.builder().name("Player2").playerType(PlayerType.COMPUTER).build();

        Player winner = gameService.conductTossToDecideWhoGoesFirst(player1, player2);

        assertTrue(winner == player1 || winner == player2);
        verifyNoInteractions(mockRepo);
    }


    @Test
    void startGameFromId_throwsIllegalArgumentException() throws IOException {
        String gameId = UUID.randomUUID().toString();
        Game mockGame = mock(Game.class);
        Board mockBoard = mock(Board.class);

        GameRepository mockRepo = mock(GameRepository.class);
        GameServiceImpl gameService = spy(new GameServiceImpl(mockRepo)); // Use spy to stub methods

        // Stubbing methods on the mock game object
        when(mockGame.getGameState()).thenReturn(GameState.ONGOING, GameState.DRAW);
        when(mockRepo.getGameFromId(gameId)).thenReturn(mockGame);

        Player player1 = Player.builder()
                .id((UUID.randomUUID().toString()))
                .name("Alice")
                .playerType(PlayerType.HUMAN)
                .markCellValueAs(CellValue.X)
                .build();
        Player player2 = Player.builder()
                .id((UUID.randomUUID().toString()))
                .name("Bob")
                .playerType(PlayerType.COMPUTER)
                .markCellValueAs(CellValue.O)
                .build();
        when(mockGame.getPlayers()).thenReturn(Arrays.asList(player1, player2));
        when(mockGame.getCurrentPlayerIndex()).thenReturn(0);

        when(mockGame.getBoard()).thenReturn(mockBoard);
        when(mockBoard.getNumberOfCellsPerRow()).thenReturn(3);
        CellValue[][] cellValues = {
                {CellValue.X, CellValue.O, CellValue.EMPTY},
                {CellValue.EMPTY, CellValue.X, CellValue.O},
                {CellValue.O, CellValue.EMPTY, CellValue.X}
        };
        when(mockBoard.getCellValues()).thenReturn(cellValues);
        when(mockRepo.getBoardFromGame(mockGame)).thenReturn(mockBoard);

        Move mockMove = mock(Move.class);
        when(mockRepo.createMove(any(Player.class), anyInt(), anyInt())).thenReturn(mockMove);
        when(mockRepo.updateGameBoardWithMove(any(Move.class), any(Board.class))).thenReturn(true);
        when(mockRepo.addMoveToMovesList(anyString(), any(Move.class))).thenReturn(true);

        // Stubbing the printBoard method to avoid NPE
        doNothing().when(gameService).printBoard(any(Game.class));

        // Stubbing getCurrentPlayerPosition to return an invalid position to trigger IllegalArgumentException
        doReturn("invalid").when(gameService).getCurrentPlayerPosition(any(Player.class), any(Game.class));

        // Asserting that IllegalArgumentException is thrown
        assertThrows(IllegalArgumentException.class, () -> {
            gameService.startGameFromId(gameId);
        });

        // Verify other interactions if needed
        verify(mockRepo, times(1)).getGameFromId(gameId);
        verify(mockGame, times(1)).getGameState(); // Since we are stubbing it twice
        verify(mockGame, times(1)).getPlayers();
        verify(mockGame, times(1)).getCurrentPlayerIndex();
        verify(gameService, times(1)).getCurrentPlayerPosition(any(Player.class), any(Game.class)); // Verify stubbed method
    }


    @Test
    void getGameFromId() {
        String gameId = UUID.randomUUID().toString();
        Game mockGame = Game.builder().id(gameId).gameState(GameState.ONGOING).build();
        when(mockRepo.getGameFromId(gameId)).thenReturn(mockGame);

        Game game = gameService.getGameFromId(gameId);

        assertNotNull(game);
        assertEquals(gameId, game.getId());
        verify(mockRepo, times(1)).getGameFromId(gameId);
    }

    @Test
    void printBoard() {
        Game mockGame = mock(Game.class);
        Board mockBoard = mock(Board.class);
        when(mockGame.getBoard()).thenReturn(mockBoard);
        when(mockRepo.getBoardFromGame(mockGame)).thenReturn(mockBoard);
        when(mockBoard.getNumberOfCellsPerRow()).thenReturn(3);
        CellValue[][] cellValues = {
                {CellValue.X, CellValue.O, CellValue.EMPTY},
                {CellValue.EMPTY, CellValue.X, CellValue.O},
                {CellValue.O, CellValue.EMPTY, CellValue.X}
        };
        when(mockBoard.getCellValues()).thenReturn(cellValues);

        gameService.printBoard(mockGame);

        verify(mockRepo, times(1)).getBoardFromGame(mockGame);
    }

}
