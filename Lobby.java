import java.net.*;
import java.util.*;
import java.io.*;
import java.awt.Rectangle;

public class Lobby extends Thread{
    private String name;
    private int playerCount;
    private ArrayList<PlayerHandler> playerHandlers;
    private ArrayList<Player> players;
    private HashMap<PlayerHandler, Player> playerHashMap;
    private Queue<String> colors; // contains hex code of color
    private Game game;
    private int round; 
    private boolean hasPlayers;
    private boolean playing;
    private Countdown countdown;
    private boolean locked; // Locked means no players can join once this is true
    private char[][] maze;

    public Lobby(String name){
        this.name = name;
        this.playerCount = 0;
        this.hasPlayers = true;
        playerHandlers = new ArrayList<PlayerHandler>(Const.LOBBY_SIZE);
        players = new ArrayList<Player>(Const.LOBBY_SIZE);
        playerHashMap = new HashMap<PlayerHandler, Player>();
        colors = new LinkedList<String>();
        colors.add("BLUE");
        colors.add("GREEN");
        colors.add("YELLOW");
        colors.add("ORANGE");
        this.round = 1;
        this.playing = false;
        this.game = new Game(Const.STARTING_ROWS, Const.STARTING_COLS, Const.MIN_DISTANCE);
        this.maze = game.constructMaze();
        this.locked = false;
    }

    public void run(){
        while(hasPlayers){
            System.out.print("");
            while(playing){
                boolean levelBeaten = true;
                for(PlayerHandler player: playerHandlers){
                    if(player.drawn){
                        int[] fovDimensions = game.calculateFov(playerHashMap.get(player));
                        //System.out.println("Calculated fov for -" + playerHashMap.get(player).name() + " " + fovDimensions[0] +  " " + fovDimensions[1] + " " + fovDimensions[2] + " " + fovDimensions[3]) ;
                        //System.out.println(Const.DRAW_MAP + " " + fovDimensions[0] + " " + fovDimensions[1] + " " + fovDimensions[2] + " " + fovDimensions[3]);
                        player.sHandler.print(Const.DRAW_MAP + " " + fovDimensions[0] + " " + fovDimensions[1] + " " + fovDimensions[2] + " " + fovDimensions[3]);
                        player.drawn = false;
                        for(int y = 0; y < fovDimensions[0]; y++){
                            String rowMessage = Const.UPDATE_MAP + " " + y;
                            for(int x = 0; x < fovDimensions[1]; x++){
                                rowMessage  = rowMessage + " " + maze[y + fovDimensions[3]][x + fovDimensions[2]];
                                //System.out.print("coords-" + x + " " + y);
                            }
                            //System.out.print(" Row-" + rowMessage);
                            player.sHandler.print(rowMessage);
                        }
                    }
                    if(!(playerHashMap.get(player).onEnd()) || !(playerHashMap.get(player).alive())){levelBeaten = false;}
                }
                if(levelBeaten){endGame(true);}
            }
        }
    }

