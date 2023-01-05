import java.net.*;
import java.util.*;
import java.io.*;

public class Lobby extends Thread{
    private String name;
    private ArrayList<PlayerHandler> playerHandlers;
    private ArrayList<Player> players;
    private HashMap<PlayerHandler, Player> playerHashMap;
    private Game game;
    private int round; 
    private boolean hasPlayers;

    public Lobby(String name){
        this.name = name;
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
    public void addPlayer(Socket socket, String playerName){
        PlayerHandler playerHandler = new PlayerHandler(socket);
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
    }
    private void removePlayer(PlayerHandler player){
        this.players.remove(playerHashMap.get(player));
        this.playerHandlers.remove(player);
        if (players.size() == 0){
            hasPlayers = false;
        }
    }
    public int playerCount(){
        return players.size();
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
        
        public PlayerHandler(Socket socket) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
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
                        System.out.println("Message from the client: " + msg);
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
                            else if (args[0].equals("PING")) {
                                this.alive = true;
                            }
                            // TURN {degree}
                            else if (args[0].equals("TURN")) {
                                if (this.hasBall()) {
                                    this.ball.setAngle(Integer.valueOf(args[1]));
                                } else {
                                    this.print("ERROR Player has not joined the game");
                                }
                            }*/
                        } catch (Exception e) {
                            this.print("ERROR invalid arguments");
                            e.printStackTrace();
                        }
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
                this.close();
            }
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
            this.heartbeat.interrupt();
            try {
                input.close();
                output.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.socket = null;
        }
        // If there is no "heartbeat" from the client for 60 seconds, assume the connection has failed
        class Heartbeat extends Thread {
            PlayerHandler playerHandler;

            Heartbeat(PlayerHandler playerHandler) {
                this.playerHandler = playerHandler;
            }
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Const.HEARTBEAT_RATE);
                    } catch (Exception e) {}
                    System.out.println("Check for heartbeat");
                    if (this.playerHandler.alive) {
                        this.playerHandler.alive = false;
                    } else {
                        System.out.println("test");
                        this.playerHandler.close();
                        break;
                    }
                }
            }
        }
    }
}
