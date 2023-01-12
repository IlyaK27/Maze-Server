import java.awt.Color;

public class Player {
    private String name;
    private Color color;
    private int x;
    private int y;
    private int direction;
    private int health;

    public Player(String playerName, Color color){
        this.name = playerName;
        this.color = color;
    }
    
    public String name(){
        return this.name;
    }
    public Color color(){
        return this.color;
    }
}
