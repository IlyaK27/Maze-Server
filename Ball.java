import java.awt.*; 

public class Ball extends Circle {
    private String name;
    private int angle;
    // todo: implement ball growth
    
    public Ball(int id, int x, int y, int angle, Color color, String name){
        super(id, x, y, Const.STARTING_RADIUS, color);
        this.name = name;
        this.angle = angle;
    }
    public String getName() {
        return this.name;
    }
    public int getAngle() {
        return this.angle;
    }
    public void setAngle(int angle) {
        this.angle = angle % 360;
    }
    // Checks if the center of the smaller circle is in the radius of the larger circle
    public boolean eats(Circle other) {
        return (double)this.radius / other.getRadius() > 1.2 && this.distance(other) < this.radius;
    }
}
