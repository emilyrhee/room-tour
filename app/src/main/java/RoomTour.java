import java.util.ArrayList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class RoomTour extends Application {

  public void setBg(StackPane s) {
    BackgroundImage bImg = new BackgroundImage(
        new Image("bg.png"),
        BackgroundRepeat.NO_REPEAT,
        BackgroundRepeat.NO_REPEAT,
        BackgroundPosition.CENTER,
        BackgroundSize.DEFAULT);
    s.setBackground(new Background(bImg));
  }

  public void setStage(Stage st, Scene sc) {
    st.setScene(sc);
    st.setTitle("Room Tour");
  }

  public void setSprite(Sprite s, String fileName, int x, int y) {
    s.setImage(fileName);
    s.setPosition(x, y);
  }

  public void keyPress(ArrayList<String> i, Scene s) {
    s.setOnKeyPressed(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent e) {
        String code = e.getCode().toString();
        if (!i.contains(code))
          i.add(code);
      }
    });
  }

  public void keyRelease(ArrayList<String> i, Scene s) {
    s.setOnKeyReleased(new EventHandler<KeyEvent>() {
      public void handle(KeyEvent e) {
        String code = e.getCode().toString();
        i.remove(code);
      }
    });
  }

  public void moveSprite(Sprite s, ArrayList<String> inputs, double deltaT, double t) {
    int velocity = 70;

    s.setVelocity(0, 0);

    if (inputs.contains("LEFT")) {
      s.addVelocity(-1 * velocity, 0);
      animate(s, t);
    }
    if (inputs.contains("RIGHT")) {
      s.addVelocity(velocity, 0);
      animate(s, t);
    }
    if (inputs.contains("UP")) {
      s.addVelocity(0, -1 * velocity);
      animate(s, t);
    }
    if (inputs.contains("DOWN")) {
      s.addVelocity(0, velocity);
      animate(s, t);
    }
    s.update(deltaT);
  }

  public Image frame(Sprite s, Image[] frames, int[] indexes, double duration, double time) {
    return frames[indexes[
      (int)((time % (indexes.length * duration)) / duration)
    ]];
  }

  public void preventOverlap(Sprite x, Sprite[] xs, double time) {
    if (x.collides(xs))
      x.update(time * -1);
  }

  public ArrayList<Sprite> collisions(Sprite x, Sprite[] xs) {
    ArrayList<Sprite> c = new ArrayList<Sprite>();

    for (int i = 0; i < xs.length; i++)
      if (x.collide(xs[i])) {
        c.add(xs[i]);
      }
    return c;
  }

  public void renderText(ArrayList<Sprite> ss, GraphicsContext g) {
    for (Sprite s : ss) {
      g.strokeText(s.description, 70, 350);
    }
  }

  public void listDifference(ArrayList<String> xs, ArrayList<String> ys) {
    for (String y : ys)
      if (xs.contains(y))
        xs.remove(y);
  }

  public void listAddition(ArrayList<String> xs, ArrayList<String> ys) {
    for (String x : xs)
      if (!ys.contains(x))
        ys.add(x);
  }

  public void renderSprites(
      int x,
      int y,
      Sprite[] s,
      GraphicsContext g) {
    g.clearRect(0, 0, x, y);

    for (int i = 0; i < s.length; i++)
      s[i].render(g);
  }

  public void animate(Sprite s, double time) {
    Image[] frames = new Image[3];

    for (int i = 0; i < 3; i++)
      frames[i] = new Image("me" + i + ".png");

    s.setImage(
      frame(
        s,
        frames,
        new int[] {0, 1, 2, 1},
        100000000,
        time
      )
    );
  }

  public void start(Stage stage) {
    int xLength = 500;
    int yLength = 450;

    // root and scene
    StackPane root = new StackPane();
    Scene scene = new Scene(root, xLength, yLength);

    setBg(root);
    setStage(stage, scene);

    // canvas
    Canvas canvas = new Canvas(xLength, yLength);
    root.getChildren().add(canvas);

    ArrayList<String> inputs = new ArrayList<String>(); // keep before keyPress and keyRelease

    keyPress(inputs, scene);
    keyRelease(inputs, scene);

    // graphic context
    GraphicsContext gc = canvas.getGraphicsContext2D();

    // text and fonts
    gc.setFont(new Font("Arial", 14));
    gc.setStroke(Color.ANTIQUEWHITE);
    gc.setFill(Color.CHOCOLATE);
    gc.setLineWidth(1);

    // sprites
    Sprite bed = new Sprite();
    Sprite me = new Sprite();
    Sprite shelf = new Sprite();
    Sprite desk = new Sprite();
    Sprite wall1 = new Sprite();
    Sprite wall2 = new Sprite();
    Sprite wall3 = new Sprite();
    Sprite piano = new Sprite();

    setSprite(bed, "bed.png", 85, 50);
    setSprite(me, "me1.png", 300, 200);
    setSprite(shelf, "shelf.png", 5, 10);
    setSprite(desk, "desk.png", 230, 30);
    setSprite(wall1, "wall1.png", 0, 0);
    setSprite(wall2, "wall2.png", 449, 0);
    setSprite(wall3, "wall3.png", 462, 363);
    setSprite(piano, "piano.png", 157, 386);

    shelf.description = "I have a lot of plushies for a 20 year old woman.";
    bed.description = "It's easy to tell that frogs are my favorite animal.";
    desk.description = "My prized pink PC. I spent too much on that graphics card, though.";
    piano.description = "A piano my dad gave me when I was little. I don't play a lot now.";

    Sprite[] sprites = {
        wall1,
        wall2,
        wall3,
        piano,
        bed,
        shelf,
        desk,
        me,
    };

    Sprite[] interceptables = {
        bed,
        shelf,
        desk,
        wall1,
        wall2,
        wall3,
        piano,
    };

    Sprite[] textboxables = {
        bed,
        shelf,
        desk,
        piano,
    };

    // game loop
    LongValue lastNanoTime = new LongValue(System.nanoTime());

    new AnimationTimer() {

      public void handle(long currentNanoTime) {

        double deltaSeconds = (currentNanoTime - lastNanoTime.value) / 1000000000.0;
        lastNanoTime.value = currentNanoTime; // reset time

        // state control
        moveSprite(me, inputs, deltaSeconds, currentNanoTime);
        ArrayList<Sprite> collisions = collisions(me, textboxables);
        preventOverlap(me, interceptables, deltaSeconds);

        // render
        renderSprites(
          xLength,
          yLength,
          sprites,
          gc
        );
        renderText(collisions, gc);

      }
    }.start();

    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}