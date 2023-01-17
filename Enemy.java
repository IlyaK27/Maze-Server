import java.awt.Rectangle;
import java.util.ArrayList;

public class Enemy {
    private int x;
    private int y;
    private int angle;
    private int health;
    private int damage;
    private Rectangle hitbox;
    private char[][] maze;
    private Player[] players;
    private AttackThread attacker;
    
    public Enemy(int x, int y, char maze[][], int level){
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.health = Const.ENEMY_HEALTH + (level - 1) * Const.ENEMY_HEALTH_INCREMENT;
        this.damage = Const.ENEMY_DAMAGE + (level - 1) * Const.ENEMY_DAMAGE_INCREMENT;
        this.hitbox = new Rectangle(x, y, Const.ENEMY_DIMENSIONS, Const.ENEMY_DIMENSIONS);
        this.maze = maze;
        this.attacker = new AttackThread();
    }

    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
    public int getAngle(){
        return this.angle;
    }
    public int getHealth(){
        return this.health;
    }
    public void setCoords(int centerX, int centerY){
        this.x = centerX - 55;
        this.y = centerY - 55;
        this.hitbox.setLocation(this.x, this.y);
    }
    public void damage(int damage){
        this.health = Math.max(0, this.health - damage);
    }
    private double radians(int angle) {
        return angle / 180d * Math.PI;
    }
    private int xChange() {
        return (int)(Math.cos(radians(this.angle)) * Const.ENEMY_MOVEMENT_SPEED);
    }
    private int yChange() {
        return (int)(Math.sin(radians(this.angle)) * Const.ENEMY_MOVEMENT_SPEED);
    }
    private int calculateAngle(int playerX, int playerY){
        int angle = (int)(Math.atan((double)(playerY - (double)Const.HEIGHT/2) / (playerX - (double)Const.WIDTH/2)) * (180 / Math.PI));
        int raa = Math.abs(angle); // related acute angle
        if (playerX >= (double)Const.WIDTH/2 && playerY >= (double)Const.HEIGHT/2) return raa;
        else if (playerX < (double)Const.WIDTH/2 && playerY >= (double)Const.HEIGHT/2) return 180 - raa;
        else if (playerX < (double)(Const.WIDTH)/2 && playerY < (double)Const.HEIGHT/2) return 180 + raa;
        else return 360 - raa;
    }
    public void move(ArrayList<Player> players){
        this.players = players.toArray(this.players);
        Player closestPlayer = null;
        int closestDistance = 0;
        for(Player player: players){
            int playerDistance = (int)Math.sqrt(Math.pow(this.x - player.getX(), 2) + Math.pow(this.y - player.getY(), 2));
            if((closestPlayer == null || closestDistance > playerDistance) && player.invisible()){
                closestPlayer = player;
                closestDistance = playerDistance;
            }
        }
        int maxBounds = maze.length * Const.TILE_DIMENSIONS;
        int tileX = this.x / Const.TILE_DIMENSIONS;
        int tileY = this.y / Const.TILE_DIMENSIONS;
        boolean wallIntersected = false;
        this.angle = calculateAngle(closestPlayer.getX(), closestPlayer.getY());
        int x = xChange();
        int y = yChange();
        this.hitbox.setLocation(x, y);
        if(tileX - 1 >= 0 && tileX + 1 <= maze.length - 1 && tileY - 1 >= 0 && tileY + 1 <= maze.length - 1 && closestPlayer != null){
            for(Integer[] adjacentTile: Const.ADJACENT_SQUARES){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= maze.length - 1 && adjRectYTile >= 0 && adjRectYTile <= maze.length - 1){
                    Rectangle adjRect = new Rectangle(adjRectXTile * Const.TILE_DIMENSIONS, adjRectYTile * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                    if(adjRect.intersects(this.hitbox) && maze[adjRectYTile][adjRectXTile] == Const.WALL){
                        wallIntersected = true;
                        // The following if statements use two points that check if the enemy is Intersecting the wall horizontally or vertically
                        // Point xPoint = {x, (int)adjRect.getY()};
                        // Point yPoint = {(int)adjRect.getX(), y};
                        if(adjRect.contains(x, (int)adjRect.getY())){ // If hit wall to the left
                            x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                        }else if (adjRect.contains(x + Const.ENEMY_DIMENSIONS, (int)adjRect.getY())){ // If hit wall to the right
                            x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                        }
                        if(adjRect.contains((int)adjRect.getX(), y)){ // If hit wall above
                            y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                        }else if (adjRect.contains((int)adjRect.getX(), y + Const.ENEMY_DIMENSIONS)){ // If hit wall below
                            y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                        }
                        this.hitbox.setLocation(x, y);
                    }
                }   
            }
        }
        this.x = x;
        this.y = y;
        if(!(wallIntersected)){
            // Making sure player doesn't go out of map
            if(this.x <= 100){ // if player is close to left edge
                this.x = Math.max(0, x);
            }else if(this.x >= maxBounds - 100 - Const.PLAYER_DIMENSIONS){ // If close to right edge
                this.x = Math.min(maxBounds - Const.PLAYER_DIMENSIONS, x);
            }else{
                this.x = x;
            }

            if(this.y <= 100){ // if player is close to top edge
                this.y = Math.max(0, y);
            }else if(this.y >= maxBounds - 100 - Const.PLAYER_DIMENSIONS){ // If close to bottom edge
                this.y = Math.min(maxBounds - Const.PLAYER_DIMENSIONS, y);
            }else{
                this.y = y;
            }
        }   
        this.hitbox.setLocation(this.x, this.y);
    }

    public class AttackThread extends Thread{
        public AttackThread(){
            this.run();
        }
        public void run(){
            while(health >= 0){
                for(Player player: players){
                    if(hitbox.intersects(player.getHitbox())){
                        player.damage(damage);
                    }
                }
                try {
                    Thread.sleep(Const.ENEMY_ATTACKS_SPEED);
                } catch (Exception e) {}
            }
        }
    }
}
