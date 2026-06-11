package controller;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import model.BoardModel;
import resources.BoardView;

public class BoardController {
    private BoardModel model;
    private BoardView view;

    public BoardController(BoardModel model, BoardView view) {
        this.model = model;
        this.view = view;
        setupHandlers();
    }

    private void setupHandlers() {
        for (int r = 0; r < model.SIZE; r++) {
            final int row = r;
            for (int c = 0; c < model.SIZE; c++) {
                final int col = c;
                StackPane cell = view.getCellPane(row, col);
                cell.setOnMouseClicked(e -> handleHumanMove(row, col));
            }
        }
    }

    private void handleHumanMove(int row, int col) {
        if (model.isGameOver() || model.getCurrentTurn() != BoardModel.PLAYER) return;

        if (model.makeMove(row, col, BoardModel.PLAYER)) {
            view.updateCell(row, col, BoardModel.PLAYER);

            if (model.checkWin(row, col, BoardModel.PLAYER)) {
                view.setStatus("Player Wins!");
                model.setGameOver(true);
                return;
            }

            model.switchTurn();
            view.setStatus("AI is thinking...");
            triggerAIMove();
        }
    }

    private void triggerAIMove() {
        int depth = view.getSelectedDepth();

        new Thread(() -> {
            int[] aiMove = model.getBestMove(depth);
            
            Platform.runLater(() -> {
                if (model.makeMove(aiMove[0], aiMove[1], BoardModel.AI)) {
                    view.updateCell(aiMove[0], aiMove[1], BoardModel.AI);

                    if (model.checkWin(aiMove[0], aiMove[1], BoardModel.AI)) {
                        view.setStatus("AI Wins!");
                        model.setGameOver(true);
                        return;
                    }

                    model.switchTurn();
                    view.setStatus("Your Turn (Black)");
                }
            });
        }).start();
    }
}