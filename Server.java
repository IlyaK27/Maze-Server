//imports for network communication
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;

public class Server {    
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter output;
    BufferedReader input;

    //ThreadMachine threadMachine;

    HashSet<PlayerHandler> handlers;
    //HashMap<Integer, Ball> balls;
    ArrayList<Lobby> lobbies;
    // Ball id to playerHandler
    HashMap<Integer, PlayerHandler> handlerMap;

    //public final Object ballLock = new Object();
    
    public static void main(String[] args) throws Exception{ 
        Server game = new Server();
        game.go();
    }
    
    public void go() throws Exception{ 
        //create a socket with the local IP address and wait for connection request       
        System.out.println("Launching server...");
        serverSocket = new ServerSocket(Const.PORT);                //create and bind a socket
        System.out.println("Connected at port " + serverSocket.getLocalPort());
        this.setup();
        // Create a thread that updates the game state
        while (true) {
            clientSocket = serverSocket.accept();             //wait for connection request
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
        //this.threadMachine = new ThreadMachine(this);
        //this.threadMachine.start();
    }
    /*public void printNew(Ball ball, PlayerHandler handler) {
        handler.print("NEW " + ball.getId() + " " + ball.getColor().getRed() + " " + ball.getColor().getGreen() + " " + ball.getColor().getBlue() + " " + ball.getName());
    }
    public Ball createBall(PlayerHandler handler, Color color, String name) {
        Ball ball = new Ball(idCounter++, (int)(Math.random() * Const.WIDTH), (int)(Math.random() * Const.HEIGHT), (int)(Math.random() * 360), color, name);
        synchronized (ballLock) {
            this.balls.put(ball.getId(), ball);
        }
        for (PlayerHandler i: this.handlers) {
            if (handler.equals(i)) {
                continue;
            }
            this.printNew(ball, i);
        }
        this.handlerMap.put(ball.getId(), handler);
        return ball;
    }*/
    /*public void killPellet(int id) {
        synchronized (pelletLock) {
            this.pellets.remove(id);
        }
        for (Integer handlerId: this.handlerMap.keySet()) {
            this.handlerMap.get(handlerId).print("REMOVE " + id + " pellet");
        }
    }*/
    /*public void killBall(int id) {
        synchronized (ballLock) {
            this.balls.remove(id);
        }
        this.handlerMap.get(id).kill();
        this.handlerMap.remove(id);
        for (PlayerHandler handler: this.handlers) {
            handler.print("REMOVE " + id + " ball");
        }
    }*/
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
        Socket socket;
        PrintWriter output;
        BufferedReader input;
        public boolean inLobby = false;
        Heartbeat heartbeat;
        private boolean alive = true;
        
        public PlayerHandler(Socket socket) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
        }
        // Whether the socket is dead
        public boolean isDead() {
            return this.socket == null || this.output == null || this.input == null;
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
                            // JOIN {red} {green} {blue} {*name}
                            if (args[0].equals("PING")) {
                                this.alive = true;
                            }
                            else if(!(inLobby)){
                                if (args[0].equals("LOBBIES")) {
                                    for (Lobby lobby: lobbies) {
                                        this.print(Const.LOBBY + " " + lobby.name() + " " + lobby.playerCount());
                                    }
                                    this.print(Const.LOBBY_SELECT);
                                } else if (args[0].equals("NEW")) {
                                    String lobbyName = args[1]; // Lobby name is the (players name)'s lobby
                                    boolean nameRepeated = true;
                                    int repeatCounter = 2; // If there is a lobby that has the same name it will add the value of this number to the end so that the lobby names don't match Ex. Ilya2's Lobby
                                    while(nameRepeated){
                                        nameRepeated = false;
                                        for (Lobby lobby: lobbies) {
                                            if(lobby.name().equals(lobbyName)){
                                                nameRepeated = true;
                                                lobbyName = args[1] + repeatCounter; 
                                                repeatCounter++;
                                                break;
                                            }
                                        }
                                    }
                                    lobbyName = lobbyName + ",s Lobby";
                                    Lobby newLobby = new Lobby(lobbyName);
                                    newLobby.addPlayer(this.socket, args[1]); // Even though the lobby name might now have a number that doesn't mean the actual players name should change
                                    lobbies.add(newLobby);
                                    inLobby = true;
                                } else if (args[0].equals("JOIN")) {
                                    String lobbyName = args[1];
                                    String playerName = args[2];
                                    for (Lobby lobby: lobbies) {
                                        if(lobby.name().equals(lobbyName)){
                                            inLobby = true;
                                            lobby.addPlayer(clientSocket, playerName);
                                            break;
                                        }
                                    }
                                    if(inLobby == false){ // This if statement will happen if the server couldn't find a lobby with the name that matches the one the client gave
                                        this.print(Const.NO_LOBBY);
                                    }
                                }
                            } else {
                                if (args[0].equals("LEAVE")) { // Server doesn't need to listen to anything but this while the player is in a lobby. After the server gets this message it means the player isn't in a lobby
                                    inLobby = false;
                                }
                            }
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
    /*class ThreadMachine {
        Server server;
        PelletThread pelletThread;
        BallThread ballThread;
        SpeakerThread speakerThread;
        SocketCleanerThread socketCleanerThread;
        AnalyticsThread analyticsThread;
        ThreadMachine(Server server) {
            this.server = server;
            this.pelletThread = new PelletThread(this.server);
            this.ballThread = new BallThread(this.server);
            this.speakerThread = new SpeakerThread(this.server);
            this.socketCleanerThread = new SocketCleanerThread(this.server);
            this.analyticsThread = new AnalyticsThread(this.server);
        }
        public void start() {
            this.pelletThread.start();
            this.ballThread.start();
            this.speakerThread.start();
            this.socketCleanerThread.start();
            this.analyticsThread.start();
        }
        class PelletThread extends Thread {
            Server server;
            PelletThread(Server server) {
                this.server = server;
            }
            public void run() {
                while (true) {
                    try {Thread.sleep(Const.PELLET_SPAWN_RATE);} catch (Exception e) {};
                    this.server.createPellet();
                }
            }
        }
        class BallThread extends Thread {
            Server server;
            BallThread(Server server) {
                this.server = server;
            }
            public void run() {
                while (true) {
                    try {
                        try {Thread.sleep(200);} catch (Exception e) {};
                        // Move balls
                        synchronized (ballLock) {
                            for (Integer id: this.server.balls.keySet()) {
                                Ball ball = this.server.balls.get(id);
                                ball.setX(ball.getX() + Const.xChange(ball.getAngle(), Const.speed(ball.getRadius())));
                                ball.setY(ball.getY() + Const.yChange(ball.getAngle(), Const.speed(ball.getRadius())));
                            }
                        }
                        // Eat pellets and balls
                        synchronized (ballLock) {
                            for (Integer id: this.server.balls.keySet()) {
                                Ball ball = this.server.balls.get(id);
                                HashSet<Integer> removals = new HashSet<Integer>();
                                synchronized (pelletLock) {
                                    for (Integer pelletId: this.server.pellets.keySet()) {
                                        if (ball.intersects(this.server.pellets.get(pelletId))) {
                                            removals.add(pelletId);
                                            ball.setRadius(ball.getRadius() + 3);
                                        }
                                    }
                                }
                                for (Integer i: removals) {
                                    this.server.killPellet(i);
                                }
                                removals.clear();
                                for (Integer ballId: this.server.balls.keySet()) {
                                    if (ball.getId() == ballId) {
                                        continue;
                                    }
                                    if (ball.eats(this.server.balls.get(ballId))) {
                                        removals.add(ballId);
                                        ball.setRadius(ball.getRadius() + this.server.balls.get(ballId).getRadius() / 2);
                                    }
                                }
                                for (Integer i: removals) {
                                    this.server.killBall(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        class SpeakerThread extends Thread {
            Server server;
            SpeakerThread(Server server) {
                this.server = server;
            }
            public void run() {
                while (true) {
                    try {
                        for (PlayerHandler handler: this.server.handlerMap.values()) {
                            // Send info about the player's own ball
                            handler.print("MOVE " + handler.ball.getX() + " " + handler.ball.getY() + " " + handler.ball.getRadius());
                            // Send pellet info
                            synchronized (pelletLock) {
                                for (Pellet pellet: this.server.pellets.values()) {
                                    if (handler.ball.distance(pellet) <= Const.CLIENT_VIEW_RADIUS) {
                                        handler.print("PELLET " + pellet.getId() + " " + pellet.getX() + " " + pellet.getY() + " " + pellet.getRadius() + " " + pellet.getColor().getRed() + " " + pellet.getColor().getGreen() + " " + pellet.getColor().getBlue());
                                    }
                                }
                            }
                            // Send ball info
                            synchronized (ballLock) {
                                for (Ball ball: this.server.balls.values()) {
                                    if (!handler.ball.equals(ball) && handler.ball.distance(ball) <= Const.CLIENT_VIEW_RADIUS) {
                                        handler.print("BALL " + ball.getId() + " " + ball.getX() + " " + ball.getY() + " " + ball.getRadius());
                                    }
                                }
                            }
                        }
                        try {Thread.sleep(1000 / Const.TPS);} catch (Exception e) {};
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        class SocketCleanerThread extends Thread {
            Server server;
            SocketCleanerThread(Server server) {
                this.server = server;
            }
            public void run() {
                while (true) {
                    try {
                        this.server.cleanSockets();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Clean!");
                    try {Thread.sleep(5000);} catch (Exception e) {};
                }
            }
        }
        class AnalyticsThread extends Thread {
            Server server;
            AnalyticsThread(Server server) {
                this.server = server;
            }
            public void run() {
                while (true) {
                    System.out.println("------------------------------------------------------------------------------");
                    // How many players connected
                    System.out.println(this.server.handlers.size() + " players connected");
                    // How many balls
                    System.out.println(this.server.balls.size() + " balls");
                    // How many pellets
                    System.out.println(this.server.pellets.size() + " pellets");
                    // HandlerMap size
                    System.out.println(this.server.handlerMap.size() + " HandlerMap size");
                    System.out.println("------------------------------------------------------------------------------");
                    try {Thread.sleep(5000);} catch (Exception e) {};
                }
            }
        }
    }*/
}