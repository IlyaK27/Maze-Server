public class Player {
    private String name;
    private int x;
    private int y;
    private int direction;
    private int health;

    public Player(String playerName){
        this.name = playerName;
    }
    
    public String name(){
        return this.name;
    }
}
