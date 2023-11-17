package sample;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.ArrayList;

public class SpriteView extends StackPane {
    protected final ImageView imageView;
    private Color color;
    EventHandler<ActionEvent> arrivalHandler;
    double colorOffset;
    private int spritesX;
    private boolean continuousWalking = false;

    public void setDirection(Main.Direction direction) {
        this.direction.setValue(direction);
    }

    public static class Goldie extends HiddenFriend {
        public Goldie(int x, int y) {
            super("goldie", x, y, 1);
        }
    }

    public static class Zee extends HiddenFriend {
        public Zee(int x, int y) {
            super("zee", x, y, 1.4);
        }
    }

    public static class Linky extends HiddenFriend {
        public Linky(int x, int y) {
            super("linky", x, y, 1.2);
        }
    }

    public static class Hazel extends HiddenFriend {
        public Hazel(int x, int y) {
            super("hazel", x, y, 1.2);
        }
    }

    public static class Tiago extends HiddenFriend {
        public Tiago(int x, int y) {
            super("tiago", x, y, 1.2);
        }
    }

    public static class Owlina extends HiddenFriend {
        public Owlina(int x, int y) {
            super("owlina", x, y, 1);
        }
    }

    public static class HiddenFriend extends Friend {
        public HiddenFriend(String name, int x, int y, double scale) {
            super(name, new Main.Location(0, 0), 1);
            setTranslateX(x);
            setTranslateY(y);
            setVisible(false);
        }
    }

    public static class CaptainKube extends Friend {
        public CaptainKube(Main.Location loc) {
            super("captainkube", loc, 1.5);
        }
    }

    public static class Friend extends SpriteView {
        public Friend(String name, Main.Location loc, double scale) {
            super(loadImage("images/" + name + ".png", 1, 1, scale), loc, 1, 1, 1);
            direction.set(Main.Direction.DOWN);
            frame.set(0);
        }
    }

    public static class PodSitter extends Pod {
        public PodSitter(Main.Location loc, Friend friend, boolean alternateColor) {
            super("podsitter", loc, friend, alternateColor, 7, 4, 1.4, 1);
        }
    }
    public static class PodCarrier extends Pod {
        public PodCarrier(Main.Location loc, Friend friend, boolean alternateColor) {
            super("podcarrier", loc, friend, alternateColor, 3, 7, 1.85, 2);
            avoid = Main.phippy;
        }
    }
    public static class PodBalloon extends Pod {
        public PodBalloon(Main.Location loc, Friend friend, boolean alternateColor) {
            super("podballoon", loc, friend, alternateColor, 6, 8,1.8, 1);
            avoid = Main.phippy;
        }
    }
    protected boolean inBounds(Main.Direction direction) {
        Main.Location loc = location.getValue().offset(direction.getXOffset(), direction.getYOffset());
        return (loc.cell_x >= 0) && (loc.cell_x < Main.HORIZONTAL_CELLS) && (loc.cell_y >= 0) && (loc.cell_y < Main.VERTICAL_CELLS);
    }
    public static class Pod extends RandomWalker {
        private String name;
        private Friend friend;

        public Pod(String name, Main.Location loc, Friend friend, boolean alternateColor, int spritesX, int spritesY, double scaleFactor, double speed) {
            super(loadImage("images/" + name + (alternateColor ? "2" : "") + ".png", spritesX, spritesY, scaleFactor), loc, spritesX, spritesY, speed);
            this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            this.friend = friend;
        }

        public String getName() {
            return name;
        }

