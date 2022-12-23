import java.awt.*;

public class Pellet extends Circle {
    Pellet(int id, int x, int y) {
        super(id, x, y, Const.FOOD_RADIUS, new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random()) * 255));
    }
}