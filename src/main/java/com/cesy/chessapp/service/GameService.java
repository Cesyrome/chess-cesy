package com.cesy.chessapp.service;

import com.cesy.chessapp.model.GameState;
import com.cesy.chessapp.repository.GameRepository;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    private final GameRepository repository;

    public GameService(GameRepository repository) {
        this.repository = repository;
    }

    public GameState getGameState() {
        return repository.getGameState();
    }

    public void processInteraction(int row, int col) {
        GameState state = repository.getGameState();
        String[][] board = state.getBoard();
        state.setErrorMessage(null);

        if (state.getSelectedRow() == null) {
            String piece = board[row][col];
            if (piece != null) {
                boolean isWhiteTurn = state.getCurrentTurn().equals("Usuarios");
                if ((isWhiteTurn && state.isWhite(piece)) || (!isWhiteTurn && state.isBlack(piece))) {
                    state.setSelectedRow(row);
                    state.setSelectedCol(col);
                } else {
                    state.setErrorMessage("No puedes mover los programas del oponente.");
                }
            }
        } else {
            int startRow = state.getSelectedRow();
            int startCol = state.getSelectedCol();
            String piece = board[startRow][startCol];
            String targetPiece = board[row][col];
            boolean isWhiteTurn = state.getCurrentTurn().equals("Usuarios");

            if (startRow == row && startCol == col) {
                state.setSelectedRow(null); state.setSelectedCol(null);
                repository.saveGameState(state); return;
            }

            if (targetPiece != null && ((isWhiteTurn && state.isWhite(targetPiece)) || (!isWhiteTurn && state.isBlack(targetPiece)))) {
                state.setErrorMessage("Colisión con programa aliado.");
                state.setSelectedRow(null); state.setSelectedCol(null);
                repository.saveGameState(state); return;
            }

            boolean isCastling = false;

            // Lógica de Enroque
            if ((piece.equals("♔") || piece.equals("♚")) && Math.abs(col - startCol) == 2) {
                if (row != startRow) {
                    state.setErrorMessage("Movimiento de enroque inválido.");
                    state.setSelectedRow(null); state.setSelectedCol(null); return;
                }
                boolean isKingside = col > startCol;
                int rookCol = isKingside ? 7 : 0;

                if (isWhiteTurn) {
                    if (state.isWhiteKingMoved() || (isKingside ? state.isWhiteRookRMoved() : state.isWhiteRookLMoved())) {
                        state.setErrorMessage("No se puede enrocar: piezas ya movidas.");
                        state.setSelectedRow(null); state.setSelectedCol(null); return;
                    }
                } else {
                    if (state.isBlackKingMoved() || (isKingside ? state.isBlackRookRMoved() : state.isBlackRookLMoved())) {
                        state.setErrorMessage("No se puede enrocar: piezas ya movidas.");
                        state.setSelectedRow(null); state.setSelectedCol(null); return;
                    }
                }

                if (!isPathClear(board, startRow, startCol, row, rookCol)) {
                    state.setErrorMessage("No se puede enrocar: hay piezas en el medio.");
                    state.setSelectedRow(null); state.setSelectedCol(null); return;
                }

                if (isKingInCheck(board, isWhiteTurn) ||
                        movePutsKingInCheck(board, startRow, startCol, row, startCol + (isKingside ? 1 : -1), isWhiteTurn) ||
                        movePutsKingInCheck(board, startRow, startCol, row, col, isWhiteTurn)) {
                    state.setErrorMessage("No se puede enrocar en jaque o pasando por jaque.");
                    state.setSelectedRow(null); state.setSelectedCol(null); return;
                }
                isCastling = true;
            } else {
                // Validación de movimiento normal y Jaque
                if (!isValidMoveBasic(piece, startRow, startCol, row, col, board, isWhiteTurn)) {
                    state.setErrorMessage("Violación de los parámetros de la Red.");
                    state.setSelectedRow(null); state.setSelectedCol(null); return;
                }
                if (movePutsKingInCheck(board, startRow, startCol, row, col, isWhiteTurn)) {
                    state.setErrorMessage("Movimiento inválido: El Rey quedaría en Jaque.");
                    state.setSelectedRow(null); state.setSelectedCol(null); return;
                }
            }

            // Ejecutar Movimiento
            if (targetPiece != null) {
                if (isWhiteTurn) state.getCapturedByUsuarios().add(targetPiece);
                else state.getCapturedByProgramas().add(targetPiece);
            }

            board[row][col] = piece;
            board[startRow][startCol] = null;

            // Ejecutar movimiento de la Torre si es Enroque
            if (isCastling) {
                boolean isKingside = col > startCol;
                int rookCol = isKingside ? 7 : 0;
                int newRookCol = isKingside ? col - 1 : col + 1;
                board[row][newRookCol] = board[row][rookCol];
                board[row][rookCol] = null;
            }

            // Actualizar banderas de Enroque
            if (piece.equals("♔")) state.setWhiteKingMoved(true);
            if (piece.equals("♚")) state.setBlackKingMoved(true);
            if (piece.equals("♖")) {
                if (startCol == 0) state.setWhiteRookLMoved(true);
                if (startCol == 7) state.setWhiteRookRMoved(true);
            }
            if (piece.equals("♜")) {
                if (startCol == 0) state.setBlackRookLMoved(true);
                if (startCol == 7) state.setBlackRookRMoved(true);
            }

            // Promoción del Peón
            if (piece.equals("♙") && row == 0) board[row][col] = "♕";
            if (piece.equals("♟") && row == 7) board[row][col] = "♛";

            state.setSelectedRow(null);
            state.setSelectedCol(null);
            state.setTurnCount(state.getTurnCount() + 1);
            state.setCurrentTurn(isWhiteTurn ? "Programas" : "Usuarios");

            // Validar Jaque Mate o Jaque al oponente
            if (isCheckmate(board, !isWhiteTurn)) {
                state.setErrorMessage("¡JAQUE MATE! Fin de la transmisión.");
            } else if (isKingInCheck(board, !isWhiteTurn)) {
                state.setErrorMessage("¡ADVERTENCIA: Rey en Jaque!");
            }
        }
        repository.saveGameState(state);
    }

    public void resetGame() {
        repository.saveGameState(new GameState());
    }

    // --- MÉTODOS DE VALIDACIÓN ---

    private boolean isValidMoveBasic(String piece, int r1, int c1, int r2, int c2, String[][] board, boolean isWhite) {
        int rowDiff = Math.abs(r2 - r1);
        int colDiff = Math.abs(c2 - c1);

        switch (piece) {
            case "♙":
                if (c1 == c2 && board[r2][c2] == null) {
                    if (r1 - r2 == 1) return true;
                    if (r1 == 6 && r1 - r2 == 2 && board[r1 - 1][c1] == null) return true;
                }
                if (colDiff == 1 && r1 - r2 == 1 && board[r2][c2] != null) return true;
                return false;
            case "♟":
                if (c1 == c2 && board[r2][c2] == null) {
                    if (r2 - r1 == 1) return true;
                    if (r1 == 1 && r2 - r1 == 2 && board[r1 + 1][c1] == null) return true;
                }
                if (colDiff == 1 && r2 - r1 == 1 && board[r2][c2] != null) return true;
                return false;
            case "♖": case "♜":
                return (r1 == r2 || c1 == c2) && isPathClear(board, r1, c1, r2, c2);
            case "♗": case "♝":
                return (rowDiff == colDiff) && isPathClear(board, r1, c1, r2, c2);
            case "♕": case "♛":
                return (r1 == r2 || c1 == c2 || rowDiff == colDiff) && isPathClear(board, r1, c1, r2, c2);
            case "♘": case "♞":
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            case "♔": case "♚":
                return rowDiff <= 1 && colDiff <= 1;
            default:
                return false;
        }
    }

    private boolean isPathClear(String[][] board, int r1, int c1, int r2, int c2) {
        int rowDir = Integer.compare(r2, r1);
        int colDir = Integer.compare(c2, c1);
        int r = r1 + rowDir;
        int c = c1 + colDir;
        while (r != r2 || c != c2) {
            if (board[r][c] != null) return false;
            r += rowDir;
            c += colDir;
        }
        return true;
    }

    private boolean movePutsKingInCheck(String[][] board, int r1, int c1, int r2, int c2, boolean isWhite) {
        String[][] tempBoard = new String[8][8];
        for (int i = 0; i < 8; i++) System.arraycopy(board[i], 0, tempBoard[i], 0, 8);
        tempBoard[r2][c2] = tempBoard[r1][c1];
        tempBoard[r1][c1] = null;
        return isKingInCheck(tempBoard, isWhite);
    }

    private boolean isKingInCheck(String[][] board, boolean isWhite) {
        int kingRow = -1, kingCol = -1;
        String kingPiece = isWhite ? "♔" : "♚";

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (kingPiece.equals(board[r][c])) {
                    kingRow = r; kingCol = c; break;
                }
            }
        }
        if (kingRow == -1) return false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                String piece = board[r][c];
                if (piece != null) {
                    boolean isPieceWhite = isWhitePiece(piece);
                    if (isPieceWhite != isWhite && isValidMoveBasic(piece, r, c, kingRow, kingCol, board, isPieceWhite)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isCheckmate(String[][] board, boolean isWhite) {
        if (!isKingInCheck(board, isWhite)) return false;

        for (int r1 = 0; r1 < 8; r1++) {
            for (int c1 = 0; c1 < 8; c1++) {
                String piece = board[r1][c1];
                if (piece != null && isWhitePiece(piece) == isWhite) {
                    for (int r2 = 0; r2 < 8; r2++) {
                        for (int c2 = 0; c2 < 8; c2++) {
                            String targetPiece = board[r2][c2];
                            if (targetPiece != null && isWhitePiece(targetPiece) == isWhite) continue;

                            if (isValidMoveBasic(piece, r1, c1, r2, c2, board, isWhite)) {
                                if (!movePutsKingInCheck(board, r1, c1, r2, c2, isWhite)) {
                                    return false; // Encontró un movimiento que lo salva
                                }
                            }
                        }
                    }
                }
            }
        }
        return true; // No hay movimientos salvadores
    }

    private boolean isWhitePiece(String piece) {
        return "♙♖♘♗♕♔".contains(piece);
    }
}