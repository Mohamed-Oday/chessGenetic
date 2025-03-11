package org.openjfx.chessgenetic;

import java.util.*;


/**
 *
 * This class represents a chess board (matrix, vector, fitness, selectionFitness, conflicts, conflictList)
 * the mutation is performed here because it is a method that changes the board
 * @version 1.0
 * @since 2025-03-11
 * @see java.util
 * @author Sellami Mohamed Oday
 */
public class ChessMatrix{
    private static final int BOARD_SIZE = 8;
    private static final double MUTATION_PROBABILITY = 0.1;

    private char[][] board;
    private Vector<Character> boardVictor;
    private double fitness;
    private double selectionFitness;
    private int conflicts;
    private List<String> conflictList;

    public ChessMatrix() {
        board = new char[BOARD_SIZE][BOARD_SIZE];
        conflictList = new ArrayList<>();
        boardVictor = new Vector<Character>();
        initializeRandomBoard();
        calculateConflicts();
        calculateFitness();
    }

    /**
     *  initializeRandomBoard() method initializes the board with random pieces
     *  and sets the vector of the board
     */
    private void initializeRandomBoard() {

        // 1- Initialize the board with empty cells
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = 'e';
            }
        }

        // 2- Create two lists of pieces (upper and lower)
        List<Character> upperPieces = new ArrayList<>(List.of('Q', 'Q', 'R', 'K', 'B'));
        List<Character> lowerPieces = new ArrayList<>(List.of('q', 'q', 'r', 'k', 'b'));

        // 3- Shuffle the pieces
        Collections.shuffle(upperPieces);
        Collections.shuffle(lowerPieces);

        // 4- Place upper pieces in the top half (rows 0-3)
        int index = 0;
        while (index < upperPieces.size()) {
            int row = (int) (Math.random() * 4);
            int col = (int) (Math.random() * BOARD_SIZE);

            if (board[row][col] == 'e') {
                board[row][col] = upperPieces.get(index);
                index++;
            }
        }

        // 5- Place lower pieces in the bottom half (rows 4-7)
        index = 0;
        while (index < lowerPieces.size()) {
            int row = (int) (Math.random() * 4) + 4;
            int col = (int) (Math.random() * BOARD_SIZE);

            if (board[row][col] == 'e') {
                board[row][col] = lowerPieces.get(index);
                index++;
            }
        }

        // 6- Set the vector of the board
        setBoardVictor(boardToVictor());
    }

    /**
     *  calculateConflicts() method calculates the number of conflicts in the board
     *  and get the conflict attributes
     */
    private void calculateConflicts() {
        conflicts = 0;
        for (int i = 0 ; i < BOARD_SIZE ; i++) {
            for (int j = 0 ; j < BOARD_SIZE ; j++) {
                if (board[i][j] != 'e') {
                    conflicts += checkPieceConflicts(i, j);
                }
            }
        }
    }

    /**
     * checkPieceConflicts() method checks the conflicts of a piece in the board
     * @param row
     * @param col
     * @return the sum piece conflicts
     */
    private int checkPieceConflicts(int row, int col) {
        int pieceConflicts = 0;
        char piece = board[row][col];

        switch (Character.toUpperCase(piece)) {
            case 'Q': pieceConflicts += checkQueenConflicts(row, col); break;
            case 'R': pieceConflicts += checkRookConflicts(row, col); break;
            case 'B': pieceConflicts += checkBishopConflicts(row, col); break;
            case 'K': pieceConflicts += checkKnightConflicts(row, col); break;
        }

        return pieceConflicts + calculateAllPenalties();
    }

    /**
     * queenPenalty() method calculates the number of queens in the same column beyond the first one
     *
     * @return the penalty
     */
    private int queenPenalty() {
        int penalty = 0;
        // Create an array to count queens in each column
        int[] columnCounts = new int[8];

        // Count the queens in each column
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (board[row][col] == 'Q' || board[row][col] == 'q') {
                    columnCounts[col]++;
                }
            }
        }

        // Calculate penalty for each column with multiple queens
        for (int col = 0; col < 8; col++) {
            if (columnCounts[col] > 1) {
                penalty += (columnCounts[col] - 1);
            }
        }

        return penalty;
    }

    /**
     * checkQueenConflicts() method checks the conflicts of a queen
     * @param row
     * @param col
     * @return the queen conflicts
     */
    private int checkQueenConflicts(int row, int col) {
        int conflicts = 0;

        conflicts += checkDiagonalConflicts(row, col);
        conflicts += checkStraightConflicts(row, col);

        return conflicts;
    }

    /**
     * checkRookConflicts() method checks the conflicts of a rook
     * @param row
     * @param col
     * @return the rook conflicts
     */
    private int checkRookConflicts(int row, int col) {
        return checkStraightConflicts(row, col);
    }

    /**
     * checkBishopConflicts() method checks the conflicts of a bishop
     * @param row
     * @param col
     * @return the bishop conflicts
     */
    private int checkBishopConflicts(int row, int col) {
        return checkDiagonalConflicts(row, col);
    }

    /**
     * checkKnightConflicts() method checks the conflicts of a knight
     * @param row
     * @param col
     * @return the knight conflicts
     */
    private int checkKnightConflicts(int row, int col) {
        int conflicts = 0;

        // Define the moves of a knight (L shape)
        int[][] knightMoves = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : knightMoves) {
            int newRow = row + move[0];
            int newCol = col + move[1];

            if (isValidPosition(newRow, newCol) && board[newRow][newCol] != 'e'){
                /*if (Character.isUpperCase(board[row][col]) != Character.isUpperCase(board[newRow][newCol])) {
                    conflicts++;
                }*/
                conflicts++;
                conflictList.add(String.format("Knight (%d,%d) -> (%d,%d)", row, col, newRow, newCol)); // Debug statement
            }
        }
        return conflicts;
    }

    /**
     * checkDiagonalConflicts() method checks the conflicts of a piece in the diagonal line
     * @param row
     * @param col
     * @return the diagonal conflicts
     */
    private int checkDiagonalConflicts(int row, int col) {
        int conflicts = 0;
        //char currentPiece = board[row][col];

        // Define the directions of the diagonal line
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            while (isValidPosition(newRow, newCol)) {
                if (board[newRow][newCol] != 'e') {
                    /*if (Character.isUpperCase(currentPiece) != Character.isUpperCase(board[newRow][newCol])) {
                        conflicts++;
                    }*/
                    conflicts++;
                    conflictList.add(String.format("Diagonal (%d,%d) -> (%d,%d)", row, col, newRow, newCol)); // Debug statement
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }
        return conflicts;
    }

    /**
     * checkStraightConflicts() method checks the conflicts of a piece in the straight line
     * @param row
     * @param col
     * @return the straight conflicts
     */
    private int checkStraightConflicts(int row, int col) {
        int conflicts = 0;
        //char currentPiece = board[row][col];

        // Define the directions of the straight line
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] direction : directions) {
            int newRow = row + direction[0];
            int newCol = col + direction[1];

            while (isValidPosition(newRow, newCol)) {
                if (board[newRow][newCol] != 'e') {
                    /*if (Character.isUpperCase(currentPiece) != Character.isUpperCase(board[newRow][newCol])) {
                        conflicts++;
                    }*/
                    conflicts++;
                    conflictList.add(String.format("Straight (%d,%d) -> (%d,%d)", row, col, newRow, newCol)); // Debug statement
                    break;
                }
                newRow += direction[0];
                newCol += direction[1];
            }
        }
        return conflicts;
    }

    /**
     * isValidPosition() method checks if the position is valid (not outside the chess board)
     * @param row
     * @param col
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * mutate() method it's a method that swaps between 2 random cells on the board
     * and recalculate the conflicts and fitness
     * Steps:
     * 1- generate a random number
     * 2- Choose which half to mutate
     * 3- choose which cell to swap
     * 4- Swap the positions
     * 5- Convert vector back to 2D board
     * 6- Recalculate conflicts and fitness after mutation
     */
    public void mutate() {

        // 1- generate a random number
        if (Math.random() < MUTATION_PROBABILITY) {

            // 2- Choose which half to mutate
            if (Math.random() < 0.5) {

                // 3- choose which cell to swap
                int index1, index2;
                int attempts = 0;

                do {
                    index1 = (int) (Math.random() * 32);
                    index2 = (int) (Math.random() * 32);

                    attempts++;

                    // Prevent infinite loop if no valid positions
                    if (attempts > 100) break;
                } while (index1 == index2 || (boardVictor.get(index1) == 'e' && boardVictor.get(index2) == 'e'));

                // 4- Swap the positions
                char temp = boardVictor.get(index1);
                boardVictor.set(index1, boardVictor.get(index2));
                boardVictor.set(index2, temp);

            } else {
                // Same thing like the upper with the lower half
                int index1, index2;
                int attempts = 0;

                do {
                    index1 = 32 + (int) (Math.random() * 32);
                    index2 = 32 + (int) (Math.random() * 32);

                    attempts++;

                    if (attempts > 100) break;
                } while (index1 == index2 && (boardVictor.get(index1) == 'e' && boardVictor.get(index2) == 'e'));

                char temp = boardVictor.get(index1);
                boardVictor.set(index1, boardVictor.get(index2));
                boardVictor.set(index2, temp);
            }

            // 5- Convert vector back to 2D board
            victorToBoard();

            // 6- Recalculate conflicts and fitness after mutation
            conflictList.clear();
            calculateConflicts();
            calculateFitness();
        }
    }

    /**
     *  calculateFitness() method calculates the fitness of the board
     */
    private void calculateFitness() {
        fitness = 1.0 / (1.0 + conflicts);
    }

    /**
     * getters and setters
     */

    public double getFitness() {
        return fitness;
    }

    public int getConflicts() {
        return conflicts;
    }

    public char[][] getBoard() {
        return board;
    }

    public void setSelectionFitness(double selectionFitness) {
        this.selectionFitness = selectionFitness;
    }

    public double getSelectionFitness() {
        return selectionFitness;
    }

    public Vector<Character> getBoardVictor() {
        return boardVictor;
    }

    public void setBoardVictor(Vector<Character> boardVictor) {
        this.boardVictor = boardVictor;
    }

    public Vector<Character> boardToVictor() {
        boardVictor.clear();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                boardVictor.add(board[i][j]);
            }
        }
        return boardVictor;
    }

    public void victorToBoard() {
        int index = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = boardVictor.get(index);
                index++;
            }
        }
    }

    /**
     * Calculates the penalty for queens in the same row.
     * @return Number of penalties (conflicts) for queens in the same row
     */
    private int calculateQueenRowPenalty() {
        int penalty = 0;

        // Create an array to count queens in each row
        int[] rowCounts = new int[8]; // Assuming 8x8 chessboard

        // Count the queens in each row
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == 'Q' || board[row][col] == 'q') {
                    rowCounts[row]++;
                }
            }
        }

        // Calculate penalty for each row with multiple queens
        for (int row = 0; row < 8; row++) {
            if (rowCounts[row] > 1) {
                // Add penalty of 1 for each queen in the same row beyond the first one
                penalty += (rowCounts[row] - 1);
            }
        }

        return penalty;
    }

    /**
     * Calculates the penalty for queens in the same column.
     * @return Number of penalties (conflicts) for queens in the same column
     */
    private int calculateQueenColumnPenalty() {
        int penalty = 0;

        // Create an array to count queens in each column
        int[] columnCounts = new int[8]; // Assuming 8x8 chessboard

        // Count the queens in each column
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (board[row][col] == 'Q' || board[row][col] == 'q') {
                    columnCounts[col]++;
                }
            }
        }

        // Calculate penalty for each column with multiple queens
        for (int col = 0; col < 8; col++) {
            if (columnCounts[col] > 1) {
                // Add penalty of 1 for each queen in the same column beyond the first one
                penalty += (columnCounts[col] - 1);
            }
        }

        return penalty;
    }

    /**
     * Calculates the penalty for rooks in the same row.
     * @return Number of penalties (conflicts) for rooks in the same row
     */
    private int calculateRookRowPenalty() {
        int penalty = 0;

        // Create an array to count rooks in each row
        int[] rowCounts = new int[8]; // Assuming 8x8 chessboard

        // Count the rooks in each row
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == 'R' || board[row][col] == 'r') {
                    rowCounts[row]++;
                }
            }
        }

        // Calculate penalty for each row with multiple rooks
        for (int row = 0; row < 8; row++) {
            if (rowCounts[row] > 1) {
                // Add penalty of 1 for each rook in the same row beyond the first one
                penalty += (rowCounts[row] - 1);
            }
        }

        return penalty;
    }

    /**
     * Calculates the penalty for rooks in the same column.
     * @return Number of penalties (conflicts) for rooks in the same column
     */
    private int calculateRookColumnPenalty() {
        int penalty = 0;

        // Create an array to count rooks in each column
        int[] columnCounts = new int[8]; // Assuming 8x8 chessboard

        // Count the rooks in each column
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                if (board[row][col] == 'R' || board[row][col] == 'r') {
                    columnCounts[col]++;
                }
            }
        }

        // Calculate penalty for each column with multiple rooks
        for (int col = 0; col < 8; col++) {
            if (columnCounts[col] > 1) {
                // Add penalty of 1 for each rook in the same column beyond the first one
                penalty += (columnCounts[col] - 1);
            }
        }

        return penalty;
    }

    /**
     * Calculates all penalties for queens and rooks.
     * This should be called during fitness calculation.
     * @return Total number of penalties (conflicts)
     */
    private int calculateAllPenalties() {
        int totalPenalties = 0;

        // Calculate all penalties
        totalPenalties += calculateQueenRowPenalty();
        totalPenalties += calculateQueenColumnPenalty();
        totalPenalties += calculateRookRowPenalty();
        totalPenalties += calculateRookColumnPenalty();

        // Add diagonal penalties for queens if needed
        // totalPenalties += calculateQueenDiagonalPenalties();

        return totalPenalties;
    }
}
