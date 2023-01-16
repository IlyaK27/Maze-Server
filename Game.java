import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue; 

public class Game {
    private int rows;
    private int cols;
    private char[][] maze;
    //private Coordinate start; // x y
    //private Coordinate end; // x y
    private int enemyLevel;
    private int minimumDistance; // Minimum distance end has to be from start (not ths same as Const.MIN_DISTANCE, this min distance increases each round)
    //private ArrayList<EnemyHandler> enemies;
    public Game(int maxRows, int maxCols, int enemyLevel, int minimumDistance){
        this.rows = maxRows;
        this.cols = maxCols;
        this.enemyLevel = enemyLevel;
        maze = new char[rows][cols];
        //start = new Coordinate(0,0);
        //end = new Coordinate(0,0);
    }

    /*public char[][] constructMaze(){
        // Generating start and end
        // Start should not be generated on on edge or corner
        this.start.setX((int)(Math.random() * (this.rows - 2 + 1) + 1)); // x
        this.start.setY((int)(Math.random() * (this.cols - 2 + 1) + 1)); // y
        maze[this.start.x()][this.start.y()] = Const.START_TILE;
        System.out.println("Start = " + start.x() + " " + start.y());
        boolean endGenerated = false;
        // End can generate anywhere as long as its not super close to the start
        int currentDistance = 0;
        while(!(endGenerated)){
            this.end.setX((int)(Math.random() * (this.rows - 1))); // x
            this.end.setY((int)(Math.random() * (this.cols - 1))); // y
            int xDistance = Math.abs(this.end.x() - this.start.x());
            int yDistance = Math.abs(this.end.y() - this.start.y());
            currentDistance = xDistance + yDistance;
            if(currentDistance >=Const.MIN_DISTANCE){endGenerated = true;}
        }
        maze[this.end.x()][this.end.y()] = Const.END_TILE;
        System.out.println("End = " + this.end.x() + " " + this.end.y());
        // Creating 1 path (all mazes will have at least 1 path which will be the one generated here but more paths can occur in the next step)
        boolean endReached = false;
        Queue<Coordinate> visitQueue = new LinkedList<Coordinate>();
        Coordinate currentPoint = new Coordinate(this.start.x(), this.start.y());
        while (!(endReached)){
            int xDistance = this.end.x() - currentPoint.x(); // Positive - end on the left, Negative - end on the right, 0 - Right above or below end
            int yDistance = this.end.y() - currentPoint.y(); // Positive - end below, Negative - end above, 0 - To the left or right of end
            int directionChoice = (int)(Math.random()); // 0 = horizontal, 1 = vertical
            if(currentDistance < this.minimumDistance){ // Go away from end until distance is minimum distance 
                if(directionChoice == 0){
                    if(xDistance != 0){
                        int direction = -(xDistance / Math.abs(xDistance)); // Using - absolute value to get opposite of good direction
                        currentPoint.setX(currentPoint.x() + direction);
                    }else{directionChoice = 1;}
                }
                if(directionChoice == 1){
                    int direction = -(yDistance / Math.abs(yDistance)); // Using - absolute value to get opposite of good direction
                    currentPoint.setY(currentPoint.y() + direction);
                }
                currentDistance++;
            }else{ // Go straight towards end
                if(directionChoice == 0){
                    if(xDistance != 0){
                        int direction = xDistance / Math.abs(xDistance); // Using absolute value to get -1 if direction was negative
                        currentPoint.setX(currentPoint.x() + direction);
                    }else{directionChoice = 1;}
                }
                if(directionChoice == 1){
                    int direction = yDistance / Math.abs(yDistance); // Using absolute value to get -1 if direction was negative
                    currentPoint.setY(currentPoint.y() + direction);
                }
                currentDistance--;
            }
            if(visitQueue.contains(currentPoint)){System.out.println("contains");}
            visitQueue.add(new Coordinate(currentPoint.x(), currentPoint.y()));
            if(currentDistance == 1){endReached = true;}
        }
        Coordinate[] directions = {new Coordinate(1,0), new Coordinate(0,1), new Coordinate(-1,0), new Coordinate(0,-1)};
        while(!(visitQueue.isEmpty())){
            currentPoint = visitQueue.poll();
            maze[currentPoint.x()][currentPoint.y()] = Const.PATH;
            for(Coordinate direction: directions){
                Coordinate nextPoint = new Coordinate(currentPoint.x() + direction.x(), currentPoint.y() + direction.y());
                if(nextPoint.x() >= 0 && nextPoint.x() < this.rows && nextPoint.y() >= 0 && nextPoint.y() < this.cols && 
                    maze[nextPoint.x()][nextPoint.y()] == 0){
                    int tileChoice = (int)(Math.random() *(100  + 1)); // 1-Const.PATH_CHANCE(inclusive) = Path, Const.PATh_CHANCe - 100 = Wall
                    if(tileChoice >=1 && tileChoice <= Const.PATH_CHANCE){
                        visitQueue.add(nextPoint);
                    }else if(tileChoice > Const.PATH_CHANCE && tileChoice <= 100){
                        maze[nextPoint.x()][nextPoint.y()] = Const.WALL;
                    }
                }
            }
        }
        // Randomly placing walls and paths
        // Making start and end areas larger
        Coordinate[] adjCoordinates = {new Coordinate(1,0), new Coordinate(0,1), new Coordinate(-1,0), new Coordinate(0,-1),
                                        new Coordinate(1,1), new Coordinate(1,-1), new Coordinate(-1,1), new Coordinate(-1, -1)};
        for(Coordinate adjCoordinate: adjCoordinates){
            // Dont have to worry about adjacent coordinates being out of bounds for start because start is never on the very edge
            maze[start.x() + adjCoordinate.x()][start.y() + adjCoordinate.y()] = Const.START_TILE; 
            if(end.x() + adjCoordinate.x() >= 0 && end.x() + adjCoordinate.x() < rows && end.y() + adjCoordinate.y() >= 0 && end.y() + adjCoordinate.y() < cols){
                maze[end.x() + adjCoordinate.x()][end.y() + adjCoordinate.y()] = Const.END_TILE; 
            }
        }
        System.out.println("MAZE--------");
        for(int x = 0; x < maze.length; x++){
            String row = "";
            for(int y = 0; y < maze[x].length; y++){
                if(maze[x][y] == 0){
                    maze[x][y] = Const.WALL;
                } 
                row = row + " " + maze[x][y];
            }   
            System.out.println(row);
        }
        return this.maze;
    }
    
    public void spawnPlayers(){

    }

    // Classes
    public class EnemyHandler{

    }

    private class Coordinate{
        private int x;
        private int y;
        public Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }
        public int x(){
            return this.x;
        }
        public void setX(int x){
            this.x = x;
        }
        public int y(){
            return this.y;
        }
        public void setY(int y){
            this.y = y;
        }
    }*/
}