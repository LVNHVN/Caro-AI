package resources;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import model.BoardModel;
import javafx.scene.layout.StackPane;

public class BoardView extends BorderPane {
    private StackPane[][] cellPanes;
    private Spinner<Integer> depthSpinner;
    private Label statusLabel;
    private final int CELL_SIZE = 40;

    public BoardView() {
        cellPanes = new StackPane[BoardModel.SIZE][BoardModel.SIZE];
        initRightPanel(); 
        initBoard();
    }

    private void initRightPanel() {
        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(20));
        rightPanel.setAlignment(Pos.TOP_CENTER);
        rightPanel.setPrefWidth(180);
        rightPanel.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #D3D3D3; -fx-border-width: 0 0 0 1;");

        statusLabel = new Label("Your Turn\n(Player X)");
        statusLabel.setStyle("-font-weight: bold; -fx-font-size: 14px; -fx-text-alignment: center;");

        Label depthLabel = new Label("AI Depth Limit:");
        
        depthSpinner = new Spinner<>();
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 6, 2);
        depthSpinner.setValueFactory(valueFactory);
        depthSpinner.setEditable(true);
        depthSpinner.setPrefWidth(80);

        rightPanel.getChildren().addAll(statusLabel, depthLabel, depthSpinner);
        this.setRight(rightPanel);
    }

    private void initBoard() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: #FFFFFF;"); 

        for (int r = 0; r < BoardModel.SIZE; r++) {
            for (int c = 0; c < BoardModel.SIZE; c++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                
                cell.setStyle("-fx-border-color: #A9A9A9; -fx-border-width: 0.5; -fx-background-color: #FAFAFA;");

                cellPanes[r][c] = cell;
                grid.add(cell, c, r);
            }
        }
        this.setCenter(grid);
    }

    public void updateCell(int row, int col, int player) {
        Text symbol = new Text();
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        if (player == BoardModel.PLAYER) {
            symbol.setText("X");
            symbol.setFill(Color.web("#2A6496"));
        } else if (player == BoardModel.AI) {
            symbol.setText("O");
            symbol.setFill(Color.web("#D9534F"));
        }
        
        cellPanes[row][col].getChildren().add(symbol);
    }

    public StackPane getCellPane(int row, int col) { return cellPanes[row][col]; }
    public int getSelectedDepth() { return depthSpinner.getValue(); }
    public void setStatus(String text) { statusLabel.setText(text); }
}