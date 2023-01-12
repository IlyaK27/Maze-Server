import java.net.*;
import java.util.*;

import java.awt.Color;

import java.io.*;

public class Lobby extends Thread{
    private String name;
    private int playerCount;
    private ArrayList<PlayerHandler> playerHandlers;
    private ArrayList<Player> players;
    private HashMap<PlayerHandler, Player> playerHashMap;
    private Queue<Color> colors; // contains hex code of color
    private Game game;
    private int round; 
    private boolean hasPlayers;

    public Lobby(String name){
        this.name = name;
        this.playerCount = 0;
        this.hasPlayers = true;
        playerHandlers = new ArrayList<PlayerHandler>(Const.LOBBY_SIZE);
        players = new ArrayList<Player>(Const.LOBBY_SIZE);
        playerHashMap = new HashMap<PlayerHandler, Player>();
        colors = new LinkedList<Color>();
        colors.add(Color.BLUE);
        colors.add(Color.GREEN);
        colors.add(Color.YELLOW);
        colors.add(Color.ORANGE);
        this.round = 1;
        this.game = new Game(Const.STARTING_ROWS, Const.STARTING_COLS, this.round, Const.MIN_DISTANCE);
        this.game.constructMaze();
    }

    public void run(){
        while(hasPlayers){

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
        String colorHex = String.format("#%02x%02x%02x", player.color().getRed(),  player.color().getGreen(),  player.color().getBlue()); 
        for(PlayerHandler players: playerHandlers){ // Telling other players a player has joined
            System.out.println("New player for current lobby");
            players.sHandler.print(Const.NEW_PLAYER + " " + playerName + " " + colorHex); // Client will decode the hex using Color.decode() method
        }
        this.playerHandlers.add(playerHandler);
        this.players.add(player);
        this.playerHashMap.put(playerHandler, player);
        playerCount++; 
        for(PlayerHandler players: playerHandlers){ // Giving the player who just joined info on the players already in the lobby
            player = playerHashMap.get(players);
            colorHex = String.format("#%02x%02x%02x", player.color().getRed(),  player.color().getGreen(),  player.color().getBlue());
            while(playerHandler.output == null){System.out.print("");} // Sometimes playerHandler.sHandler.print happens before the input and output get initialized
            System.out.println("New player");
            playerHandler.sHandler.print(Const.NEW_PLAYER + " " + playerHashMap.get(players).name() + " " + colorHex);
            if(players.getAbilities() != null){playerHandler.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(players).name() + " " + players.getAbilities());}
        }
    }
    public int playerCount(){
        return this.playerCount;
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
        
        public PlayerHandler(Socket socket, Server.PlayerHandler sHandler) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
            this.sHandler = sHandler;
        }
        // Whether the socket is dead
        public boolean isDead() {
            System.out.println(this.socket + " " + this.output + " " + this.input);
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
                        System.out.println("Message from the clientL: " + msg);
                        String[] args = msg.split(" ");
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
                            else if (args[0].equals(Const.MY_ABILITIES)) { // LEAVE    
                                this.ability1 = args[1];
                                this.ability2 = args[2];
                                this.ultimate = args[3];
                                if(ability1 != null && ability2 != null && ultimate != null){
                                    for(PlayerHandler player: playerHandlers){
                                        if(player.equals(this)){
                                            System.out.println("EQUAL" + player.equals(this));
                                            this.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(player).name() + " " + ability1 + " " + ability2 + " " + ultimate + " ME");
                                        }
                                        else{this.sHandler.print(Const.ABILITIES + " " + playerHashMap.get(player).name() + " " + ability1 + " " + ability2 + " " + ultimate);}
                                    }
                                }
                                //playerHashMap.get(this).setAbility1(ability1);
                                //playerHashMap.get(this).setAbility2(ability2);
                                //playerHashMap.get(this).setUltimate(ultimate);
                            }
                            else if (args[0].equals(Const.LEAVE)) { // LEAVE    
                                System.out.println("Lobbyleave");
                                removePlayer(true);
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
}