    public String name(){
        return this.name;
    }
    public void addPlayer(Socket socket, Server.PlayerHandler sHandler, String playerName){
        PlayerHandler playerHandler = new PlayerHandler(socket, sHandler);
        playerHandler.start();
        boolean nameRepeated = true;
        int repeatCounter = 2; // If there is a player that has the same name it will add the value of this number to the end so that the lobby names don't match Ex. Jeff2
        String oldName = playerName;
        while(nameRepeated){
            nameRepeated = false;
            for (Player player: players) {
                if(player.name().equals(playerName)){
                    nameRepeated = true;
                    playerName = oldName + repeatCounter; 
                    repeatCounter++;
                    break;
                }
            }
        }
        Player player = new Player(playerName, colors.poll());
        for(PlayerHandler players: playerHandlers){ // Telling other players a player has joined
            System.out.println("New player for current lobby");
            players.sHandler.print(Const.NEW_PLAYER + " " + playerName + " " + player.color()); // Client will decode the hex using Color.decode() method
        }
        this.playerHandlers.add(playerHandler);
        this.players.add(player);
        this.playerHashMap.put(playerHandler, player);
        playerCount++; 
        for(PlayerHandler players: playerHandlers){ // Giving the player who just joined info on the players already in the lobby
            player = playerHashMap.get(players);
            while(playerHandler.output == null){System.out.print("");} // Sometimes playerHandler.sHandler.print happens before the input and output get initialized
            System.out.println("New player");
            playerHandler.sHandler.print(Const.NEW_PLAYER + " " + playerHashMap.get(players).name() + " " + player.color());
            if(players.getAbilities() != null){playerHandler.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(players).name() + " " + players.getAbilities());}
            if(players.ready){playerHandler.sHandler.print(Const.READY + " " + playerHashMap.get(players).name());}
        }
        if(playerCount == Const.LOBBY_SIZE){locked = true;}
    }
    public int playerCount(){
        return this.playerCount;
    }
    public boolean locked() {
        return this.locked;
    }
    private void startGameCountdown(){
        this.countdown = new Countdown();
        countdown.start();
    }
    private void endGame(boolean won){
        // Reseting players
        if(won){
            for(PlayerHandler playerHandler: playerHandlers){
                playerHashMap.get(playerHandler).reset();
                playerHandler.sHandler.print(Const.WIN);
            }
            round++;
            this.maze = new char[Const.STARTING_ROWS + (round * Const.MAZE_INCREASE)][Const.STARTING_COLS + (round * Const.MAZE_INCREASE)];
            this.game = new Game(Const.STARTING_ROWS + (round * Const.MAZE_INCREASE), Const.STARTING_COLS + (round * Const.MAZE_INCREASE), Const.MIN_DISTANCE + (round * 2));
            this.maze = this.game.constructMaze();
        }else{
            for(PlayerHandler playerHandler: playerHandlers){
                playerHandler.sHandler.print(Const.LOSE);
            }
        }
        playing = false;
    }

    //----------------------------------------------------------------
    class PlayerHandler extends Thread { 
        Socket socket;
        PrintWriter output;
        BufferedReader input;
        public boolean alive = true;
        Heartbeat heartbeat;
        private String ability1;
        private String ability2;
        private String ultimate;
        Server.PlayerHandler sHandler;
        private boolean ready;
        private boolean drawn;
        
        public PlayerHandler(Socket socket, Server.PlayerHandler sHandler) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
            this.sHandler = sHandler;
            this.ready = false;
            this.drawn = true;
        }
        // Whether the socket is dead
        public boolean isDead() {
            //System.out.println(this.socket + " " + this.output + " " + this.input);
            return this.socket == null || this.output == null || this.input == null;
        }
        // Kill the player's ball
        public void kill() {
            //this.ball = null;
            this.sHandler.print("DIE");
        }
        public void run() {
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream());
                String msg;
                while (true) {
                    //receive a message from the client
                    msg = null;
                    try {
                        msg = input.readLine();
                    } catch (Exception e) {
                        this.close();
                        break;
                    }
                    if (msg != null) {
                        String[] args = msg.split(" ");
                        if(!(msg.equals(Const.DRAWN)) && !(args[0].equals(Const.MOVE))){System.out.println("Message from the clientL: " + msg);}
                        try {
                            /*  JOIN {red} {green} {blue} {*name}
                            if (args[0].equals("JOIN")) {
                                if (this.hasBall()) {
                                    this.sHandler.print("ERROR Player already has joined game");
                                } else {
                                    Color color = new Color(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
                                    String name = "";
                                    for (int i = 4; i < args.length; i++) {
                                        name += args[i] + " ";
                                    }
                                    this.ball = createBall(this, color, name);
                                    this.sHandler.print("JOIN " + this.ball.getId() + " " + this.ball.getX() + " " + this.ball.getY() + " " + this.ball.getRadius());
                                }
                            }
                            // PING
                            // TURN {degree}
                            else if (args[0].equals("TURN")) {
                                if (this.hasBall()) {
                                    this.ball.setAngle(Integer.valueOf(args[1]));
                                } else {
                                    this.sHandler.print("ERROR Player has not joined the game");
                                }
                            }*/
                            if (args[0].equals(Const.PING)) {
                                this.alive = true;
                            }
                            else if (args[0].equals(Const.SELECTED)) { // LEAVE    
                                this.sHandler.print(Const.MY_ABILITIES);
                            }
                            else if (args[0].equals(Const.RESELECT)) { // LEAVE    
                                this.ready = false;
                                if(round == 1){ // You can only reselect abilities before the first round starts, in later rounds if you click the go back button on client you just leave
                                    stopCountdown();
                                    for(PlayerHandler player: playerHandlers){
                                        if(player.equals(this)){
                                            this.sHandler.print(Const.RESELECT + " " + playerHashMap.get(this).name());
                                        }
                                        else{
                                            player.sHandler.print(Const.UNREADY + " " + playerHashMap.get(this).name());
                                        }
                                    }
                                }else{
                                    removePlayer(alive);
                                    for(PlayerHandler players: playerHandlers){ // Unreadying all players that haven't left because a player just left so maybe they aren't ready anymore
                                        for(PlayerHandler player: playerHandlers){ 
                                            player.sHandler.print(Const.UNREADY + " " + playerHashMap.get(players).name());
                                        } 
                                    }
                                }
                            }
                            else if (args[0].equals(Const.MY_ABILITIES)) { // LEAVE    
                                this.ability1 = args[1];
                                this.ability2 = args[2];
                                this.ultimate = args[3];
                                if(ability1 != null && ability2 != null && ultimate != null){
                                    for(PlayerHandler player: playerHandlers){
                                        if(player.equals(this)){
                                            System.out.println("EQUAL" + player.equals(this));
                                            this.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(this).name() + " " + ability1 + " " + ability2 + " " + ultimate + " ME");
                                        }
                                        else{
                                            System.out.println("Other" + player.equals(this));
                                            player.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(this).name() + " " + ability1 + " " + ability2 + " " + ultimate);
                                        }
                                    }
                                }
                                //playerHashMap.get(this).setAbility1(ability1);
                                //playerHashMap.get(this).setAbility2(ability2);
                                //playerHashMap.get(this).setUltimate(ultimate);
                            }
                            else if (args[0].equals(Const.READY)) { // LEAVE    
                                ready = !ready;
                                String message = " " + playerHashMap.get(this).name(); 
                                if(ready){
                                    message = Const.READY + message;
                                    boolean allReady = true;
                                    for(PlayerHandler player: playerHandlers){
                                        player.sHandler.print(message);
                                        if(!(player.ready())){
                                            allReady = false;
                                        }
                                    }if(allReady){
                                        System.out.println("All players ready");
                                        startGameCountdown();
                                    } // If everyone is ready start the countdown timer for the game
                                }else{
                                    stopCountdown();
                                    message = Const.UNREADY + message;
                                    for(PlayerHandler player: playerHandlers){
                                        player.sHandler.print(message);
                                    }
                               }
                            }
                            else if (args[0].equals(Const.MOVE)) { // LEAVE    
                                int direction = Integer.parseInt(args[1]);
                                playerHashMap.get(this).move(direction, maze);
                                Player player = playerHashMap.get(this);
                                for(PlayerHandler playerHandler: playerHandlers){
                                    playerHandler.sHandler.print(Const.PLAYER + " " + player.name() + " " + player.getX() + " " + player.getY() + " " + player.getDirection() + " " + player.getHealth());
                                }
                                if(maze[(player.getY() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS][(player.getX() + Const.PLAYER_DIMENSIONS / 2)  / Const.TILE_DIMENSIONS] == Const.END_TILE){player.setOnEnd(true);}
                                else {player.setOnEnd(false);}
                            }
                            else if (args[0].equals(Const.LEAVE)) { // LEAVE    
                                System.out.println("Lobbyleave");
                                removePlayer(true);
                            }
                            else if (args[0].equals(Const.DRAWN)) { // LEAVE    
                                this.drawn = true;
                            }
                        } catch (Exception e) {
                            this.sHandler.print("LOBBY ERROR invalid arguments");
                            e.printStackTrace();
                        }
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                this.close();
            }
        }
        private void removePlayer(boolean alive){
            String playerName = playerHashMap.get(this).name();
            colors.add(playerHashMap.get(this).color()); // Adding the players color back so it can be re used again
            players.remove(playerHashMap.get(this));
            playerHandlers.remove(this);
            if(playerCount == Const.LOBBY_SIZE){locked = false;}
            playerCount--;
            if (players.size() == 0){
                hasPlayers = false;
            }
            else{
                for (PlayerHandler otherPlayers: playerHandlers) {
                    otherPlayers.sHandler.print(Const.REMOVEP + " " + playerName);
                }
            }
            this.alive = alive; 
            this.heartbeat.playerRemoved = true;
            this.sHandler.leaveLobby(alive);
            this.sHandler.print(Const.LEAVE);
            this.close();
        }
        public String getAbilities() {
            if(ability1 != null && ability2 != null && ultimate != null){
                return this.ability1 + " " + this.ability2 + " " + this.ultimate;
            }
            return null;
        }
        private boolean ready() {
            return this.ready;
        }
        private void stopCountdown(){
            if(countdown != null){
                countdown.interrupt();
                countdown = null;
                System.out.println("Countdown should die");
                if(round == 1){locked = false;} // After first round lobby is permanently locked 
            }
        }
        public void close() {
            this.interrupt();
            // Stop the heartbeat subthread
            try {
                //input.close();
                //output.close();
                input = null;
                output = null;
                //socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.heartbeat.interrupt();
            //this.socket = null;
        }
        // If there is no "heartbeat" from the client for 60 seconds, assume the connection has failed
        class Heartbeat extends Thread {
            protected boolean playerRemoved;
            PlayerHandler playerHandler;

            Heartbeat(PlayerHandler playerHandler) {
                this.playerRemoved = false;
                this.playerHandler = playerHandler;
            }
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Const.HEARTBEAT_RATE);
                    } catch (Exception e) {}
                    System.out.println("Lobby Check for heartbeat");
                    if (this.playerHandler.alive) {
                        this.playerHandler.alive = false;
                    } else {
                        System.out.println("Lobby test");
                        if(!(playerRemoved)){
                            System.out.println("Lobby testss");
                            playerHandler.removePlayer(false);
                        }
                        break;
                    }
                }
            }
        }
    }
    class Countdown extends Thread {
        Countdown() {
        }
        public void run() {
            try {
                locked = true;
                Thread.sleep(Const.COUNTDOWN);
            } catch (Exception e) {

            }
            if(countdown != null){
                game.spawnPlayers();
                for(PlayerHandler playerHandlers: playerHandlers){
                    playerHandlers.ready = false;
                    for(Player player: players){ // Telling all of the clients all of the players positions
                        playerHandlers.sHandler.print(Const.PLAYER + " " + player.name() + " " + player.getX() + " " + player.getY() + " " + player.getDirection() + " " + player.getHealth());
                    }
                    playerHandlers.sHandler.print(Const.GAME_START + " " + playerHashMap.get(playerHandlers).name());
                }
                playing = true;
            }else if (countdown == null){
                System.out.println("Countdown stopped");
            }
        }
    }
    private class Game {
        private int rows;
        private int cols;
        private char[][] maze;
        private Coordinate start; // x y
        private Coordinate end; // x y
        private int minimumDistance; // Minimum distance end has to be from start (not ths same as Const.MIN_DISTANCE, this min distance increases each round)
        private EnemyHandler enemyHandler;
        public Game(int maxRows, int maxCols, int minimumDistance){
            this.rows = maxRows;
            this.cols = maxCols;
            maze = new char[rows][cols];
            start = new Coordinate(0,0);
            end = new Coordinate(0,0);
            this.enemyHandler = new EnemyHandler(Const.MAX_ENEMIES + (round - 1) * Const.ENEMIES_INCREMENT);
        }
    
        public char[][] constructMaze(){
            // Generating start and end
            // Start should not be generated on on edge or corner
            this.start.setX((int)(Math.random() * (this.cols - 2) + 1)); // x
            this.start.setY((int)(Math.random() * (this.rows - 2) + 1)); // y
            maze[this.start.y()][this.start.x()] = Const.START_TILE;
            System.out.println("Start = " + start.x() + " " + start.y());
            boolean endGenerated = false;
            // End can generate anywhere as long as its not super close to the start
            int currentDistance = 0;
            while(!(endGenerated)){
                this.end.setX((int)(Math.random() * (this.cols - 1))); // x
                this.end.setY((int)(Math.random() * (this.rows - 1))); // y
                int xDistance = Math.abs(this.end.x() - this.start.x());
                int yDistance = Math.abs(this.end.y() - this.start.y());
                currentDistance = xDistance + yDistance;
                if(currentDistance >=Const.MIN_DISTANCE){endGenerated = true;}
            }
            maze[this.end.y()][this.end.x()] = Const.END_TILE;
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
                maze[currentPoint.y()][currentPoint.x()] = Const.PATH;
                for(Coordinate direction: directions){
                    Coordinate nextPoint = new Coordinate(currentPoint.x() + direction.x(), currentPoint.y() + direction.y());
                    if(nextPoint.x() >= 0 && nextPoint.x() < this.cols && nextPoint.y() >= 0 && nextPoint.y() < this.rows && 
                        maze[nextPoint.y()][nextPoint.x()] == 0){
                        int tileChoice = (int)(Math.random() *(100  + 1)); // 1-Const.PATH_CHANCE(inclusive) = Path, Const.PATh_CHANCe - 100 = Wall
                        if(tileChoice >=1 && tileChoice <= Const.PATH_CHANCE){
                            visitQueue.add(nextPoint);
                        }else if(tileChoice > Const.PATH_CHANCE && tileChoice <= 100){
                            maze[nextPoint.y()][nextPoint.x()] = Const.WALL;
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
                maze[start.y() + adjCoordinate.y()][start.x() + adjCoordinate.x()] = Const.START_TILE; 
                if(end.x() + adjCoordinate.x() >= 0 && end.x() + adjCoordinate.x() < cols && end.y() + adjCoordinate.y() >= 0 && end.y() + adjCoordinate.y() < rows){
                    maze[end.y() + adjCoordinate.y()][end.x() + adjCoordinate.x()] = Const.END_TILE; 
                    System.out.println("End - " + (end.x() + adjCoordinate.x()) * Const.TILE_DIMENSIONS + " " + (end.y() + adjCoordinate.y()) * Const.TILE_DIMENSIONS);
                }
            }
            System.out.println("MAZE--------");
            for(int y = 0; y < maze.length; y++){
                String row = "";
                for(int x = 0; x < maze[y].length; x++){
                    if(maze[y][x] == 0){
                        maze[y][x] = Const.WALL;
                    } 
                    row = row + " " + maze[y][x];
                }   
                System.out.println(row);
            }
            return this.maze;
        }

        public int[] calculateFov(Player player){
            int[] fov = new int[4]; // 0 - topLeftX, 1 - topLeftY, 2 - bottomRightX, 3 - bottomRightY
            int playerXTile = (player.getX() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
            int playerYTile = (player.getY() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
            //System.out.print("Player tile - " + playerXTile + " " + playerYTile);
            // Using Math.min and Math.max to make sure if player is close to edge no array out of bounds exception happens
            // Player can maximum seen 9 tiles wide or 7 tiles high. Perfect situation if all tiles are fully show is an 8x6 tileset (1200/150)x(900/150)
            int topX = Math.max(0, playerXTile - 4);
            int topY = Math.max(0, playerYTile - 3);
            int botX = Math.min(rows - 1, playerXTile + 5); // 1 added more so that a 9x7 can be drawn
            int botY = Math.min(cols - 1, playerYTile + 4); // 1 added more so that a 9x7 can be drawn
            int cols = botX - topX + 1; // Adding 1 to include the right most col so that a 9x7 can be drawn
            int rows = botY - topY + 1; // Adding 1 to include the lower most row so that a 9x7 can be drawn
            //System.out.print(" |dimensions-" + topX + " " + botX + " " + topY + " " + + botY + " " + rows + " " + cols);
            fov[0] = rows; fov[1] = cols; fov[2] = topX; fov[3] = topY;
            //System.out.print("Player tile - " + playerXTile + " " + playerYTile + " " + topX + " " + topY + " " + botX + " " + botY);
            return fov;
        }
    
        public void spawnPlayers(){ // This will spawn the players on the 4 squares on the corner of the spawn box going from top left to bottom right
            Coordinate topLeftStart = new Coordinate(this.start.x() - 1, this.start.y() - 1);
            for(int y = 0, playerCount = 0; y < 2 && playerCount < players.size(); y++){
                for(int x = 0; x < 2 && playerCount < players.size(); x++, playerCount++){
                    //System.out.println("Coords - " + topLeftStart.x() * Const.TILE_DIMENSIONS + " " + topLeftStart.y() * Const.TILE_DIMENSIONS);
                    //System.out.println("Coords - " + (topLeftStart.x() + 2 * x) * Const.TILE_DIMENSIONS + " " + (topLeftStart.y() + 2 * y) * Const.TILE_DIMENSIONS + " " + ((topLeftStart.x() + 2 * x) * Const.TILE_DIMENSIONS + + Const.TILE_DIMENSIONS/2) + " " + ((topLeftStart.y() + 2 * y) * Const.TILE_DIMENSIONS + + Const.TILE_DIMENSIONS/2));
                    players.get(playerCount).setCoords(((topLeftStart.x() + 2 * x) * Const.TILE_DIMENSIONS) + Const.TILE_DIMENSIONS/2, ((topLeftStart.y() + 2 * y) * Const.TILE_DIMENSIONS) + Const.TILE_DIMENSIONS/2);
                }
            }
        }
    
        // Classes
        public class EnemyHandler extends Thread{
            private int maxEnemies;
            private ArrayList<Enemy> enemies;
            public EnemyHandler(int maxEnemies){
                this.maxEnemies = maxEnemies;
                enemies = new ArrayList<Enemy>();
                while(enemies.size() < maxEnemies){
                    Enemy newEnemy = addEnemy();
                    for(PlayerHandler playerHandler: playerHandlers){
                        playerHandler.sHandler.print(Const.NEWE + " " + newEnemy.getX() + " " + newEnemy.getY() + newEnemy.getHealth());
                    }
                }
            }
            public void run(){
                while(playing){
                    int idCounter = 0;
                    for(Enemy enemy: enemies){
                        if(enemy.getHealth() > 0){
                            enemy.move(players);
                            for(PlayerHandler playerHandler: playerHandlers){
                                playerHandler.sHandler.print(Const.ENEMY + " " + idCounter + " " + enemy.getX() + " " + enemy.getY() + enemy.getAngle() + " " + enemy.getHealth());
                            }
                        }else{
                            enemies.remove(enemy);
                            Enemy newEnemy = addEnemy();
                            for(PlayerHandler playerHandler: playerHandlers){
                                playerHandler.sHandler.print(Const.KILLEDE + " " + idCounter);
                                playerHandler.sHandler.print(Const.NEWE + " " + newEnemy.getX() + " " + newEnemy.getY() + newEnemy.getHealth());
                            }
                        }
                        idCounter++;
                    }
                }
            }
            private Enemy addEnemy(){
                boolean cantSpawn = true;
                int enemyXSquare = 0;
                int enemyYSquare = 0;
                while(cantSpawn){
                    enemyXSquare = (int)(Math.random() * (maze.length - 1));
                    enemyYSquare = (int)(Math.random() * (maze.length - 1));
                    if(maze[enemyYSquare][enemyXSquare] == Const.PATH){
                        cantSpawn = false;
                        Rectangle tile = new Rectangle(enemyXSquare * Const.TILE_DIMENSIONS, enemyYSquare * Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS, Const.TILE_DIMENSIONS);
                        for(Player player: players){
                            if(tile.intersects(player.getHitbox())){ // Enemy cant spawn in a tile that a player is in
                                cantSpawn = true;
                                break;
                            }
                        }
                    }
                }
                int enemyX = enemyXSquare * Const.TILE_DIMENSIONS + Const.ENEMY_DIMENSIONS / 2;
                int enemyY = enemyYSquare * Const.TILE_DIMENSIONS + Const.ENEMY_DIMENSIONS / 2;
                Enemy enemy = new Enemy(enemyX, enemyY, maze, round);
                enemies.add(enemy);
                return enemy;
            }
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
        }
    }
}