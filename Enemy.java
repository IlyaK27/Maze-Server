import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Enemy {
    private int x;
    private int y;
    private int angle;
    private int health;
    private int damage;
    private Rectangle hitbox;
    private char[][] maze;
    private ArrayList<Player> players;
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
    public void startAttacking(){
        attacker.run();
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
        int angle = (int)(Math.atan((double)(this.y - playerY) / (this.x - playerX)) * (180 / Math.PI));
        int raa = Math.abs(angle); // related acute angle
        if (playerX >= this.x && playerY >= this.y) return raa;
        else if (playerX < this.x && playerY >= this.y) return 180 - raa;
        else if (playerX < this.x && playerY < this.y) return 180 + raa;
        else return 360 - raa;
    }
    public boolean move(ArrayList<Player> players){
        this.players = players;
        Player closestPlayer = null;
        int closestDistance = 0;
        for(Player player: players){
            int playerDistance = (int)Math.sqrt(Math.pow(this.x - player.getX(), 2) + Math.pow(this.y - player.getY(), 2));
            if(player.alive() && !(player.downed()) && !(player.invisible()) && (closestPlayer == null || closestDistance > playerDistance)){
                closestPlayer = player;
                closestDistance = playerDistance;
            }
        }
        int maxBounds = maze.length * Const.TILE_DIMENSIONS;
        int tileX = (this.x + Const.ENEMY_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
        int tileY = (this.y + Const.ENEMY_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
        int x = this.x;
        int y = this.y;
        if(tileX - 1 >= 0 && tileX + 1 <= maze.length - 1 && tileY - 1 >= 0 && tileY + 1 <= maze.length - 1 && closestPlayer != null && closestDistance <= Const.ENEMY_RANGE){
            //System.out.println("enemymove");
            this.angle = calculateAngle(closestPlayer.getX(), closestPlayer.getY());
            x = this.x + xChange();
            y = this.y + yChange();
            this.hitbox.setLocation(x, y);
            Queue<Rectangle> collidedTiles = new LinkedList<Rectangle>();
            for(Integer[] adjacentTile: Const.ADJACENT_SQUARES){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= maze.length - 1 && adjRectYTile >= 0 && adjRectYTile <= maze.length - 1){
                    Rectangle adjRect = new Rectangle(adjRectXTile * Const.TILE_DIMENSIONS, adjRectYTile * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                    if(adjRect.intersects(this.hitbox) && maze[adjRectYTile][adjRectXTile] == Const.WALL){
                        collidedTiles.add(adjRect);
                        //System.out.println("tiles " + tileX + " " + tileY + " " + adjRectXTile + " " + adjRectYTile);
                        // The following if statements use two points that check if the enemy is Intersecting the wall horizontally or vertically
                        // Point xPoint = {x, (int)adjRect.getY()};
                        // Point yPoint = {(int)adjRect.getX(), y};
                        /* 
                        if(adjRect.contains(x, (int)adjRect.getY())){ // If hit wall to the left
                            x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                        }else if (adjRect.contains(x + Const.ENEMY_DIMENSIONS, (int)adjRect.getY())){ // If hit wall to the right
                            x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                        }
                        if(adjRect.contains((int)adjRect.getX(), y)){ // If hit wall above
                            System.out.println("yhit");
                            y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                        }else if (adjRect.contains((int)adjRect.getX(), y + Const.ENEMY_DIMENSIONS)){ // If hit wall below
                            y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                            System.out.println("yhit");
                        }
                        if(adjRect.contains(x, (int)adjRect.getY()) && (counter == 0 || counter == 6 || counter == 7) && !(xCollided)){ // If hit wall to the left
                            x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                            System.out.println("leftCollided");
                            xCollided = true;
                        }else if (adjRect.contains(x + Const.ENEMY_DIMENSIONS, (int)adjRect.getY()) && (counter == 2 || counter == 3 || counter == 4) && !(xCollided)){ // If hit wall to the right
                            x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                            xCollided = true;
                        }
                        if(adjRect.contains((int)adjRect.getX(), y) && (counter == 4 || counter == 5 || counter == 6) && !(yCollided)){ // If hit wall above
                            //System.out.println("yhit");
                            y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                            yCollided = true;
                        }else if (adjRect.contains((int)adjRect.getX(), y + Const.ENEMY_DIMENSIONS)  && (counter == 0 || counter == 1 || counter == 2) && !(yCollided)){ // If hit wall below
                            y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                            yCollided = true;
                            //System.out.println("yhit");
                        }
                        this.hitbox.setLocation(x, y);*/
                    }
                }   
            }
            while(!(collidedTiles.isEmpty())){
                Rectangle adjRect = collidedTiles.poll();
                // These variables are to make sure the right collision spot is checked
                int xDistance = (int)Math.abs(adjRect.getCenterX() - hitbox.getCenterX());
                int yDistance = (int)Math.abs(adjRect.getCenterY() - hitbox.getCenterY());
                if(xChange() < 0 && xDistance >= yDistance && ((int)adjRect.getX() + Const.TILE_DIMENSIONS) > x && (y >= (int)adjRect.getY() && y <= (int)adjRect.getY() + Const.TILE_DIMENSIONS)){ // If hit wall to the left
                //if(x >= (int)adjRect.getX() && x <= (int)(adjRect.getX() + Const.TILE_DIMENSIONS)){ // If hit wall to the left
                    x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                    System.out.println("left");
                }
                else if(xChange() > 0 && xDistance >= yDistance && ((int)adjRect.getX()) < x + Const.ENEMY_DIMENSIONS  && (y >= (int)adjRect.getY() && y <= (int)adjRect.getY() + Const.TILE_DIMENSIONS)){ // If hit wall to the right
                    x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                    System.out.println("right");
                }
                if(yChange() < 0 && yDistance >= xDistance && ((int)adjRect.getY() + Const.TILE_DIMENSIONS) > y && (x >= (int)adjRect.getX() && x <= (int)adjRect.getX() + Const.TILE_DIMENSIONS)){ // If hit wall above
                    y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                    System.out.println("up");
                }
                else if(yChange() > 0 && yDistance >= xDistance && ((int)adjRect.getY()) < y + Const.ENEMY_DIMENSIONS && (x >= (int)adjRect.getX() && x <= (int)adjRect.getX() + Const.TILE_DIMENSIONS)){ // If hit wall below
                    y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                    System.out.println("down");
                }
                /*if(adjRect.contains(x, (int)adjRect.getY())){ // If hit wall to the left
                    x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                }else if (adjRect.contains(x + Const.ENEMY_DIMENSIONS, (int)adjRect.getY())){ // If hit wall to the right
                    x = (int)(adjRect.getX() - Const.ENEMY_DIMENSIONS);
                }
                if(adjRect.contains((int)adjRect.getX(), y)){ // If hit wall above
                    System.out.println("yhit");
                    y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                }else if (adjRect.contains((int)adjRect.getX(), y + Const.ENEMY_DIMENSIONS)){ // If hit wall below
                    y = (int)(adjRect.getY() - Const.ENEMY_DIMENSIONS);
                    System.out.println("yhit");
                }*/
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

    public class AttackThread extends Thread{
        public AttackThread(){}
        public void run(){
            while(health > 0){
                if(players != null){
                    for(Player player: players){
                        //System.out.println(hitbox.getX() + " " + hitbox.getY() + " " + player.getHitbox().getX() + " " + player.getHitbox().getY());
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
}
