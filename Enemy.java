/**
 * Final Game Enemy Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This is the class for the Enemy of the game
 * Houses the state/position of the player
 */

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
    private boolean inWall;
    
    public Enemy(int x, int y, char maze[][], int level){
        this.x = x;
        this.y = y;
        this.angle = 0;
        this.health = Const.ENEMY_HEALTH + (level - 1) * Const.ENEMY_HEALTH_INCREMENT;
        this.damage = Const.ENEMY_DAMAGE + (level - 1) * Const.ENEMY_DAMAGE_INCREMENT;
        this.hitbox = new Rectangle(x, y, Const.ENEMY_DIMENSIONS, Const.ENEMY_DIMENSIONS);
        this.maze = maze;
        this.inWall = false;
    }
    // Getters and Setters
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
    public int getDamage(){
        return this.damage;
    }
    public Rectangle getHitbox(){
        return this.hitbox;
    }
    public boolean inWall(){
        return this.inWall;
    }
    public void setCoords(int centerX, int centerY){
        this.x = centerX - 55;
        this.y = centerY - 55;
        this.hitbox.setLocation(this.x, this.y);
    }

    // Methods to do with angles and movement
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
        int angle = (int)(Math.atan((double)(this.y - playerY) / (this.x - playerX)) * (180 / Math.PI));
        int raa = Math.abs(angle); // related acute angle
        if (playerX >= this.x && playerY >= this.y) return raa;
        else if (playerX < this.x && playerY >= this.y) return 180 - raa;
        else if (playerX < this.x && playerY < this.y) return 180 + raa;
        else return 360 - raa;
    }
    public boolean move(ArrayList<Player> players){
        Player closestPlayer = null;
        int closestDistance = 0;
        // Finding the closest player and if they are within the detection range 
        for(Player player: players){
            int playerDistance = (int)Math.sqrt(Math.pow(this.x - player.getX(), 2) + Math.pow(this.y - player.getY(), 2));
            if(player.alive() && !(player.downed()) && (closestPlayer == null || closestDistance > playerDistance)){
                closestPlayer = player;
                closestDistance = playerDistance;
            }
        }
        int tileX = (this.x + Const.ENEMY_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
        int tileY = (this.y + Const.ENEMY_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
        int x = this.x;
        int y = this.y;
        if(closestPlayer != null && !(closestPlayer.cloaked()) && closestDistance <= Const.ENEMY_RANGE || closestPlayer.cloaked() && closestDistance <= Const.ENEMY_RANGE - Const.CLOAK_REDUCTION){ // If there is a valid player close
            this.angle = calculateAngle(closestPlayer.getX(), closestPlayer.getY());
            x = this.x + xChange();
            y = this.y + yChange();
            this.hitbox.setLocation(x, y);
            for(Integer[] adjacentTile: Const.ADJACENT_SQUARES){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= maze.length - 1 && adjRectYTile >= 0 && adjRectYTile <= maze.length - 1){
                    Rectangle adjRect = new Rectangle(adjRectXTile * Const.TILE_DIMENSIONS, adjRectYTile * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                    if(adjRect.intersects(this.hitbox) && maze[adjRectYTile][adjRectXTile] == Const.WALL){ // Checking if enemy has collided with any walls
                        if(adjRect.contains(this.hitbox)){ // If the enemy somehow ended up fully inside the wall it will get deleted and respawned
                            this.inWall = true;
                        }else{ // If the player hit a wall
                            // These variables are to make sure the right collision spot is checked
                            int xDistance = (int)Math.abs(adjRect.getCenterX() - (x + Const.ENEMY_DIMENSIONS / 2));
                            int yDistance = (int)Math.abs(adjRect.getCenterY() - (y + Const.ENEMY_DIMENSIONS / 2));
                            Rectangle intersection = adjRect.intersection(this.hitbox);
                            // Horizontal
                            if(xChange() < 0 && xDistance >= yDistance && (int)intersection.getWidth() > 0){ // Enemy hit wall on its left
                                x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                            }else if(xChange() > 0 && xDistance >= yDistance && (int)intersection.getWidth() > 0){ // Enemy hit wall on its right
                                x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                            }
                            // Vertical
                            if(yChange() < 0 && yDistance >= xDistance && (int)intersection.getHeight() > 0){ // Enemy hit wall above it
                                y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                            }else if(yChange() > 0 && yDistance >= xDistance && (int)intersection.getHeight() > 0){ // Enemy hit wall below it
                                y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                            }
                        }
                    }
                }   
            }
        }
        else{
            return false;
        }
        this.x = x;
        this.y = y;
        this.hitbox.setLocation(this.x, this.y);
        return true;
    }
    public void damage(int damage){
        this.health = Math.max(0, this.health - damage);
    }
}
