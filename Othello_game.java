import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Othello {
    private int turn;
    private int[][] board;
    private int winner;

    public Othello() {
        turn = -1;
        board = new int[8][8];
        winner = -1;
    }

    public void loadGame(String filename) {
        try {
            Scanner scanner = new Scanner(new File(filename));

            // Read turn
            if (scanner.hasNextInt()) {
                turn = scanner.nextInt();
            }

            // Read board
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (scanner.hasNextInt()) {
                        board[i][j] = scanner.nextInt();
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int boardScore() {
        int blackPieces = 0;
        int whitePieces = 0;

        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) {
                    blackPieces++;
                } else if (cell == 1) {
                    whitePieces++;
                }
            }
        }

        return (turn == 0) ? (blackPieces - whitePieces) : (whitePieces - blackPieces);
    }

    public int bestMove(int k) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == -1 && isValidMove(i, j)) {
                    int score = minimax(i, j, k, Integer.MIN_VALUE, Integer.MAX_VALUE, turn, true);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = i * 8 + j;
                    }
                }
            }
        }

        return bestMove;
    }

    private boolean isValidMove(int row, int col) {
        // Check if the cell is empty
        if (board[row][col] != -1) {
            return false;
        }

        // Check if there is a valid direction to flip opponent's pieces
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;  // Skip the current cell
                }
                if (isValidDirection(row, col, dRow, dCol)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isValidDirection(int row, int col, int dRow, int dCol) {
        int opponent = (turn == 0) ? 1 : 0;
        int newRow = row + dRow;
        int newCol = col + dCol;
        boolean foundOpponent = false;

        while (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8) {
            if (board[newRow][newCol] == opponent) {
                foundOpponent = true;
            } else if (board[newRow][newCol] == turn && foundOpponent) {
                return true;
            } else {
                return false;
            }

            newRow += dRow;
            newCol += dCol;
        }

        return false;
    }

    private int minimax(int row, int col, int depth, int alpha, int beta, int currentPlayer, boolean maximizingPlayer) {
        int[][] tempBoard = copyBoard(board);
        tempBoard[row][col] = currentPlayer;
        flipOpponentPieces(row, col, currentPlayer);

        if (depth == 0 || isGameOver()) {
            return boardScore();
        }

        if (maximizingPlayer) {
            int maxScore = Integer.MIN_VALUE;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (tempBoard[i][j] == -1 && isValidMove(i, j)) {
                        int score = minimax(i, j, depth - 1, alpha, beta, (currentPlayer == 0) ? 1 : 0, false);
                        maxScore = Math.max(maxScore, score);
                        alpha = Math.max(alpha, score);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (tempBoard[i][j] == -1 && isValidMove(i, j)) {
                        int score = minimax(i, j, depth - 1, alpha, beta, (currentPlayer == 0) ? 1 : 0, true);
                        minScore = Math.min(minScore, score);
                        beta = Math.min(beta, score);
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return minScore;
        }
    }

    private void flipOpponentPieces(int row, int col, int currentPlayer) {
        int opponent = (currentPlayer == 0) ? 1 : 0;

        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) {
                    continue;  // Skip the current cell
                }
                if (isValidDirection(row, col, dRow, dCol)) {
                    int newRow = row + dRow;
                    int newCol = col + dCol;
                    while (board[newRow][newCol] == opponent) {
                        board[newRow][newCol] = currentPlayer;
                        newRow += dRow;
                        newCol += dCol;
                    }
                }
            }
        }
    }

    private boolean isGameOver() {
        return (bestMove(1) == -1);
    }

    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 8);
        }
        return copy;
    }

    public ArrayList<Integer> fullGame(int k) {
        ArrayList<Integer> moves = new ArrayList<>();
        moves.add(turn);

        while (!isGameOver()) {
            int bestMove = bestMove(k);
            int row = bestMove / 8;
            int col = bestMove % 8;

            board[row][col] = turn;
            flipOpponentPieces(row, col, turn);

            turn = (turn == 0) ? 1 : 0;
            moves.add(bestMove);
        }

        updateWinner();
        return moves;
    }

    private void updateWinner() {
        int blackPieces = 0;
        int whitePieces = 0;

        for (int[] row : board) {
            for (int cell : row) {
                if (cell == 0) {
                    blackPieces++;
                } else if (cell == 1) {
                    whitePieces++;
                }
            }
        }

        if (blackPieces > whitePieces) {
            winner = 0;
        } else if (whitePieces > blackPieces) {
            winner = 1;
        } else {
            winner = -1;
        }
    }
}
