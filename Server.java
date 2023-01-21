/**
 * Final Game Server Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This is the Server class of the game
 * This class basically encompasses all of the lobbies where the actual games happens
 * This is where the program is started
 */

//imports for network communication
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {    
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter output;
    BufferedReader input;

    HashSet<PlayerHandler> handlers;
    ArrayList<Lobby> lobbies;
    HashMap<Integer, PlayerHandler> handlerMap;
    
    public static void main(String[] args) throws Exception{ 
        Server game = new Server();
        game.go();
    }
    
    public void go() throws Exception{ 
        //create a socket with the local IP address and wait for connection request       
        System.out.println("Launching server...");
        serverSocket = new ServerSocket(Const.PORT);  //create and bind a socket
        System.out.println("Connected at port " + serverSocket.getLocalPort());
        this.setup();
        // Create a thread that updates the game state
        while (true) {
            clientSocket = serverSocket.accept(); //wait for connection request
            System.out.println("Player connected");
            PlayerHandler handler = new PlayerHandler(clientSocket);
            handlers.add(handler);
            handler.start();
        }
    }
    public void setup() {
        this.handlers = new HashSet<PlayerHandler>();
        this.lobbies = new ArrayList<Lobby>();
        this.handlerMap = new HashMap<Integer, PlayerHandler>();
    }
    public void cleanSockets() {
        for (PlayerHandler handler: this.handlers) {
            if (handler.isDead()) {
                System.out.println("removed");
                this.handlers.remove(handler);
            }
        }
    }
//------------------------------------------------------------------------------
    class PlayerHandler extends Thread { 
        private Socket socket;
        private PrintWriter output;
        private BufferedReader input;
        public boolean inLobby = false;
        private Heartbeat heartbeat;
        private boolean alive = true;
        private String lobbyName;
        private boolean newLobby; // This variable makes it so that when the player sends the name command something different can happen depending on the state
        
        public PlayerHandler(Socket socket){ 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
            this.lobbyName = "";
            this.newLobby = false;
        }
        // Whether the socket is dead
        public boolean isDead(){
            return this.socket == null || this.output == null || this.input == null;
        }
        public void run(){
            try{
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream());
                String msg;
                while (true){
                    //receive a message from the client
                    msg = null;
                    /*try {Thread.sleep(1000 / Const.TPS);} catch (Exception e) {};
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    System.out.print("");
                    if(!(inLobby)){ // Server doesn't need to listen to anything but this while the player is in a lobby (besides PING). After the server gets this message it means the player isn't in a lobby
                        try {
                            msg = input.readLine();
                        } catch (Exception e) {
                            this.close();
                            break;
                        }
                    }
                    if (msg != null) {
                        System.out.println("Message from the client: " + msg);
                        String[] args = msg.split(" ");
                        try {
                            if (args[0].equals(Const.PING)) { // PING
                                this.alive = true;
                            }
                            if (args[0].equals(Const.LOBBIES_LIST)) { // LOBBIES
                                this.print(Const.CLEAR_LOBBIES);
                                for (Lobby lobby: lobbies) {
                                    if((lobby.playerCount() != 4 || lobby.playerCount() != 0) && !(lobby.locked())){
                                        this.print(Const.LOBBY + " " + lobby.name() + " " + lobby.playerCount());
                                    }
                                }
                                this.print(Const.LOBBY_SELECT);
                            } else if (args[0].equals(Const.NEW_LOBBY)) { // NEW
                                this.print(Const.NAME + " " + Const.NEW_LOBBY);
                                newLobby = true;
                            }else if (args[0].equals(Const.JOIN_LOBBY)) { // JOIN lobbyName playerName
                                String lobbyName = args[1];
                                this.print(Const.NAME + " " + Const.JOIN_LOBBY + " " + lobbyName);
                            }else if (args[0].equals(Const.NAME)) { // NAME lobbyName playerName 
                                lobbyName = args[1]; // Lobby name is the (players name)'s lobby
                                if(newLobby){
                                    boolean nameRepeated = true;
                                    int repeatCounter = 2; // If there is a lobby that has the same name it will add the value of this number to the end so that the lobby names don't match Ex. Ilya2's Lobby
                                    while(nameRepeated){
                                        nameRepeated = false;
                                        for (Lobby lobby: lobbies) {
                                            if(lobby.name().equals(lobbyName + "'s")){
                                                nameRepeated = true;
                                                lobbyName = args[1] + repeatCounter; 
                                                repeatCounter++;
                                                break;
                                            }
                                        }
                                    }
                                    lobbyName = lobbyName + "'s";
                                    Lobby newLobby = new Lobby(lobbyName);
                                    this.print(Const.JOINED + " " + lobbyName);
                                    lobbies.add(newLobby);
                                    inLobby = true;
                                    newLobby.addPlayer(clientSocket, this, args[1]); // Even though the lobby name might now have a number that doesn't mean the actual players name should change
                                    newLobby.start();
                                }else{
                                    String playerName = args[2];
                                    for (Lobby lobby: lobbies) {
                                        if(lobby.name().equals(lobbyName) && !(lobby.locked())){
                                            this.print(Const.JOINED + " " + lobbyName);
                                            inLobby = true;
                                            lobby.addPlayer(clientSocket, this, playerName);
                                            break;
                                        }
                                    }
                                    if(inLobby == false){ // This if statement will happen if the server couldn't find a lobby with the name that matches the one the client gave
                                        this.print(Const.NO_LOBBY);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            this.print("SERVER ERROR invalid arguments");
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
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
        public void leaveLobby(boolean alive){
            inLobby = false;
            newLobby = false;
            this.alive = alive;
            for (Lobby lobby: lobbies){ // Making sure that there are no empty lobbies 
                if(lobby.name().equals(this.lobbyName) && lobby.playerCount() == 0){
                    lobbies.remove(lobby);
                    break;
                }
            }
            if(!(this.alive)){
                this.close();
            }
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
                    if(!(playerHandler.inLobby)){
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
}