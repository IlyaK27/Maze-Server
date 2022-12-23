import java.awt.*;

public class Circle {
    private int id;
    protected int x;
    protected int y;
    protected int radius;
    protected Color color;
    
    public Circle(int id, int x, int y, int radius, Color color) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.color = color;
    }
    public int getId() {
        return this.id;
    }
    public int getX() {
        return this.x;
    }
    public void setX(int x) {
        this.x = Math.min(Const.WIDTH, Math.max(0, x));
    }
    public int getY() {
        return this.y;
    }
    public void setY(int y) {
        this.y = Math.min(Const.HEIGHT, Math.max(0, y));
    }
    public int getRadius() {
        return this.radius;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }
    public Color getColor() {
        return this.color;
    }
    public boolean equals(Circle other) {
        return this.id == other.getId();
    }
    public int diameter() {
        return this.radius * 2;
    }
    public double distance(Circle other) {
        return Math.sqrt(Math.pow(this.x - other.getX(), 2) + Math.pow(this.y - other.getY(), 2));
    }
    public boolean intersects(Circle other) {
        return this.distance(other) <= this.radius + other.getRadius();
    }
}
