/**
 * Final Game InvestigateAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that lasts over a period of time
 * This ability shows the player the beginning portion of the most optimal path to the end from the players positon upon cast
 */

import java.util.HashMap;
import java.util.LinkedList;

public class InvestigateAbility extends DurationAbility{
    private int[][] optimalTiles;
    public InvestigateAbility(Player player, Lobby lobby){
        super(player, Const.INVESTIGATE_COOLDOWN, Const.INVESTIGATE_DURATION, lobby);
        optimalTiles = new int[3][2];
    }
    
    public boolean doEffect(){
        int playerXTile = player.getX() / Const.TILE_DIMENSIONS;
        int playerYTile = player.getY() / Const.TILE_DIMENSIONS;
        boolean pathGenerated = bfsSearch(playerXTile, playerYTile);
        return pathGenerated;
    }

    public void undoEffect(){
        for(int i = 0; i < optimalTiles.length; i++){
            lobby.getMaze()[optimalTiles[i][1]][optimalTiles[i][0]] = Const.PATH;
        }
    }

    private boolean bfsSearch(int playerX, int playerY){
        Lobby.Game game = lobby.getGame();
        char[][] maze = lobby.getMaze();
        // Using strings as generics because if I use coordinate or Integer[] it doesn't work. I assume the cause is because how the hashcode methods work for those types.
        HashMap<String, String> parentMap = new HashMap<String, String>(); // Key - Child, Value - Parent,
        LinkedList<String> queue = new LinkedList<String>(); // Queue to use during bfs
        queue.add(game.getEnd().x() + " " + game.getEnd().y()); // Start searching from the end to the player
        boolean playerFound = false;
        Lobby.Game.Coordinate[] directions = {game.new Coordinate(0, -1), game.new Coordinate(-1, 0), game.new Coordinate(0, 1), game.new Coordinate(1, 0)};
        while(queue.size() > 0 && playerFound == false){
            String currentCoord = queue.pollFirst();
            String[] splitCoord = currentCoord.split(" ", 2);
            for (Lobby.Game.Coordinate direction : directions) { // For every connected node of the current one
                int newCoordX = Integer.parseInt(splitCoord[0]) + direction.x();
                int newCoordY = Integer.parseInt(splitCoord[1]) + direction.y();
                if(newCoordX >= 0 && newCoordX < maze.length && newCoordY >= 0 && newCoordY < maze.length && maze[newCoordY][newCoordX] != Const.WALL){ // Checking if next coordinate is suitable
                    String newCoord = newCoordX + " " + newCoordY;
                    if(!(parentMap.containsKey(newCoord))){ // Making sure code won't keep visiting same tiles
                        parentMap.put(newCoord, currentCoord);
                        queue.add(newCoord);
                    }
                }
                if(newCoordX == playerX && newCoordY == playerY){playerFound = true;} // Player has been reached
            }
        }
        // After answer is found make the three nearest tiles to the player Optimal path tiles
        if(playerFound){
            Lobby.Game.Coordinate currentCoord = game.new Coordinate(playerX, playerY);
            for(int i = 0; i < Const.INVESTIGATE_RANGE && maze[currentCoord.y()][currentCoord.x()] != Const.END_TILE; i++){ // If the player presses this ability too close to the end for fun no need to change the tile types of the end tiles themselves
                
                optimalTiles[i][0] = currentCoord.x(); 
                optimalTiles[i][1] = currentCoord.y(); 
                maze[currentCoord.y()][currentCoord.x()] = Const.OPTIMAL_PATH;
                String newCoord = parentMap.get(currentCoord.x() + " " + currentCoord.y());
                String[] splitCoord = newCoord.split(" ", 2);
                currentCoord.setX(Integer.parseInt(splitCoord[0]));
                currentCoord.setY(Integer.parseInt(splitCoord[1]));
            }
        }
        return playerFound;
    }
}
