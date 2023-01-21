/**
 * Final Game Lobby Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This is the Lobby class of the game
 * In the lobby the actual game will take place and where the players play
 * Each lobby can be treated as 1 game
 */

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
                try{
                    Thread.sleep(10); 
                }catch(Exception e){}
                boolean levelBeaten = true;
                for(PlayerHandler playerHandler: playerHandlers){
                    if(playerHandler.drawn){
                        int[] fovDimensions = game.calculateFov(playerHandler.currentPlayer); // Giving the client the fov of the player they are currently spectating (By default its themselves but once they die it can change)
                        playerHandler.sHandler.print(Const.DRAW_MAP + " " + fovDimensions[0] + " " + fovDimensions[1] + " " + fovDimensions[2] + " " + fovDimensions[3]);
                        playerHandler.drawn = false;
                        for(int y = 0; y < fovDimensions[0]; y++){
                            String rowMessage = Const.UPDATE_MAP + " " + y;
                            for(int x = 0; x < fovDimensions[1]; x++){
                                rowMessage  = rowMessage + " " + maze[y + fovDimensions[3]][x + fovDimensions[2]];
                            }
                            //System.out.print(" Row-" + rowMessage);
                            playerHandler.sHandler.print(rowMessage);
                        }
                    }
                    Player currentPlayer = playerHashMap.get(playerHandler);
                    if(currentPlayer.getHealth() == 0 && !(currentPlayer.downed())){
                        currentPlayer.setDown(true);
                        System.out.println("player downed");
                        for(PlayerHandler handlers: playerHandlers){
                            handlers.sHandler.print(Const.DOWNED + " " + currentPlayer.name());
                        }
                        Bleeder bleeder = new Bleeder(playerHandler);
                    }
                    if(!(currentPlayer.onEnd()) && currentPlayer.alive()){levelBeaten = false;}
                }
                if(levelBeaten && playing){endGame(true);}
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
        playerHandler.currentPlayer = player;
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
        for(Enemy enemy: game.enemyHandler.enemies){
            playerHandler.sHandler.print(Const.NEWE + " " + enemy.getX() + " " + enemy.getY() + " " + enemy.getHealth());
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
        playing = false;
        if(won){
            round++;
            this.maze = new char[Const.STARTING_ROWS + (round * Const.MAZE_INCREASE)][Const.STARTING_COLS + (round * Const.MAZE_INCREASE)];
            this.game = new Game(Const.STARTING_ROWS + (round * Const.MAZE_INCREASE), Const.STARTING_COLS + (round * Const.MAZE_INCREASE), Const.MIN_DISTANCE + (round * 2));
            this.maze = this.game.constructMaze();
            for(PlayerHandler playerHandler: playerHandlers){
                playerHashMap.get(playerHandler).reset();
                playerHandler.sHandler.print(Const.WIN);
            }
        }else{
            for(PlayerHandler playerHandler: playerHandlers){
                playerHandler.sHandler.print(Const.LOSE + " " + round);
            }
            for(PlayerHandler playerHandler: playerHandlers){ // Removing all players after everyone got the message that they lost
                playerHandler.removePlayer(true);
            }
        }
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
        private Player currentPlayer;
        
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
                            if (args[0].equals(Const.PING)){ // PING
                                this.alive = true;
                            }
                            else if (args[0].equals(Const.SELECTED)) { // LEAVE    
                                this.sHandler.print(Const.MY_ABILITIES); // Asking for the players abilities once they selected them
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
                            else if (args[0].equals(Const.MY_ABILITIES)) { // MY_ABILITIES ability1Name ability2Name ultimateName    
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
                            else if (args[0].equals(Const.READY)) { // READY    
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
                            else if (args[0].equals(Const.MOVE)) { // MOVE, direction 
                                if(!(playerHashMap.get(this).downed()) && playerHashMap.get(this).alive()){
                                    int direction = Integer.parseInt(args[1]);
                                    playerHashMap.get(this).move(direction, maze);
                                    Player player = playerHashMap.get(this);
                                    for(PlayerHandler playerHandler: playerHandlers){
                                        playerHandler.sHandler.print(Const.PLAYER + " " + player.name() + " " + player.getX() + " " + player.getY() + " " + player.getDirection() + " " + player.getHealth());
                                    }
                                    if(maze[(player.getY() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS][(player.getX() + Const.PLAYER_DIMENSIONS / 2)  / Const.TILE_DIMENSIONS] == Const.END_TILE){player.setOnEnd(true);}
                                    else {player.setOnEnd(false);}
                                }
                            }
                            else if (args[0].equals(Const.LEAVE)) { // LEAVE    
                                removePlayer(true);
                            }
                            else if (args[0].equals(Const.DRAWN)) { // DRAWN    
                                this.drawn = true; // This is so that the server doesn't send a new FOV to client before the client drew the previous one
                            }
                            else if (args[0].equals(Const.ATTACK)) { // ATTACK
                                // Attack command only gets sent when player is on the game screen so server doesn't need to worry about if they are in game or not
                                Player player = playerHashMap.get(this);
                                if(player.attackReady() && !(player.downed()) && player.alive()){ 
                                    player.attack(game.enemyHandler.enemies);
                                }
                            }
                            else if (args[0].equals(Const.SPECTATE)) { // SPECTATE playerName    
                                String playerName = args[1];
                                Player spectatingPlayer;
                                for(Player player: players){
                                    if(player.name().equals(playerName)){
                                        spectatingPlayer = player;
                                        this.currentPlayer = spectatingPlayer;
                                        break;
                                    }
                                }
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
        private void removePlayer(boolean alive){ // the alive boolean is to later tell the server if the player left because of the heartbeat or the client lfet themselves
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
        public String getAbilities(){
            if(ability1 != null && ability2 != null && ultimate != null){
                return this.ability1 + " " + this.ability2 + " " + this.ultimate;
            }
            return null;
        }
        private boolean ready(){
            return this.ready;
        }
        private void stopCountdown(){
            if(countdown != null){
                countdown.interrupt();
                countdown = null;
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
                        if(!(playerRemoved)){
                            playerHandler.removePlayer(false);
                        }
                        break;
                    }
                }
            }
        }
    }
    class Countdown extends Thread{ // This class is used when all players readied up this class waits and starts the game after a certian amount of time hahs pased without a player unreadying
        Countdown() {}
        public void run() {
            try {
                locked = true;
                Thread.sleep(Const.COUNTDOWN);
            } catch (Exception e) {}
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
                game.enemyHandler.start();
            }else if (countdown == null){
                System.out.println("Countdown stopped");
            }
        }
    }
    class Bleeder extends Thread { // This thread gets made when the player is downed and is dying
        private PlayerHandler player;
        private PlayerReviver reviver;
        Bleeder(PlayerHandler player) {
            this.player = player;
            this.start();
        }
        public void run() {
            int counter = 0;
            while(Const.BLEEDING_OUT_TIME / 10 != counter && playerHashMap.get(player).getHealth() != Const.PLAYER_MAX_HEALTH){
                try {
                    Thread.sleep(10);
                } catch (Exception e) {}
                for(Player currentPlayer: players){
                    if(currentPlayer.getHitbox().intersects(playerHashMap.get(player).getHitbox()) && !(currentPlayer.name().equals(playerHashMap.get(player).name())) && reviver == null 
                        && playerHashMap.get(player).alive() && (playerHashMap.get(player).downed())){
                        reviver = new PlayerReviver(player, this);  
                    }
                }
                if(reviver == null){ // If player isn't being revived add to the timer
                    counter++;
                }
            }
            Player currentPlayer = playerHashMap.get(player);
            if(currentPlayer.getHealth() != Const.PLAYER_MAX_HEALTH && currentPlayer.downed()){
                currentPlayer.die();
                boolean allPlayersDead = true;
                for(PlayerHandler playerHandler: playerHandlers){
                    playerHandler.sHandler.print(Const.DIED + " " + currentPlayer.name());
                    if(playerHashMap.get(playerHandler).alive()){
                        allPlayersDead = false;
                    }
                }
                player.sHandler.print(Const.DIE);
                if(allPlayersDead){ // Ending the game
                    endGame(false);
                }
            }
        }
    }
    class PlayerReviver extends Thread { // This thread gets made when the player is downed and is dying
        private PlayerHandler player;
        private Bleeder bleeder;
        PlayerReviver(PlayerHandler player, Bleeder bleeder) {
            this.player = player;
            this.bleeder = bleeder;
            this.start();
        }
        public void run() {
            boolean reviving = true;
            Player currentPlayer = playerHashMap.get(player);
            do {
                reviving = false;
                try {
                    Thread.sleep(Const.REVIVE_INTERVAL);
                } catch (Exception e) {}
                currentPlayer.heal(Const.PLAYER_MAX_HEALTH / 10);
                for(PlayerHandler playerHandler: playerHandlers){ // Checking if there is someone still reviving the player
                    playerHandler.sHandler.print(Const.PLAYER + " " + currentPlayer.name() + " " + currentPlayer.getX() + " " + currentPlayer.getY() + " " + currentPlayer.getDirection() + " " + currentPlayer.getHealth());
                    Player otherPlayer = playerHashMap.get(playerHandler);
                    if(currentPlayer.getHitbox().intersects(otherPlayer.getHitbox()) && !(otherPlayer.name().equals(currentPlayer.name()) && otherPlayer.alive() && !(otherPlayer.downed()))){
                        reviving = true;
                    }
                }
            }
            while(reviving && currentPlayer.getHealth() != Const.PLAYER_MAX_HEALTH);
            if(currentPlayer.getHealth() >= Const.PLAYER_MAX_HEALTH){
                System.out.println("player revived");
                for(PlayerHandler playerHandler: playerHandlers){ // Checking if there is someone still reviving the player
                    playerHandler.sHandler.print(Const.REVIVED + " " + currentPlayer.name());
                }
                currentPlayer.setDown(false);
            }else{
                bleeder.reviver = null;
            }
        }
    }
    private class Game{ // Class contains maze and enemyHandlers, can be regenerated
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
        }
    
        public char[][] constructMaze(){
            // Generating start and end
            // Start should not be generated on on edge or corner
            this.start.setX((int)(Math.random() * (this.cols - 2) + 1)); // x
            this.start.setY((int)(Math.random() * (this.rows - 2) + 1)); // y
            maze[this.start.y()][this.start.x()] = Const.START_TILE;
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
                    else if(directionChoice == 1){
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
                    else if(directionChoice == 1){ 
                        int direction = yDistance / Math.abs(yDistance); // Using absolute value to get -1 if direction was negative
                        currentPoint.setY(currentPoint.y() + direction);
                    }
                    currentDistance--;
                }
                visitQueue.add(new Coordinate(currentPoint.x(), currentPoint.y())); // Will be used later
                if(currentDistance == 1){endReached = true;} // Not going right up to the end because the end will be increased in size later
            }
            // This code segment will open up the maze adding more paths randomly potientially creating an additonal solution to the maze and hiding the first solution
            Coordinate[] directions = {new Coordinate(0,-1), new Coordinate(1,0), new Coordinate(0,1), new Coordinate(-1,0)}; // Up, Right, Down, Left
            while(!(visitQueue.isEmpty())){
                currentPoint = visitQueue.poll();
                maze[currentPoint.y()][currentPoint.x()] = Const.PATH;
                for(Coordinate direction: directions){
                    Coordinate nextPoint = new Coordinate(currentPoint.x() + direction.x(), currentPoint.y() + direction.y());
                    if(nextPoint.x() >= 0 && nextPoint.x() < this.cols && nextPoint.y() >= 0 && nextPoint.y() < this.rows && 
                        maze[nextPoint.y()][nextPoint.x()] == 0){
                        int tileChoice = (int)(Math.random() * (100 + 1)); // 1-Const.PATH_CHANCE(inclusive) = Path, Const.PATh_CHANCe - 100 = Wall
                        if(tileChoice >=1 && tileChoice <= Const.PATH_CHANCE){
                            visitQueue.add(nextPoint);
                        }else if(tileChoice > Const.PATH_CHANCE && tileChoice <= 100){
                            maze[nextPoint.y()][nextPoint.x()] = Const.WALL;
                        }
                    }
                }
            }
            // Making start and end areas larger
            Coordinate[] adjCoordinates = {new Coordinate(1,0), new Coordinate(0,1), new Coordinate(-1,0), new Coordinate(0,-1),
                                            new Coordinate(1,1), new Coordinate(1,-1), new Coordinate(-1,1), new Coordinate(-1, -1)};
            for(Coordinate adjCoordinate: adjCoordinates){
                // Dont have to worry about adjacent coordinates being out of bounds for start because start is never on the very edge
                maze[start.y() + adjCoordinate.y()][start.x() + adjCoordinate.x()] = Const.START_TILE; 
                if(end.x() + adjCoordinate.x() >= 0 && end.x() + adjCoordinate.x() < cols && end.y() + adjCoordinate.y() >= 0 && end.y() + adjCoordinate.y() < rows){
                    maze[end.y() + adjCoordinate.y()][end.x() + adjCoordinate.x()] = Const.END_TILE; 
                }
            }
            /* This code segment can be uncommented to print the maze into the console so the server can see
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
            }*/
            this.enemyHandler = new EnemyHandler(Const.MAX_ENEMIES + (round - 1) * Const.ENEMIES_INCREMENT);
            return this.maze;
        }

        public int[] calculateFov(Player player){
            int[] fov = new int[4]; // 0 - number of rows in fov, 1 - number of cols in fov, 2 - bottomRightX, 3 - bottomRightY
            int playerXTile = (player.getX() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
            int playerYTile = (player.getY() + Const.PLAYER_DIMENSIONS / 2) / Const.TILE_DIMENSIONS;
            // Using Math.min and Math.max to make sure if player is close to edge no array out of bounds exception happens
            // Player can maximum seen 9 tiles wide or 7 tiles high. Perfect situation if all tiles are fully show is an 8x6 tileset (1200/150)x(900/150)
            int topX = Math.max(0, playerXTile - 4);
            int topY = Math.max(0, playerYTile - 3);
            int botX = Math.min(rows - 1, playerXTile + 5); // 1 added more so that a 9x7 can be drawn
            int botY = Math.min(cols - 1, playerYTile + 4); // 1 added more so that a 9x7 can be drawn
            int cols = botX - topX + 1; // Adding 1 to include the right most col so that a 9x7 can be drawn
            int rows = botY - topY + 1; // Adding 1 to include the lower most row so that a 9x7 can be drawn
            fov[0] = rows; fov[1] = cols; fov[2] = topX; fov[3] = topY;
            return fov;
        }
    
        public void spawnPlayers(){ // This will spawn the players on the 4 squares on the corner of the spawn box going from top left to bottom right
            Coordinate topLeftStart = new Coordinate(this.start.x() - 1, this.start.y() - 1);
            for(int y = 0, playerCount = 0; y < 2 && playerCount < players.size(); y++){
                for(int x = 0; x < 2 && playerCount < players.size(); x++, playerCount++){
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
                while(enemies.size() < this.maxEnemies){
                    Enemy newEnemy = addEnemy();
                    for(PlayerHandler playerHandler: playerHandlers){ // When lobby is generated this code won't run but this is useful for after a new round is reached
                        playerHandler.sHandler.print(Const.NEWE + " " + newEnemy.getX() + " " + newEnemy.getY() + " " + newEnemy.getHealth());
                    }
                }
                System.out.println(enemies.size());
            }
            public void run(){
                Stack<Integer> deadEnemies = new Stack<Integer>();
                int attackTimer = 0;
                while(playing){
                    try{
                        Thread.sleep(15); 
                    }catch(Exception e){}
                    int idCounter = 0;
                    for(Enemy enemy: enemies){
                        if(enemy.getHealth() > 0 && !(enemy.inWall())){
                            boolean enemyMoved = enemy.move(players);
                            //System.out.print(" enemy moved " + enemyMoved);
                            if(enemyMoved){
                                for(PlayerHandler playerHandler: playerHandlers){
                                    playerHandler.sHandler.print(Const.ENEMY + " " + idCounter + " " + enemy.getX() + " " + enemy.getY() + " " + enemy.getHealth());
                                }
                            }
                            if(attackTimer >= Const.ENEMY_ATTACKS_SPEED){
                                for(Player player: players){
                                    if(enemy.getHitbox().intersects(player.getHitbox())){
                                        player.damage(enemy.getDamage());
                                    }
                                }
                            }
                        }else{
                            deadEnemies.add(idCounter);
                        }
                        idCounter++;
                    }
                    if(attackTimer < Const.ENEMY_ATTACKS_SPEED){attackTimer = attackTimer + (enemies.size());} // Adding based on enemy size to prevent large enemy games to have enemies attacking slowly
                    else{attackTimer = 0;}
                    while(!(deadEnemies.isEmpty())){ // Remove enemy and add a new one in its place
                        int id = deadEnemies.pop();
                        enemies.remove(id);
                        Enemy newEnemy = addEnemy();
                        for(PlayerHandler playerHandler: playerHandlers){
                            playerHandler.sHandler.print(Const.KILLEDE + " " + id);
                            playerHandler.sHandler.print(Const.NEWE + " " + newEnemy.getX() + " " + newEnemy.getY() + " " + newEnemy.getHealth());
                        }
                    }
                    try{
                        Thread.sleep(Const.ENEMY_MOVEMENT_BREAK); // Making it so enemies dont move at lightning speed
                    }catch(Exception e){}

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
                int enemyX = enemyXSquare * Const.TILE_DIMENSIONS + (Const.TILE_DIMENSIONS - Const.ENEMY_DIMENSIONS)/2;
                int enemyY = enemyYSquare * Const.TILE_DIMENSIONS + (Const.TILE_DIMENSIONS - Const.ENEMY_DIMENSIONS)/2;
                Enemy enemy = new Enemy(enemyX, enemyY, maze, round);
                enemies.add(enemy);
                System.out.println("enemy added");
                return enemy;
            }
        }
        private class Coordinate{ // Class made to make sure coordinates can be added to queue because sometimes certian coordinates wouldn't be added because there were "duplicates" due to the nature of Integer
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