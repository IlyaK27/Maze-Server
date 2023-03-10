/**
 * Final Game Player Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This is the class for the Player of the game
 * Houses the state/position of the player and their various buffs
 */

import java.awt.Rectangle;
import java.util.ArrayList;

public class Player {
    private String name;
    private String color;
    private int x;
    private int y;
    private Rectangle hitbox;
    private int direction;
    private int health;
    private boolean onEnd; // This boolean is true if the player is currently standing on an end square
    private boolean alive;
    private boolean downed; // Player is not dead yet but will die if not revived
    private boolean attackReady;
    private AttackThread attacker;
    // Buffs
    private int maxHealth;
    private int damage;
    private double attackSpeed;
    private int speed;
    private double damageReduction;
    private double lifeSteal;
    private boolean passiveHealing;
    private boolean cloaked; // Enemys will have a harder time detecting this player if this is true


    public Player(String playerName, String color){
        this.name = playerName;
        this.color = color;
        this.direction = 0;
        this.health = Const.PLAYER_MAX_HEALTH;
        this.alive = true;
        this.downed = false;
        this.hitbox = new Rectangle(0, 0, Const.PLAYER_DIMENSIONS, Const.PLAYER_DIMENSIONS);
        this.attackReady = true;
        this.attacker = new AttackThread();
        this.attacker.start();
        resetBuffs();
    } 
    // Getters and Setters
    public String name(){
        return this.name;
    }
    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
    public int getDirection(){
        return this.direction;
    }
    public int getHealth(){
        return this.health;
    }
    public int getMaxHealth(){
        return this.maxHealth;
    }
    public String color(){
        return this.color;
    }
    public boolean onEnd(){
        return this.onEnd;
    }
    public boolean alive(){
        return this.alive;
    }
    public boolean downed(){
        return this.downed;
    }
    public void setDown(boolean down){
        this.downed = down;
    }
    public boolean attackReady(){
        return this.attackReady;
    }
    public boolean cloaked(){
        return this.cloaked;
    }
    public Rectangle getHitbox(){
        return this.hitbox;
    }
    public void setOnEnd(boolean onEnd){
        this.onEnd = onEnd;
    }
    public void die(){
        this.alive = false;
    }
    // Setting the abilities
    public void buffMaxHealth(int addedHealth){
        this.maxHealth = this.maxHealth + addedHealth;
        this.health = this.maxHealth;
    }
    // for all the methods that contain a paramater somethingDiff it means the difference between the current and new attribute. Its not a buff since it can be negative nor a nerf since it can be positive. So its a difference
    public void setDamage(int damageDiff){
        this.damage = this.damage + damageDiff;
    }
    public void setAttackSpeed(double attackSpeedDiff){
        this.attackSpeed = this.attackSpeed + attackSpeedDiff;
    }
    public void setSpeed(int speedDiff){
        this.speed = this.speed + speedDiff;
    }
    public void setToughness(double toughnessDiff){
        this.damageReduction = this.damageReduction + toughnessDiff;
    }
    public void addLifeSteal(double lifeStealDiff){
        this.lifeSteal = this.lifeSteal + lifeStealDiff;
    }
    public void addHealing(boolean healingState){
        this.passiveHealing = healingState;
    }
    public void addCloak(boolean cloakState){
        this.cloaked = cloakState;
    }
    public boolean passivelyHealing(){
        return this.passiveHealing;
    }
    public void resetBuffs(){
        this.maxHealth = Const.PLAYER_MAX_HEALTH;
        this.health = Const.PLAYER_MAX_HEALTH;
        this.damage = Const.PLAYER_DAMAGE;
        this.attackSpeed = 1;
        this.speed = Const.PLAYER_MOVEMENT_SPEED;
        this.damageReduction = 1;
        this.lifeSteal = 0;
        this.passiveHealing = false;
        this.cloaked = false;
    }

