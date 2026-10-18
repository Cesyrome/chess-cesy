package com.cesy.chessapp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameState {
    private String[][] board = new String[8][8];
    private int turnCount = 1;
    private String currentTurn = "Usuarios";
    private Integer selectedRow = null;
    private Integer selectedCol = null;
    private String errorMessage = null;
    private List<String> capturedByUsuarios = new ArrayList<>();
    private List<String> capturedByProgramas = new ArrayList<>();

    // Banderas para el Enroque
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookLMoved = false;
    private boolean whiteRookRMoved = false;
    private boolean blackRookLMoved = false;
    private boolean blackRookRMoved = false;

    private static final List<String> WHITE_PIECES = Arrays.asList("♙", "♖", "♘", "♗", "♕", "♔");
    private static final List<String> BLACK_PIECES = Arrays.asList("♟", "♜", "♞", "♝", "♛", "♚");

    public GameState() {
        initializeBoard();
    }

    private void initializeBoard() {
        board[0] = new String[]{"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"};
        board[1] = new String[]{"♟", "♟", "♟", "♟", "♟", "♟", "♟", "♟"};
        board[6] = new String[]{"♙", "♙", "♙", "♙", "♙", "♙", "♙", "♙"};
        board[7] = new String[]{"♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖"};
    }

    public boolean isWhite(String piece) { return WHITE_PIECES.contains(piece); }
    public boolean isBlack(String piece) { return BLACK_PIECES.contains(piece); }

    // Getters y Setters
    public List<String> getCapturedByUsuarios() { return capturedByUsuarios; }
    public void setCapturedByUsuarios(List<String> capturedByUsuarios) { this.capturedByUsuarios = capturedByUsuarios; }
    public List<String> getCapturedByProgramas() { return capturedByProgramas; }
    public void setCapturedByProgramas(List<String> capturedByProgramas) { this.capturedByProgramas = capturedByProgramas; }
    public String[][] getBoard() { return board; }
    public void setBoard(String[][] board) { this.board = board; }
    public int getTurnCount() { return turnCount; }
    public void setTurnCount(int turnCount) { this.turnCount = turnCount; }
    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }
    public Integer getSelectedRow() { return selectedRow; }
    public void setSelectedRow(Integer selectedRow) { this.selectedRow = selectedRow; }
    public Integer getSelectedCol() { return selectedCol; }
    public void setSelectedCol(Integer selectedCol) { this.selectedCol = selectedCol; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Getters y Setters de Enroque
    public boolean isWhiteKingMoved() { return whiteKingMoved; }
    public void setWhiteKingMoved(boolean whiteKingMoved) { this.whiteKingMoved = whiteKingMoved; }
    public boolean isBlackKingMoved() { return blackKingMoved; }
    public void setBlackKingMoved(boolean blackKingMoved) { this.blackKingMoved = blackKingMoved; }
    public boolean isWhiteRookLMoved() { return whiteRookLMoved; }
    public void setWhiteRookLMoved(boolean whiteRookLMoved) { this.whiteRookLMoved = whiteRookLMoved; }
    public boolean isWhiteRookRMoved() { return whiteRookRMoved; }
    public void setWhiteRookRMoved(boolean whiteRookRMoved) { this.whiteRookRMoved = whiteRookRMoved; }
    public boolean isBlackRookLMoved() { return blackRookLMoved; }
    public void setBlackRookLMoved(boolean blackRookLMoved) { this.blackRookLMoved = blackRookLMoved; }
    public boolean isBlackRookRMoved() { return blackRookRMoved; }
    public void setBlackRookRMoved(boolean blackRookRMoved) { this.blackRookRMoved = blackRookRMoved; }
}