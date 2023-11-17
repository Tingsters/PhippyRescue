package sample;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Main extends Application {

    static final int SCALE = 2;
    static final int SPRITE_SIZE = 32;
    static final int CELL_SIZE = SPRITE_SIZE * SCALE;
    static final int HORIZONTAL_CELLS = 12;
    static final int VERTICAL_CELLS = 7;
    static final int BOARD_WIDTH = HORIZONTAL_CELLS * CELL_SIZE;
    static final int BOARD_HEIGHT = VERTICAL_CELLS * CELL_SIZE;
    public static boolean gameover = false;
    private static int anInt;
    private static ImageView sun;
    private ImageView background;
    public static List<SpriteView> sprites = new ArrayList<>();
    public static Group root;
    public static Font pixelated = Font.loadFont(Main.class.getResourceAsStream("fonts/pixelated.ttf"), Main.CELL_SIZE);
    public static Group spriteGroup = new Group();
    public static SpriteView.Phippy phippy;
    public static SpriteView.CaptainKube captainKube;
    public static BooleanProperty earthquake = new SimpleBooleanProperty(false);
    private static Label messageDisplay;
    private static Label pokemonCounter;
    private static int pokemonCaught = 0;
    private static Timeline clearMessageDisplay = new Timeline(new KeyFrame(Duration.seconds(5)));
    private static ColorAdjust colorAdjustment;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PokeTime");
        StackPane wrapper = new StackPane();
        root = new Group();
        Scene scene = new Scene(root, BOARD_WIDTH, BOARD_HEIGHT, Color.BLACK);
        primaryStage.setScene(scene);
        populateBackground(root);
        messageDisplay = new Label();
        pokemonCounter = new Label();

        // Create Phippy
        phippy = new SpriteView.Phippy(new Location(0, 3));

        // Add Captain Kube
        captainKube = new SpriteView.CaptainKube(new Location(2, 3));
        spriteGroup.getChildren().add(captainKube);

        // Create Phippy's Friends
        SpriteView.Goldie goldie = new SpriteView.Goldie(30, 50);
        spriteGroup.getChildren().add(goldie);
        SpriteView.Zee zee = new SpriteView.Zee(80, 59);
        spriteGroup.getChildren().add(zee);
        SpriteView.Linky linky = new SpriteView.Linky(140, 55);
        spriteGroup.getChildren().add(linky);
        SpriteView.Hazel hazel = new SpriteView.Hazel(44, 80);
        spriteGroup.getChildren().add(hazel);
        SpriteView.Tiago tiago = new SpriteView.Tiago(106, 92);
        spriteGroup.getChildren().add(tiago);
        SpriteView.Owlina owlina = new SpriteView.Owlina(155, 85);
        spriteGroup.getChildren().add(owlina);

        // Add some pods
        spriteGroup.getChildren().add(new SpriteView.PodSitter(new Location(8, 2), goldie,false));
        spriteGroup.getChildren().add(new SpriteView.PodSitter(new Location(6, 6), zee,true));
        spriteGroup.getChildren().add(new SpriteView.PodCarrier(new Location(9, 4), linky,false));
        spriteGroup.getChildren().add(new SpriteView.PodCarrier(new Location(5, 1), hazel,true));
        spriteGroup.getChildren().add(new SpriteView.PodBalloon(new Location(7, 6), tiago, false));
        spriteGroup.getChildren().add(new SpriteView.PodBalloon(new Location(9, 4), owlina, true));

        // Load the Sun
        sun = new ImageView(new Image(Main.class.getResource("images/sun.png").toString(), Main.CELL_SIZE * Main.SCALE, Main.CELL_SIZE * Main.SCALE, true, false));

        populateCells(root, phippy);
        root.getChildren().add(spriteGroup);
        spriteGroup.getChildren().add(phippy);
        addKeyHandler(scene, phippy);
        phippy.idle = true;

        colorAdjustment = new ColorAdjust();
        background.setEffect(colorAdjustment);
        root.getChildren().add(messageDisplay);
        messageDisplay.setFont(pixelated);
        messageDisplay.setLayoutX(CELL_SIZE / 4);
        messageDisplay.setLayoutY(BOARD_HEIGHT - CELL_SIZE * 1.7);
        messageDisplay.setTextFill(Color.WHITESMOKE);
        clearMessageDisplay.setOnFinished((ae) -> messageDisplay.setText(""));

        root.getChildren().add(pokemonCounter);
        pokemonCounter.setFont(pixelated);
        pokemonCounter.setLayoutX(CELL_SIZE / 4);
        pokemonCounter.setLayoutY(CELL_SIZE / 4);
        pokemonCounter.setTextFill(Color.DARKGREEN);

        SensorFactory sensorFactory = SensorFactory.create();
        sensorFactory.createButton();
        sensorFactory.createLightSensor();
        sensorFactory.createAccelerometer();

        primaryStage.show();
    }

    private void populateBackground(Group root) {
        background = new ImageView(new Image(getClass().getResource("images/island_background.png").toString(), 768, 448, true, false));
        background.setFitHeight(BOARD_HEIGHT);
        background.setFitWidth(BOARD_WIDTH);
        root.getChildren().add(background);
    }

    private void populateCells(Group root, final SpriteView mainCharacter) {
        // Gratuitous use of lambdas to do nested iteration!
        Group cells = new Group();
        IntStream.range(0, HORIZONTAL_CELLS).mapToObj(i ->
            IntStream.range(0, VERTICAL_CELLS).mapToObj(j -> {
                Rectangle rect = new Rectangle(i * CELL_SIZE, j * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                rect.setFill(Color.rgb(0, 0, 0, 0));
                rect.setStrokeType(StrokeType.INSIDE);
                rect.setStroke(Color.BLACK);
                rect.setOnMousePressed(e -> mainCharacter.move(mainCharacter.location.get().directionTo(new Location(i, j))));
                return rect;
            })
        ).flatMap(s -> s).forEach(cells.getChildren()::add);
        root.getChildren().add(cells);
    }

    private void addKeyHandler(Scene scene, SpriteView mary) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, ke -> {
            KeyCode keyCode = ke.getCode();
            switch (keyCode) {
                case W:
                case UP:
                    mary.move(Direction.UP);
                    break;
                case A:
                case LEFT:
                    mary.move(Direction.LEFT);
                    break;
                case S:
                case DOWN:
                    mary.move(Direction.DOWN);
                    break;
                case D:
                case RIGHT:
                    mary.move(Direction.RIGHT);
                    break;
                case Z:
                    if (ke.isControlDown() && ke.isShiftDown())
                        whistle();
                    break;
                case X:
                    if (ke.isControlDown() && ke.isShiftDown())
                        sun();
                    break;
                case C:
                    if (ke.isControlDown() && ke.isShiftDown())
                        earthquake();
                    break;
                case ESCAPE:
                    System.exit(0);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
    private static SpriteView.Pod enemy;

    public static void whistle() {
        Platform.runLater(() -> whistleImpl());
    }

    private static void whistleImpl() {
        Main.phippy.playAnimation();
        for (SpriteView sprite : sprites) {
            if (sprite instanceof SpriteView.PodSitter) {
                SpriteView.PodSitter podSitter = (SpriteView.PodSitter) sprite;
                sprite.moveTo(phippy.getLocation());
                if (!phippy.getAnimals().contains(podSitter)) {
                    phippy.getAnimals().add(podSitter);
                }
                podSitter.stop();
                podSitter.continuousWalk();
            }
        }
    }

    public static void sun() {
        Main.root.getChildren().add(sun);
        Timeline sunTimeline = new Timeline(new KeyFrame[] {new KeyFrame(Duration.seconds(2), new KeyValue(colorAdjustment.brightnessProperty(), 0.6)),
                     new KeyFrame(Duration.seconds(4), new KeyValue(colorAdjustment.brightnessProperty(), 0))});
        sunTimeline.onFinishedProperty().set(event -> {
            Main.root.getChildren().remove(sun);
            for (SpriteView sprite : sprites) {
                if (sprite instanceof SpriteView.PodBalloon) {
                    SpriteView.PodBalloon podBalloon = (SpriteView.PodBalloon) sprite;
                    podBalloon.stop();
                    podBalloon.playAnimation();
                }
            }
        });
        sunTimeline.play();
    }

    public static void earthquake() {
        earthquake.setValue(true);
        Timeline quakeTimeline = new Timeline();
        for (int i = 1; i < 20; i++) {
            quakeTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(.1 * i), new KeyValue(spriteGroup.translateXProperty(), i % 2 == 0 ? -10 : 10)));
        }
        quakeTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2), new KeyValue(spriteGroup.translateXProperty(), 0)));
        quakeTimeline.setOnFinished((o) -> {
            earthquake.setValue(false);
            for (SpriteView sprite : sprites) {
                if (sprite instanceof SpriteView.Pod.PodCarrier || sprite instanceof SpriteView.Pod.PodBalloon) {
                    SpriteView.Pod pod = (SpriteView.Pod) sprite;
                    pod.stop();
                    pod.playAnimation();
                }
            }
        });
        quakeTimeline.play();
    }

    public static void display(String message) {
        Platform.runLater(() -> {
            messageDisplay.setText(message);
            clearMessageDisplay.playFromStart();
        });
    }

    public static enum Direction {
        DOWN(0), LEFT(1), RIGHT(2), UP(3), ANIMATE1(4), ANIMATE2(5), ANIMATE3(6), ANIMATE4(7);
        private final int offset;
        Direction(int offset) {
            this.offset = offset;
        }
        public int getOffset() {
            return offset;
        }
        public int getXOffset() {
            switch (this) {
                case LEFT:
                    return -1;
                case RIGHT:
                    return 1;
                default:
                    return 0;
            }
        }
        public int getYOffset() {
            switch (this) {
                case UP:
                    return -1;
                case DOWN:
                    return 1;
                default:
                    return 0;
            }
        }
        public static Direction random() {
            switch ((int)(4 * Math.random())) {
                case 0:
                    return DOWN;
                case 1:
                    return LEFT;
                case 2:
                    return RIGHT;
                default:
                    return UP;
            }
        }
    }

    public static class Location {
        int cell_x;
        int cell_y;
        public Location(int cell_x, int cell_y) {
            this.cell_x = cell_x;
            this.cell_y = cell_y;
        }
        public int getX() {
            return cell_x;
        }
        public int getY() {
            return cell_y;
        }
        public Location offset(int x, int y) {
            return new Location(cell_x + x, cell_y + y);
        }
        public Direction directionTo(Location loc) {
            if (Math.abs(loc.cell_x - cell_x) > Math.abs(loc.cell_y - cell_y)) {
                return (loc.cell_x > cell_x) ? Direction.RIGHT : Direction.LEFT;
            } else {
                return (loc.cell_y > cell_y) ? Direction.DOWN : Direction.UP;
            }
        }
        public Direction directionFrom(Location loc) {
            if (Math.abs(loc.cell_x - cell_x) < Math.abs(loc.cell_y - cell_y)) {
                return (loc.cell_x > cell_x) ? Direction.LEFT : Direction.RIGHT;
            } else {
                return (loc.cell_y > cell_y) ? Direction.UP : Direction.DOWN;
            }
        }
        public int distance(Location loc) {
            return (Math.abs(loc.cell_x - cell_x) + Math.abs(loc.cell_y - cell_y)) / 2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Location location = (Location) o;

            if (cell_x != location.cell_x) return false;
            return cell_y == location.cell_y;

        }

        @Override
        public int hashCode() {
            int result = cell_x;
            result = 31 * result + cell_y;
            return result;
        }

        @Override
        public String toString() {
            return "Location{" +
                "cell_x=" + cell_x +
                ", cell_y=" + cell_y +
                '}';
        }
    }

}