        public void displayFriend() {
            friend.setVisible(true);
        }
    }
    public static class Phippy extends Shepherd {
        int animalsReturned = 0;
        static final Image PHIPPY = loadImage("images/phippy.png", 3, 8, 2);
        public Phippy(Main.Location loc) {
            super(PHIPPY, loc, 3, 5);
            arrivalHandler = e -> {
                if (Main.captainKube.location.get().equals(location.get())) {
                    System.out.println("Returning pods to the ship");
                    for (SpriteView animal : getAnimals()) {
                        Main.root.getChildren().remove(animal);
                        Main.spriteGroup.getChildren().remove(animal);
                        Main.sprites.remove(animal);
                        ((Pod)animal).displayFriend();
                        animalsReturned++;
                    }
                    getAnimals().clear();
                };
                if ((!Main.gameover) && (animalsReturned >= 6)) {
                    win();
                }
                for (SpriteView sprite : Main.sprites) {
                    if (sprite.getLocation().equals(location.get())) {
                        if (sprite instanceof PodBalloon) {
                            System.out.println("Picking up a pod balloon");
                            if (!getAnimals().contains(sprite)) {
                                getAnimals().add(sprite);
                            }
                        } else if (sprite instanceof PodCarrier) {
                            System.out.println("Picking up a pod carrier");
                            if (!getAnimals().contains(sprite)) {
                                getAnimals().add(sprite);
                            }
                        }
                    }
                }
            };
        }
        public void die() {
            Main.gameover = true;
            RotateTransition rotate = new RotateTransition(Duration.seconds(3), Phippy.this);
            rotate.byAngleProperty().set(1080);
            rotate.setOnFinished(actionEvent -> Main.root.getChildren().remove(Phippy.this));
            rotate.play();
            Main.sprites.remove(this);
            Main.root.getChildren().add(new Rectangle(Main.BOARD_WIDTH, Main.BOARD_HEIGHT, Color.color(0, 0, 0, .4)));
            Label label = new Label("GAME OVER");
            label.setTextFill(Color.WHITESMOKE);
            label.setAlignment(Pos.BASELINE_CENTER);
            label.setFont(Main.pixelated);
            label.setPrefHeight(Main.BOARD_HEIGHT);
            label.setPrefWidth(Main.BOARD_WIDTH);
            Main.root.getChildren().add(label);
        }
        public void win() {
            Main.gameover = true;
            Main.root.getChildren().add(new Rectangle(Main.BOARD_WIDTH, Main.BOARD_HEIGHT, Color.color(0, 0, 0, .4)));
            Label label = new Label("YOU WIN!!!");
            label.setTextFill(Color.LIGHTGREEN);
            label.setAlignment(Pos.BASELINE_CENTER);
            label.setFont(Main.pixelated);
            label.setPrefHeight(Main.BOARD_HEIGHT);
            label.setPrefWidth(Main.BOARD_WIDTH);
            Main.root.getChildren().add(label);
        }
    }

    public static class RandomWalker extends SpriteView {
        protected Timeline walk;
        protected boolean idle = false;
        protected Main.Location target;
        protected SpriteView avoid;
        public RandomWalker(Image spriteSheet, Main.Location loc) {
            this(spriteSheet, loc, 3, 4, 1);
        }
        public RandomWalker(Image spriteSheet, Main.Location loc, int spritesX, int spritesY, double speed) {
            super(spriteSheet, loc, spritesX, spritesY, speed);
            walk = new Timeline(new KeyFrame(Duration.seconds(.2), actionEvent -> {
                if (idle) return;
                if (target != null) {
                    move(getLocation().directionTo(target));
                } else if (avoid != null && (getLocation().distance(avoid.location.get()) < 2)) {
                    move(getLocation().directionFrom(avoid.location.get()));
                } else {
                    Main.Direction random = Main.Direction.random();
                    if (inBounds(random)) {
                        move(random);
                    }
                }
            }));
            walk.setCycleCount(Timeline.INDEFINITE);
            walk.play();
            Main.earthquake.addListener((observable, oldValue, earthquake) -> {
                if (earthquake) {
                    stop();
                } else {
                    play();
                }
            });
        }
        public void stop() {
            walk.stop();
        }
        public void play() {
            walk.play();
        }
    }

