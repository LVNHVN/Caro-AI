package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BoardModel {
    public static final int SIZE = 15;
    public static final int EMPTY = 0;
    public static final int AI = 1;
    public static final int PLAYER = 2;

    private int[][] board;
    private int currentTurn;
    private boolean gameOver;
    private int currentBoardScore;

    // Optimized Candidate Set Tracking
    private HashSet<Integer> candidateSet;

    public BoardModel() {
        reset();
    }

    public void reset() {
        board = new int[SIZE][SIZE];
        currentTurn = PLAYER;
        gameOver = false;
        currentBoardScore = 0;
        candidateSet = new HashSet<>(SIZE * SIZE);
    }

    public boolean makeMove(int row, int col, int player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || board[row][col] != EMPTY || gameOver) {
            return false;
        }
        
        currentBoardScore = evaluateIncrementalMove(row, col, player);
        board[row][col] = player;
        
        // Permanently commit the candidate set changes for an actual game move
        addMoveToSet(row, col);
        
        return true;
    }

    public int[][] getBoard() { return board; }
    public int getCurrentTurn() { return currentTurn; }
    public void switchTurn() { currentTurn = (currentTurn == PLAYER) ? AI : PLAYER; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }

    public boolean checkWin(int row, int col, int player) {
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        for (int[] dir : dirs) {
            int count = 1;
            count += countDirection(row, col, dir[0], dir[1], player);
            count += countDirection(row, col, -dir[0], -dir[1], player);
            if (count >= 5) return true;
        }
        return false;
    }

    private int countDirection(int r, int c, int dr, int dc, int player) {
        int count = 0;
        int nr = r + dr;
        int nc = c + dc;
        while (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE && board[nr][nc] == player) {
            count++;
            nr += dr;
            nc += dc;
        }
        return count;
    }

    // --- CANDIDATE SET (DELTA TRACKING) ---

    private int getId(int r, int c) {
        return r * SIZE + c;
    }

    /**
     * Removes the target cell from candidates, and adds all surrounding empty cells
     * within a 2-cell radius. Returns the list of strictly newly added cell IDs.
     */
    private List<Integer> addMoveToSet(int r, int c) {
        List<Integer> addedCells = new ArrayList<>();
        int targetId = getId(r, c);
        
        // The cell is now occupied, remove it from candidates
        candidateSet.remove(targetId);

        for (int dr = -2; dr <= 2; dr++) {
            for (int dc = -2; dc <= 2; dc++) {
                if (dr == 0 && dc == 0) continue; // Skip the center piece

                int nr = r + dr;
                int nc = c + dc;

                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE) {
                    if (board[nr][nc] == EMPTY) {
                        int nid = getId(nr, nc);
                        if (candidateSet.add(nid)) {
                            addedCells.add(nid); // Track delta
                        }
                    }
                }
            }
        }
        return addedCells;
    }

    /**
     * Restores the target cell to candidates and removes the delta cells.
     */
    private void removeMoveFromSet(int r, int c, List<Integer> delta) {
        int targetId = getId(r, c);
        candidateSet.add(targetId); // Re-add the now empty cell
        
        for (Integer nid : delta) {
            candidateSet.remove(nid); // Remove only what was dynamically added
        }
    }

    // --- INCREMENTAL EVALUATION ENGINE ---

    private int evaluateIncrementalMove(int row, int col, int player) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        int oldLinesScore = 0;
        int newLinesScore = 0;

        for (int[] dir : directions) {
            oldLinesScore += evaluateLineOnAxis(row, col, dir[0], dir[1]);
        }

        board[row][col] = player;

        for (int[] dir : directions) {
            newLinesScore += evaluateLineOnAxis(row, col, dir[0], dir[1]);
        }

        board[row][col] = EMPTY;

        return currentBoardScore - oldLinesScore + newLinesScore;
    }

    private int evaluateLineOnAxis(int row, int col, int dr, int dc) {
        int axisScore = 0;

        for (int i = -4; i <= 0; i++) {
            int startR = row + i * dr;
            int startC = col + i * dc;
            int endR = startR + 4 * dr;
            int endC = startC + 4 * dc;

            if (startR >= 0 && startR < SIZE && startC >= 0 && startC < SIZE &&
                endR >= 0 && endR < SIZE && endC >= 0 && endC < SIZE) {
                axisScore += evaluateWindow(startR, startC, dr, dc);
            }
        }
        return axisScore;
    }

    private int evaluateWindow(int startR, int startC, int dr, int dc) {
        StringBuilder sb = new StringBuilder(5);
        
        for (int i = 0; i < 5; i++) {
            int cell = board[startR + i * dr][startC + i * dc];
            if (cell == AI) {
                sb.append('O');
            } else if (cell == PLAYER) {
                sb.append('X');
            } else {
                sb.append('_');
            }
        }
        
        String pattern = sb.toString();
        
        int score = 0;
        score += getPrecisePatternScore(pattern, true);  
        score -= getPrecisePatternScore(pattern, false); 
        
        return score;
    }

    // --- THE BRAIN: STRING PATTERN DICTIONARY ---

    private int getPrecisePatternScore(String pattern, boolean isAI) {
        char token = isAI ? 'O' : 'X';
        char enemy = isAI ? 'X' : 'O';
        
        // Mixed pieces kill the line's potential completely
        if (pattern.indexOf(enemy) != -1) {
            return 0;
        }
        
        String genericPattern = pattern.replace(token, 'M');
        
        // Priority: Survival > Aggression. 
        // Player configurations subtract MORE points than AI configurations add.
        switch (genericPattern) {
            case "MMMMM": 
                return 100000;
                
            case "_MMMM": case "M_MMM": case "MM_MM": case "MMM_M": case "MMMM_":
                return isAI ? 12000 : 10000; 
                
            case "_MMM_":
                return isAI ? 7000 : 6000;
                
            case "M_MM_": case "_MM_M": case "MM_M_": case "_M_MM": case "M_M_M": 
                return isAI ? 4000 : 3000; 
                
            case "MM___": case "_MM__": case "__MM_": case "___MM": case "M_M__":
            case "_M_M_": case "__M_M": case "M__M_": case "_M__M": case "M___M":
                return isAI ? 100 : 80;
                
            case "M____": case "_M___": case "__M__": case "___M_": case "____M":
                return 10;
                
            default:
                return 0;
        }
    }

    // --- MINIMAX & TACTICAL MOVE ORDERING ---

    public int[] getBestMove(int maxDepth) {
        if (candidateSet.isEmpty()) {
            return new int[]{SIZE / 2, SIZE / 2}; 
        }

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        List<int[]> candidates = getOrderedCandidates();

        for (int[] move : candidates) {
            int r = move[0];
            int c = move[1];

            int previousScoreState = currentBoardScore;
            currentBoardScore = evaluateIncrementalMove(r, c, AI);
            board[r][c] = AI;
            List<Integer> deltaTracker = addMoveToSet(r, c);

            int score = minimax(maxDepth - 1, alpha, beta, false);

            removeMoveFromSet(r, c, deltaTracker);
            board[r][c] = EMPTY;
            currentBoardScore = previousScoreState;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMove;
    }

    private int minimax(int depth, int alpha, int beta, boolean isMax) {
        // The Zombie Game Fix: Stop simulation instantly on lethal states.
        // Adjust by depth to prefer faster wins and slower losses.
        if (currentBoardScore >= 90000) {
            return currentBoardScore + depth;
        }
        if (currentBoardScore <= -90000) {
            return currentBoardScore - depth;
        }
        
        if (depth == 0) {
            return currentBoardScore; 
        }

        List<int[]> candidates = getOrderedCandidates();
        if (candidates.isEmpty()) return currentBoardScore;

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : candidates) {
                int r = move[0];
                int c = move[1];

                int previousScoreState = currentBoardScore;
                currentBoardScore = evaluateIncrementalMove(r, c, AI);
                board[r][c] = AI;
                List<Integer> deltaTracker = addMoveToSet(r, c);

                int eval = minimax(depth - 1, alpha, beta, false);

                removeMoveFromSet(r, c, deltaTracker);
                board[r][c] = EMPTY;
                currentBoardScore = previousScoreState;

                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] move : candidates) {
                int r = move[0];
                int c = move[1];

                int previousScoreState = currentBoardScore;
                currentBoardScore = evaluateIncrementalMove(r, c, PLAYER);
                board[r][c] = PLAYER;
                List<Integer> deltaTracker = addMoveToSet(r, c);

                int eval = minimax(depth - 1, alpha, beta, true);

                removeMoveFromSet(r, c, deltaTracker);
                board[r][c] = EMPTY;
                currentBoardScore = previousScoreState;

                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private List<int[]> getOrderedCandidates() {
        List<TacticalMove> scoredCandidates = new ArrayList<>(candidateSet.size());
        int baseScore = currentBoardScore;

        for (int id : candidateSet) {
            int r = id / SIZE;
            int c = id % SIZE;

            int aiDelta = Math.abs(evaluateIncrementalMove(r, c, AI) - baseScore);
            int playerDelta = Math.abs(evaluateIncrementalMove(r, c, PLAYER) - baseScore);
            
            // Tactical weight is purely heuristic, evaluating urgency
            int tacticalWeight = aiDelta + playerDelta;
            scoredCandidates.add(new TacticalMove(r, c, tacticalWeight));
        }

        scoredCandidates.sort((a, b) -> Integer.compare(b.weight, a.weight));

        List<int[]> orderedMoves = new ArrayList<>(scoredCandidates.size());
        for (TacticalMove tm : scoredCandidates) {
            orderedMoves.add(new int[]{tm.r, tm.c});
        }
        return orderedMoves;
    }

    private static class TacticalMove {
        int r, c, weight;
        TacticalMove(int r, int c, int weight) {
            this.r = r;
            this.c = c;
            this.weight = weight;
        }
    }
}