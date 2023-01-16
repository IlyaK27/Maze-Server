import java.awt.Rectangle;

public class Player {
    private String name;
    private String color;
    private int x;
    private int y;
    private Rectangle hitbox;
    private int direction;
    private int health;
    private int speed;
    private int damageReduction;
    private boolean onEnd; // This boolean is true if the player is currently standing on an end square
    private boolean alive;

    public Player(String playerName, String color){
        this.name = playerName;
        this.color = color;
        this.direction = 0;
        this.health = 100;
        this.speed = 1;
        this.damageReduction = 1;
        this.alive = true;
        this.hitbox = new Rectangle(0, 0, Const.PLAYER_DIMENSIONS, Const.PLAYER_DIMENSIONS);
    }   
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
    public String color(){
        return this.color;
    }
    public boolean onEnd(){
        return this.onEnd;
    }
    public boolean alive(){
        return this.alive;
    }
    public Rectangle getHitbox(){
        return this.hitbox;
    }
    public void setOnEnd(boolean onEnd){
        if(onEnd){System.out.println("OnEnd");}
        this.onEnd = onEnd;
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
        if(direction == 1 || direction == 3 && (tileX - 1 >= 0 && tileX + 1 <= 19)){
            for(Integer[] adjacentTile: tilesInfront){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= 19 && adjRectYTile >= 0 && adjRectYTile <= 19){
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

        }else if(direction == 0 || direction == 2 && (tileY - 1 >= 0 && tileY + 1 <= 19)){
            for(Integer[] adjacentTile: tilesInfront){
                int adjRectXTile = tileX + adjacentTile[0];
                int adjRectYTile = tileY + adjacentTile[1];
                if(adjRectXTile >= 0 && adjRectXTile <= 19 && adjRectYTile >= 0 && adjRectYTile <= 19){
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
    public void reset(){
        direction = 0;
        health = 100;
        speed = 1;
        damageReduction = 1;
        onEnd = false;
    }
}
