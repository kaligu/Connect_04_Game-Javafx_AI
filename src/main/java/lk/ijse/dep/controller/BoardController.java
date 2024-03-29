package lk.ijse.dep.controller;

import com.jfoenix.controls.JFXButton;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import lk.ijse.dep.service.AiPlayer; //(solved)-import service package AiPlayer Class
import lk.ijse.dep.service.Board;  //(solved)-import service package Board Class
import lk.ijse.dep.service.BoardImpl;  //(solved)-import service package BoardImpl Class
import lk.ijse.dep.service.BoardUI;  //(solved)-import service package BoardUI Class
import lk.ijse.dep.service.HumanPlayer;  //(solved)-import service package HumanPlayer Class
import lk.ijse.dep.service.Piece;  //(solved)-import service package Piece Class
import lk.ijse.dep.service.Player;  //(solved)-import service package Player Class
import lk.ijse.dep.service.Winner;  //(solved)-import service package Winner Class

public class BoardController implements BoardUI {  //(solved)- BoardUI class)
    private static final int RADIUS = 42;
    public Label lblStatus;
    public Group grpCols;
    public AnchorPane root;
    public Pane pneOver;
    public JFXButton btnPlayAgain;

    private String playerName;
    private boolean isAiPlaying;
    private boolean isGameOver;

    private Player humanPlayer;  //(solved)
    private Player aiPlayer;    //(solved)

    private void initializeGame() { //2nd method also creates objects
        Board newBoard = new BoardImpl(this); //(solved)
        humanPlayer = new HumanPlayer(newBoard); //(solved)
        aiPlayer = new AiPlayer(newBoard); //(solved)
    }

    public void initialize() {  //1st  same as constructor
        initializeGame();  //2nd
        grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> vbox.setOnMouseClicked(mouseEvent -> colOnClick(vbox))); //3rd mouse is which colum clickd pass vbox id
    }

    private void colOnClick(VBox col) {  //3rd
        if (!isAiPlaying && !isGameOver) humanPlayer.movePiece(grpCols.getChildren().indexOf(col)); //(solved ) -movePiece()) 4th pass coloumn index
    }

    public void initData(String playerName) {   //entered player name set playerName attribute  1st before run
        this.playerName = playerName;  //this means current object player name assign pre entered name
    }

    @Override  //(solved)- method override error)
    public void update(int col, boolean isHuman) {
        if (isGameOver) return;
        VBox vCol = (VBox) grpCols.lookup("#col" + col);
        if (vCol.getChildren().size() == 5)
            throw new RuntimeException("Double check your logic, no space available within the column: " + col);
        if (!isHuman) {
            vCol.getStyleClass().add("col-ai");
        }
        Circle circle = new Circle(RADIUS);
        circle.getStyleClass().add(isHuman ? "circle-human" : "circle-ai");
        vCol.getChildren().add(0, circle);
        if (vCol.getChildren().size() == 5) vCol.getStyleClass().add("col-filled");
        TranslateTransition tt = new TranslateTransition(Duration.millis(250), circle);
        tt.setFromY(-50);
        tt.setToY(circle.getLayoutY());
        tt.playFromStart();
        lblStatus.getStyleClass().clear();
        lblStatus.getStyleClass().add(isHuman ? "ai" : "human");
        if (isHuman) {
            isAiPlaying = true;
            grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> vbox.getStyleClass().remove("col-human"));
            KeyFrame delayFrame = new KeyFrame(Duration.millis(300), actionEvent -> {
                if (!isGameOver) lblStatus.setText("Wait, AI is playing");
            });
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), actionEvent -> {
                if (!isGameOver) aiPlayer.movePiece(-1); //(solved)-movePiece())
            });
            new Timeline(delayFrame, keyFrame).playFromStart();
        } else {
            KeyFrame delayFrame = new KeyFrame(Duration.millis(300), actionEvent -> {
                grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> {
                    vbox.getStyleClass().remove("col-ai");
                    vbox.getStyleClass().add("col-human");
                });
            });
            new Timeline(delayFrame).playFromStart();
            isAiPlaying = false;
            lblStatus.setText(playerName + ", it is your turn now!");
        }
    }

    @Override  //(solved-overriding error)
    public void notifyWinner(Winner winner) { //(solved- Winner class)
        isGameOver = true;
        lblStatus.getStyleClass().clear();
        lblStatus.getStyleClass().add("final");
        switch (winner.getWinningPiece()) { //(solved - method)
            case BLUE:  //(solved)
                lblStatus.setText(String.format("%s, you have won the game !", playerName));
                break;
            case GREEN: //(solved)
                lblStatus.setText("Game is over, AI has won the game !");
                break;
            case EMPTY:  //(solved)
                lblStatus.setText("Game is tied !");
        }
        if (winner.getWinningPiece() != Piece.EMPTY) { //(solved)-getWinningPiece() != Piece.EMPTY)
            VBox vCol = (VBox) grpCols.lookup("#col" + winner.getCol1()); //(solved)-getCol1())
            Rectangle rect = new Rectangle((winner.getCol2() - winner.getCol1() + 1) * vCol.getWidth(), //(solved)-getCol1() , getCol2() )
                    (winner.getRow2() - winner.getRow1() + 1) * (((RADIUS + 2) * 2))); //(solved)-getRow1() , getRow2() )
            rect.setId("rectOverlay");
            root.getChildren().add(rect);
            rect.setLayoutX(vCol.localToScene(0, 0).getX());
            rect.setLayoutY(vCol.localToScene(0, 0).getY() + (4 - winner.getRow2()) * ((RADIUS + 2) * 2)); //(solved)-getRow2() )
            rect.getStyleClass().add("winning-rect");
        }
        pneOver.setVisible(true);
        pneOver.toFront();
        Platform.runLater(btnPlayAgain::requestFocus);
    }

    public void btnPlayAgainOnAction(ActionEvent actionEvent) {
        initializeGame();
        isAiPlaying = false;
        isGameOver = false;
        pneOver.setVisible(false);
        lblStatus.getStyleClass().clear();
        lblStatus.setText("LET'S PLAY !");
        grpCols.getChildren().stream().map(n -> (VBox) n).forEach(vbox -> {
            vbox.getChildren().clear();
            vbox.getStyleClass().remove("col-ai");
            vbox.getStyleClass().remove("col-filled");
            vbox.getStyleClass().add("col-human");
        });
        root.getChildren().remove(root.lookup("#rectOverlay"));
    }
}
