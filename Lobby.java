import java.net.*;
import java.util.*;
import java.io.*;

public class Lobby extends Thread{
    private String name;
    private int playerCount;
    private ArrayList<PlayerHandler> playerHandlers;
    private ArrayList<Player> players;
    private HashMap<PlayerHandler, Player> playerHashMap;
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
        Player player = new Player(playerName);
        this.playerHandlers.add(playerHandler);
        this.players.add(player);
        this.playerHashMap.put(playerHandler, player);
        playerCount++;
        playerHandler.start();
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
        String ability1;
        String ability2;
        String ultimate;
        Server.PlayerHandler sHandler;
        
        public PlayerHandler(Socket socket, Server.PlayerHandler sHandler) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
            this.sHandler = sHandler;
        }
        // Whether the socket is dead
        public boolean isDead() {
            return this.socket == null || this.output == null || this.input == null;
        }
        // Kill the player's ball
        public void kill() {
            //this.ball = null;
            this.print("DIE");
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
                                    this.print("ERROR Player already has joined game");
                                } else {
                                    Color color = new Color(Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
                                    String name = "";
                                    for (int i = 4; i < args.length; i++) {
                                        name += args[i] + " ";
                                    }
                                    this.ball = createBall(this, color, name);
                                    this.print("JOIN " + this.ball.getId() + " " + this.ball.getX() + " " + this.ball.getY() + " " + this.ball.getRadius());
                                }
                            }
                            // PING
                            // TURN {degree}
                            else if (args[0].equals("TURN")) {
                                if (this.hasBall()) {
                                    this.ball.setAngle(Integer.valueOf(args[1]));
                                } else {
                                    this.print("ERROR Player has not joined the game");
                                }
                            }*/
                            if (args[0].equals(Const.PING)) {
                                this.alive = true;
                            }
                            else if (args[0].equals(Const.SELECTED)) { // LEAVE    
                                this.print(Const.MY_ABILITIES);
                            }
                            else if (args[0].equals(Const.MY_ABILITIES)) { // LEAVE    
                                String ability1 = args[1];
                                String ability2 = args[2];
                                String ultimate = args[3];
                                if(ability1 != null && ability2 != null && ultimate != null){
                                    for(PlayerHandler player: playerHandlers){
                                        this.print(Const.ABILITIES + " " + playerHashMap.get(player).name() + " " + ability1 + " " + ability2 + " " + ultimate);
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
                            this.print("LOBBY ERROR invalid arguments");
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
            players.remove(playerHashMap.get(this));
            playerHandlers.remove(this);
            playerCount--;
            if (players.size() == 0){
                hasPlayers = false;
            }
            else{
                for (PlayerHandler otherPlayers: playerHandlers) {
                    otherPlayers.print(Const.REMOVEP + " " + playerName);
                }
            }
            this.alive = alive; 
            this.heartbeat.playerRemoved = true;
            this.sHandler.leaveLobby(alive);
            this.print(Const.LEAVE);
            this.close();
        }
        public void print(String text) {
            if (this.isDead()) {
                System.out.println("Dead socket, message send failure");
                return;
            };
            output.println(text);
            output.flush();
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