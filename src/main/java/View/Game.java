package View;

import Controller.CustomizeMapController;
import Controller.GameMenuController;
import Model.*;
import Model.Buildings.Building;
import Model.Units.Unit;
import View.Controller.GetCoordinate;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class Game extends Application{
    private static final HashMap<Land, Image> tiles;
    private static final HashMap<String, Image> units;
    private static final HashMap<String, Image> buildings;
    private static final HashMap<Trees, Image> trees;

    private static final double screenWidth;
    public static final double screenHeight;
    private static int blockPixel;
    public static final int leftX;
    private static int blockWidth;
    private static int blockHeight;
    private static final Rectangle blackRec;
    private static final Rectangle selectSq;

    public static Pane mainPane; // this pane contains all other panes such as pane
    private static Stage stage;
    private static Pane pane;
    public static AnchorPane bottomPane;
    public static AnchorPane customizePane;
    private static Pane squareInfo;

    public static Trees tree;
    public static Land land;
    private static AnchorPane buildingDetail;
    private static int selectedX;
    private static int selectedY;

    private Scene scene;
    private final Map map;
    private Square[][] squares;
    private int squareI;
    private int squareJ;
    private int blockX;
    private int blockY;
    private boolean moveMode;
    private Building building;
    private Timeline hoverTimeline;
    private double mouseX;
    private double mouseY;


    static {
        tiles = new HashMap<>();
        units = new HashMap<>();
        buildings = new HashMap<>();
        trees = new HashMap<>();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        screenWidth = 1115;
        screenHeight = Math.ceil(screenBounds.getHeight()) - 40;
        leftX = (int) (Math.floor((screenBounds.getWidth() - screenWidth) / 2));

        blackRec = new Rectangle(0, 0, Color.BLACK);
        blackRec.setWidth(screenBounds.getWidth());
        blackRec.setHeight(screenBounds.getHeight() + 50);

        selectSq = new Rectangle();
        selectSq.setFill(null);
        selectSq.setStroke(Color.BLUE);
    }

    public Game() {
        this.map = DataBase.getSelectedMap();
        squares = map.getSquares();
        squareI = 0;
        squareJ = 0;
        moveMode = true;
        building = null;
        customizePane = null;
        tree = null;
        land = null;
        DataBase.setSelectedUnit(null);
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainPane = new Pane();
        pane = new Pane();
        mainPane.getChildren().add(pane);
        Game.stage = stage;

        this.scene = new Scene(mainPane, screenWidth, screenHeight);
        stage.setScene(scene);

        blockPixel = 30;
        blockWidth = ((int) Math.ceil(screenWidth / blockPixel));
        blockHeight = ((int) Math.ceil(screenHeight / blockPixel));

        setHoverTimeline();

        drawMap();
        drawBottom();
        keys();

        stage.setFullScreen(true);
        stage.show();
    }

    public static void setXY(int x, int y) {
        selectedX = x;
        selectedY = y;
    }

    private boolean isCordInMap(double x, double y) {
        return x > leftX && x < screenWidth + leftX && y < screenHeight;
    }

    private void setHoverTimeline() throws IOException {
        squareInfo = FXMLLoader.load(
                new URL(Objects.requireNonNull(Game.class.getResource("/fxml/SquareInfo.fxml")).toExternalForm()));
        squareInfo.setLayoutX(leftX + screenWidth + 50);
        squareInfo.setLayoutY(50);

        Label landLabel = new Label();
        landLabel.setLayoutY(25);
        landLabel.setAlignment(Pos.CENTER);
        landLabel.setPrefHeight(25);
        landLabel.setPrefWidth(100);
        squareInfo.getChildren().add(landLabel);

        Label treeLabel = new Label();
        treeLabel.setLayoutY(75);
        treeLabel.setAlignment(Pos.CENTER);
        treeLabel.setPrefHeight(25);
        treeLabel.setPrefWidth(100);
        squareInfo.getChildren().add(treeLabel);

        Label buildingLabel = new Label();
        buildingLabel.setLayoutY(125);
        buildingLabel.setAlignment(Pos.CENTER);
        buildingLabel.setPrefHeight(25);
        buildingLabel.setPrefWidth(100);
        squareInfo.getChildren().add(buildingLabel);

        hoverTimeline = new Timeline(new KeyFrame(Duration.seconds(3), actionEvent -> {
            int nowX = (int) (Math.floor((mouseX - leftX) / blockPixel));
            int nowY = (int) (Math.floor(mouseY / blockPixel));

            try {
                if (moveMode && isCordInMap(mouseX, mouseY))
                    drawSquareInfo(squares[nowX + squareI][squareJ + nowY], landLabel, treeLabel, buildingLabel);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        hoverTimeline.setCycleCount(-1);
        hoverTimeline.play();
    }

    public void keys() {
        double minX = leftX;
        double maxX = leftX + blockWidth * blockPixel;
        Robot robot = new Robot();

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            double x = event.getX();
            double y = event.getY();

            if (customizePane == null && (x < minX || x > maxX)) {
                robot.mouseMove(Math.min(Math.max(minX, x), maxX), y);
            }
        });

        scene.setOnMousePressed(event -> {
            double startX = event.getX();
            double startY = event.getY();
            blockX = (int) (Math.floor((startX - leftX) / blockPixel));
            blockY = (int) (Math.floor(startY / blockPixel));

            if (event.getButton() == MouseButton.SECONDARY) {
                moveMode = true;
                mainPane.getChildren().remove(customizePane);
                customizePane = null;
                tree = null;
                land = null;
                building = null;
                selectSq.setVisible(false);
                DataBase.setSelectedUnit(null);
                bottomPane.getChildren().remove(buildingDetail);
            }

            if (customizePane != null) {
               if (tree != null)
                   CustomizeMapController.putTree(tree, squareI + blockX, squareJ + blockY);
               else if (land != null)
                   CustomizeMapController.changeLand(land, squareI + blockX, squareJ + blockY);

               drawMap();
            } else if (DataBase.getSelectedUnit() != null) {
                //TODO : move
            } else if (squares[squareI + blockX][squareJ + blockY].getBuilding() != null) {
                try {
                    showBuildingDetail(squares[squareI + blockX][squareJ + blockY].getBuilding());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                moveMode = true;
            } else if (!moveMode) {
                selectSq.setX(leftX + blockX * blockPixel);
                selectSq.setY(blockY * blockPixel);
                selectSq.setWidth(blockPixel);
                selectSq.setHeight(blockPixel);
                selectSq.setVisible(true);
            }
        });

        scene.setOnMouseDragged(event -> {
            double endX = event.getX();
            double endY = event.getY();
            boolean draw = false;
            int nowX = (int) (Math.floor((endX - leftX) / blockPixel));
            int nowY = (int) (Math.floor(endY / blockPixel));


            if (building != null) {

            } else if (DataBase.getSelectedUnit() != null) {
                //Nothing
            } else if (moveMode) {
                if (nowX > blockX && squareI < map.getWidth() - blockWidth + 1) {
                    squareI++;
                    draw = true;
                } else if (nowX < blockX && squareI > 0) {
                    squareI--;
                    draw = true;
                }

                if (nowY > blockY && squareJ < map.getLength() - blockHeight + 1) {
                    squareJ++;
                    draw = true;
                } else if (nowY < blockY && squareJ > 0) {
                    squareJ--;
                    draw = true;
                }

                if (draw ) {
                    blockX = nowX;
                    blockY = nowY;
                    drawMap();
                }
            } else if (customizePane == null) {
                selectSq.setWidth(Math.abs((nowX - blockX) * blockPixel));
                selectSq.setHeight(Math.abs((nowY - blockY) * blockPixel));
                selectSq.setX(leftX + Math.min(blockX, nowX) * blockPixel);
                selectSq.setY(Math.min(blockY, nowY) * blockPixel);
            }
        });

        pane.setOnMouseReleased(event -> {
            double endX = event.getX();
            double endY = event.getY();
            int nowX = (int) (Math.floor((endX - leftX) / blockPixel));
            int nowY = (int) (Math.floor(endY / blockPixel));

            if (building != null) {
                //TODO : put building
            } else if (DataBase.getSelectedUnit() != null) {
                //Nothing
            } else if (!moveMode && customizePane == null) {
                ArrayList<Unit> selectedUnit = new ArrayList<>();
                for (int i = Math.min(blockX, nowX); i < Math.max(blockX, nowX); i++) {
                    for (int j = Math.min(blockY, nowY); j < Math.max(blockY, nowY); j++) {
                        Square thisSquare = squares[squareI + i][squareJ + j];
                        for (Unit unit : thisSquare.getUnits())
                            if (DataBase.getCurrentGovernment().equals(unit.getOwner())) selectedUnit.add(unit);
                    }
                }

                if (selectedUnit.size() != 0) {
                    DataBase.setSelectedUnit(selectedUnit);
                    try {
                        showSelectedSquares(nowX, nowY);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    drawMapDetails(nowX, nowY);
                }
            }
        });

        scene.setOnMouseMoved(event -> {
            mouseX = event.getX();
            mouseY = event.getY();

            mainPane.getChildren().remove(squareInfo);

            hoverTimeline.playFromStart();
        });

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.I) {
                if (blockPixel < 35) {
                    blockPixel += 5;
                    blockWidth = ((int) Math.ceil(screenWidth / blockPixel));
                    blockHeight = ((int) Math.ceil(screenHeight / blockPixel));

                    while (blockWidth * blockPixel < screenWidth)
                        blockWidth++;
                    while (blockHeight * blockPixel < screenHeight)
                        blockHeight++;

                    drawMap();
                }
            } else if (event.getCode() == KeyCode.O) {
                if (blockPixel > 25) {
                    blockPixel -= 5;
                    blockWidth = ((int) Math.ceil(screenWidth / blockPixel));
                    blockHeight = ((int) Math.ceil(screenHeight / blockPixel));

                    while (blockWidth * blockPixel < screenWidth)
                        blockWidth++;
                    while (blockHeight * blockPixel < screenHeight)
                        blockHeight++;

                    if (squareI > map.getWidth() - blockWidth) squareI = map.getWidth() - blockWidth;
                    if (squareJ > map.getLength() - blockHeight) squareJ = map.getLength() - blockHeight;

                    drawMap();
                }
            } else if (event.getCode() == KeyCode.S) {
                moveMode = !moveMode;
                customizePane = null;
                building = null;
                tree = null;
                land = null;
            } else if (event.getCode() == KeyCode.C) {
                if (customizePane == null) {
                    try {
                        building = null;
                        moveMode = false;
                        drawLeft();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    mainPane.getChildren().remove(customizePane);
                    customizePane = null;
                    tree = null;
                    land = null;
                    building = null;
                    moveMode = true;
                }
            } else if (event.getCode() == KeyCode.M) {
                if (DataBase.getSelectedUnit() != null) moveGetCoordinate();
            } else if (event.getCode() == KeyCode.A) {
                if (DataBase.getSelectedUnit() != null) attackGetCoordinate();
            }
        });
    }

    private void drawMap() {
        squares = map.getSquares();
        pane.getChildren().clear();
        pane.getChildren().add(blackRec);

        ArrayList<Building> buildingsInMap = new ArrayList<>();

        boolean check;
        int k = leftX, l = 0;
        for (int i = squareI; i < squareI + blockWidth; i++) {
            for (int j = squareJ; j < squareJ + blockHeight; j++) {
                ImageView imageView = new ImageView(tiles.get(squares[i][j].getLand()));
                imageView.setLayoutX(k);
                imageView.setLayoutY(l);
                imageView.setFitHeight(blockPixel);
                imageView.setFitWidth(blockPixel);
                pane.getChildren().add(imageView);

                check = true;

                if (l + blockPixel > screenHeight) {
                    imageView.setFitHeight(screenHeight - l);
                    check = false;
                }
                if (k + blockPixel > screenWidth + leftX) {
                    imageView.setFitWidth(screenWidth + leftX - k);
                    check = false;
                }

                if (check) {
                    if (squares[i][j].getTree() != null) {
                        ImageView treeImage = new ImageView(trees.get(squares[i][j].getTree()));
                        treeImage.setLayoutX(k);
                        treeImage.setLayoutY(l);
                        treeImage.setFitHeight(blockPixel);
                        treeImage.setFitWidth(blockPixel);
                        pane.getChildren().add(treeImage);
                    }

                    for (Unit unit : squares[i][j].getUnits()) {
                        ImageView unitImage = new ImageView(units.get(unit.getName()));
                        unitImage.setLayoutX(k);
                        unitImage.setLayoutY(l);
                        unitImage.setFitHeight(blockPixel);
                        unitImage.setFitWidth(blockPixel);
                        pane.getChildren().add(unitImage);
                    }

                    if (squares[i][j].getBuilding() != null && !buildingsInMap.contains(squares[i][j].getBuilding()))
                        buildingsInMap.add(squares[i][j].getBuilding());
                }

                l += blockPixel;
            }
            k += blockPixel;
            l = 0;
        }

        for (Building building : buildingsInMap) {
            ImageView buildingImage = new ImageView(buildings.get(building.getName()));

            buildingImage.setLayoutX((building.getXCoordinateLeft() - squareI) * blockPixel + leftX);
            buildingImage.setLayoutY((building.getYCoordinateUp() - squareJ) * blockPixel);
            buildingImage.setFitWidth(building.getWidth() * blockPixel);
            buildingImage.setFitHeight(building.getLength() * blockPixel);

            //TODO : buildings in edge

            pane.getChildren().add(buildingImage);
        }

        pane.getChildren().add(selectSq);
        selectSq.setVisible(false);
    }

    private void drawBottom() throws IOException {
        bottomPane = FXMLLoader.load(
                new URL(Objects.requireNonNull(Game.class.getResource("/fxml/BottomMenu.fxml")).toExternalForm()));
        bottomPane.setLayoutX(leftX);
        bottomPane.setLayoutY(screenHeight - 60);
//        todo uncomment when code finished

//        GameGraphicController.setPopularityGoldPopulation();
        mainPane.getChildren().add(bottomPane);
    }

    private void showSelectedSquares(int finalBlockX, int finalBlockY) throws IOException {
        System.out.println("show selected squares!");
    }

    private void showBuildingDetail(Building building) throws IOException {
        if (building.getName().equals("MercenaryPost")) {
            buildingDetail = FXMLLoader.load(
                    new URL(Objects.requireNonNull(Game.class.getResource("/fxml/MercenaryPost.fxml")).toExternalForm()));

        } else if (building.getName().equals("Barrack")) {
            buildingDetail = FXMLLoader.load(
                    new URL(Objects.requireNonNull(Game.class.getResource("/fxml/Barrack.fxml")).toExternalForm()));
        } else if (building.getName().equals("EngineerGuild")) {
            buildingDetail = FXMLLoader.load(
                    new URL(Objects.requireNonNull(Game.class.getResource("/fxml/EngineerGuild.fxml")).toExternalForm()));
        } else {
            buildingDetail = FXMLLoader.load(
                    new URL(Objects.requireNonNull(Game.class.getResource("/fxml/mercenaryPost.fxml")).toExternalForm()));
        }

        buildingDetail.setLayoutX(115);
        buildingDetail.setLayoutY(30);

        bottomPane.getChildren().add(buildingDetail);
    }

    private void drawMapDetails(int finalBlockX, int finalBlockY) {
        System.out.println("show Map details");
    }

    private void drawLeft() throws IOException {
        customizePane = FXMLLoader.load(
                new URL(Objects.requireNonNull(Game.class.getResource("/fxml/CustomizeMap.fxml")).toExternalForm()));
        customizePane.setLayoutX(0);
        customizePane.setLayoutY(0);
        mainPane.getChildren().add(customizePane);
    }

    private void drawSquareInfo(Square square, Label landLabel, Label treeLabel, Label buildingLabel) throws IOException {
        landLabel.setText(Land.getName(square.getLand()));
        landLabel.setTextFill(Color.WHITE);

        treeLabel.setText(Trees.getName(square.getTree()));
        treeLabel.setTextFill(Color.WHITE);

        if (square.getBuilding() != null) buildingLabel.setText(square.getBuilding().getName());
        else buildingLabel.setText("--");
        buildingLabel.setTextFill(Color.WHITE);

        HashMap<Unit,Integer> unitCount = new HashMap<>();
        for (Unit unit : square.getUnits()) {
            if (!DataBase.getCurrentGovernment().equals(unit.getOwner())) continue;
            if (unitCount.containsKey(unit))
                unitCount.put(unit, unitCount.get(unit) + 1);
            else
                unitCount.put(unit, 1);
        }

        int y = 175;
        for (java.util.Map.Entry<Unit, Integer> set : unitCount.entrySet()) {
            ImageView unitImage = new ImageView(units.get(set.getKey().getName()));
            unitImage.setLayoutX(10);
            unitImage.setLayoutY(y);
            unitImage.setFitWidth(40);
            unitImage.setFitHeight(40);

            Label unitCnt = new Label(set.getValue().toString());
            unitCnt.setLayoutX(60);
            unitCnt.setLayoutY(y);
            unitCnt.setAlignment(Pos.CENTER);
            unitCnt.setPrefHeight(40);
            unitCnt.setPrefWidth(40);
            unitCnt.setTextFill(Color.WHITE);

            squareInfo.getChildren().add(unitImage);
            squareInfo.getChildren().add(unitCnt);

            y += 40;
        }

        if (!mainPane.getChildren().contains(squareInfo)) mainPane.getChildren().add(squareInfo);
    }

    private void moveGetCoordinate() {
        new GetCoordinate();
        if (selectedX != -1 && selectedY != -1) {

        }
    }

    private void attackGetCoordinate() {
        new GetCoordinate();
        if (selectedX != -1 && selectedY != -1) {

        }
    }

    public static void loadImages() throws FileNotFoundException {
        //tiles :
        for (Land land : Land.values()) {
            Image image = new Image(new FileInputStream("src/main/resources/Images/tiles/" + Land.getName(land) + ".jpg"));
            tiles.put(land, image);
        }
        //units :
        for (String unit : Unit.getAllUnits()) {
            Image image = new Image(new FileInputStream("src/main/resources/Images/units/" + unit + ".png"));
            units.put(unit, image);
        }
        //buildings :
        for (String building : Building.getBuildingsNames()) {
            Image image = new Image(new FileInputStream("src/main/resources/Images/buildings/" + building + ".png"));
            buildings.put(building, image);
        }
        //trees :
        for (Trees tree : Trees.values()) {
            Image image = new Image(new FileInputStream("src/main/resources/Images/trees/" + Trees.getName(tree) + ".png"));
            trees.put(tree, image);
        }
    }
}