    public void attack(ArrayList<Enemy> enemies){
        int directionEven = this.direction % 2; // If this is 0 the player is facing up or down, if its 1 its odd and they are facing left or right
        Rectangle attackHitbox;
        if(directionEven == 0){
            int attackY = this.y - Const.ATTACK_RANGE;
            if(direction == 2){attackY = this.y + Const.PLAYER_DIMENSIONS;}
            attackHitbox = new Rectangle(this.x, attackY, Const.PLAYER_DIMENSIONS, Const.ATTACK_RANGE);
        }else{
            int attackX = this.x - Const.ATTACK_RANGE;
            if(direction == 1){attackX = this.x + Const.PLAYER_DIMENSIONS;}
            attackHitbox = new Rectangle(attackX, this.y, Const.ATTACK_RANGE, Const.PLAYER_DIMENSIONS);
        }
        for(Enemy enemy: enemies){
            if(attackHitbox.intersects(enemy.getHitbox())){
                enemy.damage(this.damage);
                this.heal((int)(this.damage * this.lifeSteal)); // If player's life steal is 0 they just won't heal
            }
        }
        this.attackReady = false;
    }
    public boolean heavyAttack(ArrayList<Enemy> enemies){
        int directionEven = this.direction % 2; // If this is 0 the player is facing up or down, if its 1 its odd and they are facing left or right
        Rectangle attackHitbox;
        if(directionEven == 0){
            int attackY = this.y - Const.BIG_ATTACK_RANGE;
            if(direction == 2){attackY = this.y + Const.PLAYER_DIMENSIONS;}
            attackHitbox = new Rectangle(this.x, attackY, Const.PLAYER_DIMENSIONS, Const.ATTACK_RANGE);
        }else{
            int attackX = this.x - Const.BIG_ATTACK_RANGE;
            if(direction == 1){attackX = this.x + Const.PLAYER_DIMENSIONS;}
            attackHitbox = new Rectangle(attackX, this.y, Const.BIG_ATTACK_RANGE, Const.PLAYER_DIMENSIONS);
        }
        for(Enemy enemy: enemies){
            if(attackHitbox.intersects(enemy.getHitbox())){
                enemy.damage(this.damage * Const.BIG_ATTACK_MULTIPLIER);
                this.heal((int)(this.damage * Const.BIG_ATTACK_MULTIPLIER * this.lifeSteal)); // If player's life steal is 0 they just won't heal
            }
        }
        // No need to reset attackTimer since this is an ability and not a normal attack
        return true; // Attack worked
    }
    public void move(int direction, char[][] maze){
        this.direction = direction;
        int maxBounds = maze.length * Const.TILE_DIMENSIONS;
        int tileX = this.x / Const.TILE_DIMENSIONS;
        int tileY = this.y / Const.TILE_DIMENSIONS;
        Integer[][] tilesInfront = Const.ADJACENT_TILES.get(direction); // This array will contain coordinates of the 3 tiles in the current players direction, for example if direction = 0 the tiles are: top left, top middle, top right of player
        boolean wallIntersected = false;
        int x = this.x + Const.PLAYER_DIRECTIONS.get(direction)[0] * this.speed;
        int y = this.y + Const.PLAYER_DIRECTIONS.get(direction)[1] * this.speed;
        this.hitbox.setLocation(x, y);
        if(direction == 1 || direction == 3 && (tileX - 1 >= 0 && tileX + 1 <= maze.length - 1)){
            for(Integer[] adjacentTile: tilesInfront){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= maze.length - 1 && adjRectYTile >= 0 && adjRectYTile <= maze.length - 1){
                    Rectangle adjRect = new Rectangle(adjRectXTile * Const.TILE_DIMENSIONS, adjRectYTile * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                    if(adjRect.intersects(this.hitbox) && maze[adjRectYTile][adjRectXTile] == Const.WALL){
                        wallIntersected = true;
                        this.y = y;
                        if(direction == 1){ // If it hit a wall to the right
                            this.x = (int)(adjRect.getX() - Const.PLAYER_DIMENSIONS);
                        }else{
                            this.x = (int)(adjRect.getX() + Const.TILE_DIMENSIONS);
                        }
                        break;
                    }
                }   
            }

        }else if(direction == 0 || direction == 2 && (tileY - 1 >= 0 && tileY + 1 <= maze.length - 1)){
            for(Integer[] adjacentTile: tilesInfront){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= maze.length - 1 && adjRectYTile >= 0 && adjRectYTile <= maze.length - 1){
                    Rectangle adjRect = new Rectangle(adjRectXTile * Const.TILE_DIMENSIONS, adjRectYTile * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                    if(adjRect.intersects(this.hitbox) && maze[adjRectYTile][adjRectXTile] == Const.WALL){
                        wallIntersected = true;
                        this.x = x;
                        if(direction == 0){ // If it hit a wall above
                            this.y = (int)(adjRect.getY() + Const.TILE_DIMENSIONS);
                        }else{ // If it hit a wall below
                            this.y = (int)(adjRect.getY() - Const.PLAYER_DIMENSIONS);
                        }
                        break;
                    }
                }   
            }

        }
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
    public void setCoords(int centerX, int centerY){
        this.x = centerX - 55;
        this.y = centerY - 55;
        this.hitbox.setLocation(this.x, this.y);
    }
    public void heal(int healthGained){
        this.health = Math.min(this.maxHealth, this.health + healthGained); // Using Math.min so player can't go over 100 health    
    }
    public void damage(int damage){
        if(!(this.downed)){ // Can't damage while downed or invulnerable
            this.health = Math.max(0, (int)(this.health - damage * damageReduction));
        }
    }
    public void reset(){
        direction = 0;
        health = maxHealth;
        speed = Const.PLAYER_MOVEMENT_SPEED;
        damageReduction = 1;
        onEnd = false;
        alive = true;
        downed = false;
    }
    public class AttackThread extends Thread{
        public AttackThread(){}
        public void run(){
            while(true){
                try {
                    Thread.sleep((int)(Const.PLAYER_ATTACK_SPEED * attackSpeed));
                } catch (Exception e) {}
                if(!(attackReady)){
                    attackReady = true;
                }
            }
        }
    }
}