    public static class Shepherd extends RandomWalker {
        private ObservableList<SpriteView> animals;
        public ObservableList<SpriteView> getAnimals() {
            return animals;
        }
        public Shepherd(Image spriteSheet, Main.Location loc) {
            this(spriteSheet, loc, 3, 4);
        }
        public Shepherd(Image spriteSheet, Main.Location loc, int spritesX, int spritesY) {
            super(spriteSheet, loc, spritesX, spritesY, 1);
            animals = FXCollections.observableArrayList();
            animals.addListener((ListChangeListener) c -> {
                ObservableList<Node> children = ((Group) getParent()).getChildren();
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved() || c.wasReplaced()) {
                        SpriteView prev = this;
                        int number = 0;
                        for (SpriteView a : animals) {
                            a.following = prev;
                            a.number.set(++number);
                            prev.follower = a;
                            prev = a;
                        }
                    }
                }
            });
        }
        public void move(Main.Direction direction) {
            if (walking != null && walking.getStatus().equals(Animation.Status.RUNNING))
                return;
            if (!inBounds(direction))
                return;
            Main.Location myOldLoc = location.get();
            moveTo(location.getValue().offset(direction.getXOffset(), direction.getYOffset()));
            animals.stream().reduce(myOldLoc,
                    (loc, sprt) -> {
                        Main.Location oldLoc = sprt.location.get();
                        sprt.moveTo(loc);
                        return oldLoc;
                    }, (loc1, loc2) -> loc1
            );
        }
    }

    public static class Ghoul extends SpriteView {
        static final Image GHOUL = loadImage("images/ghoul.png");
        public Ghoul(SpriteView following) {
            super(GHOUL, following);
        }
    }

    private SpriteView following;
    IntegerProperty number = new SimpleIntegerProperty();
    public int getNumber() {
        return number.get();
    }
    public SpriteView(Image spriteSheet, SpriteView following) {
        this(spriteSheet, following.getLocation().offset(-following.getDirection().getXOffset(), -following.getDirection().getYOffset()));
        number.set(following.number.get() + 1);
        this.following = following;
        setDirection(following.getDirection());
        following.follower = this;
        setMouseTransparent(true);
    }
    public SpriteView getFollowing() {
        return following;
    }

    ObjectProperty<Main.Direction> direction = new SimpleObjectProperty<>();
    ObjectProperty<Main.Location> location = new SimpleObjectProperty<>();
    IntegerProperty frame = new SimpleIntegerProperty(1);
    int spriteWidth;
    int spriteHeight;
    Timeline walking;
    SpriteView follower;
    private int spritesY;
    double speed;

    static Image loadImage(String url) {
        return loadImage(url, 3, 4);
    }
    static Image loadImage(String url, int spritesX, int spritesY) {
        return loadImage(url, spritesX, spritesY, 1);
    }
    static Image loadImage(String url, int spritesX, int spritesY, double scale) {
        return new Image(SpriteView.class.getResource(url).toString(), Main.SPRITE_SIZE * spritesX * Main.SCALE * scale, Main.SPRITE_SIZE * spritesY * Main.SCALE * scale, true, false);
    }
    public SpriteView(Image spriteSheet, Main.Location loc) {
        this(spriteSheet, loc, 3, 4, 1);
    }
    public SpriteView(Image spriteSheet, Main.Location loc, int spritesX, int spritesY, double speed) {
        this.spritesX = spritesX;
        this.spritesY = spritesY;
        this.speed = speed;
        imageView = new ImageView(spriteSheet);
        this.location.set(loc);
        Main.sprites.add(this);
        spriteWidth = (int) (spriteSheet.getWidth() / spritesX);
        spriteHeight = (int) (spriteSheet.getHeight() / spritesY);
        setTranslateX(loc.getX() * Main.CELL_SIZE + (Main.CELL_SIZE - spriteWidth) / 2);
        setTranslateY(loc.getY() * Main.CELL_SIZE + (Main.CELL_SIZE - spriteHeight));
        // shouldn't need to subtract or add Main.SCALE
        ChangeListener<Object> updateImage = (ov, o, o2) -> imageView.setViewport(
                new Rectangle2D(frame.get() * spriteWidth + Main.SCALE,
                        direction.get().getOffset() * spriteHeight + Main.SCALE,
                        spriteWidth-Main.SCALE, spriteHeight-Main.SCALE));
        direction.addListener(updateImage);
        frame.addListener(updateImage);
        direction.set(Main.Direction.RIGHT);
        getChildren().add(imageView);
        setPrefSize(Main.SCALE, Main.SCALE);
        StackPane.setAlignment(imageView, Pos.TOP_LEFT);
    }
    public void continuousWalk() {
        if (continuousWalking) return;
        Timeline timeline = new Timeline(Animation.INDEFINITE);
        timeline.getKeyFrames().addAll(getWalkingAnimation());
        timeline.onFinishedProperty().setValue(e -> timeline.play());
        timeline.play();
        continuousWalking = true;
    }
    public void playAnimation() {
        playAnimation(1);
    }
    public void playAnimation(int line) {
        Timeline timeline = new Timeline(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0), event -> {
            switch (line) {
                case 1:
                    setDirection(Main.Direction.ANIMATE1);
                    break;
                case 2:
                    setDirection(Main.Direction.ANIMATE2);
                    break;
                case 3:
                    setDirection(Main.Direction.ANIMATE3);
                    break;
                case 4:
                    setDirection(Main.Direction.ANIMATE4);
                    break;
            }
        }));
        timeline.getKeyFrames().addAll(getWalkingAnimation());
        if (spritesY - 4 > line) {
            timeline.onFinishedProperty().set(t -> playAnimation(line + 1));
        }
        timeline.play();
    }
    public void moveTo(Main.Location loc) {
        if (loc.cell_x < 0)
            loc.cell_x = Main.HORIZONTAL_CELLS - 1;
        if (loc.cell_x >= Main.HORIZONTAL_CELLS)
            loc.cell_x = 0;
        if (loc.cell_y < 0)
            loc.cell_y = Main.VERTICAL_CELLS - 1;
        if (loc.cell_y >= Main.VERTICAL_CELLS) {
            loc.cell_y = 0;
        }
        direction.setValue(location.getValue().directionTo(loc));
        location.setValue(loc);
        walking = new Timeline(Animation.INDEFINITE,
                new KeyFrame(Duration.seconds(1.0 / speed), new KeyValue(translateXProperty(), loc.getX() * Main.CELL_SIZE + (Main.CELL_SIZE - spriteWidth) / 2)),
                new KeyFrame(Duration.seconds(1.0 / speed), new KeyValue(translateYProperty(), loc.getY() * Main.CELL_SIZE + (Main.CELL_SIZE - spriteHeight)))
        );
        walking.getKeyFrames().addAll(getWalkingAnimation());
        walking.setOnFinished(e -> {
            if (arrivalHandler != null) {
                arrivalHandler.handle(e);
            }
        });
        Platform.runLater(walking::play);
    }

    private ArrayList<KeyFrame> getWalkingAnimation() {
        ArrayList<KeyFrame> frames = new ArrayList<>();
        frames.add(new KeyFrame(Duration.seconds(0), new KeyValue(frame, 0)));
        if (spritesX == 3) {
            frames.add(new KeyFrame(Duration.seconds(.75 * speed), new KeyValue(frame, 2)));
            frames.add(new KeyFrame(Duration.seconds(1L / speed), new KeyValue(frame, 1)));
        } else {
            frames.add(new KeyFrame(Duration.seconds(speed), new KeyValue(frame, spritesX - 1)));
        }
        return frames;
    }

    public void move(Main.Direction direction) {
        if (walking != null && walking.getStatus().equals(Animation.Status.RUNNING))
            return;
        moveTo(location.getValue().offset(direction.getXOffset(), direction.getYOffset()));
    }
    public Main.Location getLocation() {
        return location.get();
    }
    public Main.Direction getDirection() {
        return direction.get();
    }
    public void setColor(Color color) {
        this.color = color;
        if (color == null) {
            imageView.setEffect(null);
        } else {
            imageView.setEffect(new ColorAdjust(color.getHue() / 180 - colorOffset, 0.3, 0, 0));
        }
    }
    public Color getColor() {
        return color;
    }
}
