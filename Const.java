public class Const {
    public static final int PORT = 5001;

    // Map width and height
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;

    public static final int CLIENT_VIEW_RADIUS = 1500;

    public static final int HEARTBEAT_RATE = 60000;
    
    public static final int TPS = 50;

    // How many pellets will be in the game when the server starts
    public static final int START_PELLETS = 10;
    public static final int PELLET_SPAWN_RATE = 1000;

    // Stored as a percent
    public static final int GROWTH_RATE = 40;
    public static final int STARTING_RADIUS = 20;
    public static final int FOOD_RADIUS = 10;

    public static final double radians(int angle) {
        return angle / 180d * Math.PI;
    }
    // Conditional speed formula
    public static final int speed(int radius) {
        if (radius < 50) return 20;
        else if (radius < 100) return 25;
        else return 30;
    }
    /* // exponential speed formula
    public static final int speed(int radius) {
        return (int)(5 * Math.pow(0.9, (radius - 30))) + 3;
    }
    */
    public static final int xChange(int angle, int speed) {
        return (int)(Math.cos(radians(angle)) * speed);
    }
    public static final int yChange(int angle, int speed) {
        return (int)(Math.sin(radians(angle)) * speed);
    }
    private Const(){}
}
