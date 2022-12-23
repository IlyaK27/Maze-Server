//imports for network communication
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;

public class Game {    
    ServerSocket serverSocket;
    Socket clientSocket;
    PrintWriter output;
    BufferedReader input;
    int clientCounter = 0;
    public int idCounter = Integer.MIN_VALUE;

    ThreadMachine threadMachine;

    HashSet<PlayerHandler> handlers;
    HashMap<Integer, Ball> balls;
    HashMap<Integer, Pellet> pellets;
    // Ball id to playerHandler
    HashMap<Integer, PlayerHandler> handlerMap;

    public final Object ballLock = new Object();
    public final Object pelletLock = new Object();
    
    public static void main(String[] args) throws Exception{ 
        Game game = new Game();
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
            clientCounter = clientCounter + 1;
            System.out.println("Player " + clientCounter + " connected");
            PlayerHandler handler = new PlayerHandler(clientSocket);
            handlers.add(handler);
            handler.start();
            synchronized (ballLock) {
                for (Integer id: this.balls.keySet()) {
                    this.printNew(this.balls.get(id), handler);
                }
            }
        }
    }
    public void setup() {
        this.handlers = new HashSet<PlayerHandler>();
        this.balls = new HashMap<Integer, Ball>();
        this.pellets = new HashMap<Integer, Pellet>();
        this.handlerMap = new HashMap<Integer, PlayerHandler>();
        for (int i = 0; i < Const.START_PELLETS; i++) {
            createPellet();
        }
        this.threadMachine = new ThreadMachine(this);
        this.threadMachine.start();
    }
    public void printNew(Ball ball, PlayerHandler handler) {
        handler.print("NEW " + ball.getId() + " " + ball.getColor().getRed() + " " + ball.getColor().getGreen() + " " + ball.getColor().getBlue() + " " + ball.getName());
    }
    public void createPellet() {
        Pellet pellet = new Pellet(idCounter++, (int)(Math.random() * Const.WIDTH), (int)(Math.random() * Const.HEIGHT));
        synchronized (ballLock) {
            this.pellets.put(pellet.getId(), pellet);
        }
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
    }
    public void killPellet(int id) {
        synchronized (pelletLock) {
            this.pellets.remove(id);
        }
        for (Integer handlerId: this.handlerMap.keySet()) {
            this.handlerMap.get(handlerId).print("REMOVE " + id + " pellet");
        }
    }
    public void killBall(int id) {
        synchronized (ballLock) {
            this.balls.remove(id);
        }
        this.handlerMap.get(id).kill();
        this.handlerMap.remove(id);
        for (PlayerHandler handler: this.handlers) {
            handler.print("REMOVE " + id + " ball");
        }
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
        Socket socket;
        PrintWriter output;
        BufferedReader input;
        public boolean alive = true;
        Heartbeat heartbeat;
        Ball ball;
        
        public PlayerHandler(Socket socket) { 
            this.socket = socket;
            this.heartbeat = new Heartbeat(this);
            this.heartbeat.start();
        }
        public boolean hasBall() {
            return this.ball != null;
        }
        // Whether the socket is dead
        public boolean isDead() {
            return this.socket == null || this.output == null || this.input == null;
        }
        // Kill the player's ball
        public void kill() {
            this.ball = null;
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
                            // JOIN {red} {green} {blue} {*name}
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
            if (this.hasBall()) {
                killBall(this.ball.getId());
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
    class ThreadMachine {
        Game game;
        PelletThread pelletThread;
        BallThread ballThread;
        SpeakerThread speakerThread;
        SocketCleanerThread socketCleanerThread;
        AnalyticsThread analyticsThread;
        ThreadMachine(Game game) {
            this.game = game;
            this.pelletThread = new PelletThread(this.game);
            this.ballThread = new BallThread(this.game);
            this.speakerThread = new SpeakerThread(this.game);
            this.socketCleanerThread = new SocketCleanerThread(this.game);
            this.analyticsThread = new AnalyticsThread(this.game);
        }
        public void start() {
            this.pelletThread.start();
            this.ballThread.start();
            this.speakerThread.start();
            this.socketCleanerThread.start();
            this.analyticsThread.start();
        }
        class PelletThread extends Thread {
            Game game;
            PelletThread(Game game) {
                this.game = game;
            }
            public void run() {
                while (true) {
                    try {Thread.sleep(Const.PELLET_SPAWN_RATE);} catch (Exception e) {};
                    this.game.createPellet();
                }
            }
        }
        class BallThread extends Thread {
            Game game;
            BallThread(Game game) {
                this.game = game;
            }
            public void run() {
                while (true) {
                    try {
                        try {Thread.sleep(200);} catch (Exception e) {};
                        // Move balls
                        synchronized (ballLock) {
                            for (Integer id: this.game.balls.keySet()) {
                                Ball ball = this.game.balls.get(id);
                                ball.setX(ball.getX() + Const.xChange(ball.getAngle(), Const.speed(ball.getRadius())));
                                ball.setY(ball.getY() + Const.yChange(ball.getAngle(), Const.speed(ball.getRadius())));
                            }
                        }
                        // Eat pellets and balls
                        synchronized (ballLock) {
                            for (Integer id: this.game.balls.keySet()) {
                                Ball ball = this.game.balls.get(id);
                                HashSet<Integer> removals = new HashSet<Integer>();
                                synchronized (pelletLock) {
                                    for (Integer pelletId: this.game.pellets.keySet()) {
                                        if (ball.intersects(this.game.pellets.get(pelletId))) {
                                            removals.add(pelletId);
                                            ball.setRadius(ball.getRadius() + 3);
                                        }
                                    }
                                }
                                for (Integer i: removals) {
                                    this.game.killPellet(i);
                                }
                                removals.clear();
                                for (Integer ballId: this.game.balls.keySet()) {
                                    if (ball.getId() == ballId) {
                                        continue;
                                    }
                                    if (ball.eats(this.game.balls.get(ballId))) {
                                        removals.add(ballId);
                                        ball.setRadius(ball.getRadius() + this.game.balls.get(ballId).getRadius() / 2);
                                    }
                                }
                                for (Integer i: removals) {
                                    this.game.killBall(i);
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
            Game game;
            SpeakerThread(Game game) {
                this.game = game;
            }
            public void run() {
                while (true) {
                    try {
                        for (PlayerHandler handler: this.game.handlerMap.values()) {
                            // Send info about the player's own ball
                            handler.print("MOVE " + handler.ball.getX() + " " + handler.ball.getY() + " " + handler.ball.getRadius());
                            // Send pellet info
                            synchronized (pelletLock) {
                                for (Pellet pellet: this.game.pellets.values()) {
                                    if (handler.ball.distance(pellet) <= Const.CLIENT_VIEW_RADIUS) {
                                        handler.print("PELLET " + pellet.getId() + " " + pellet.getX() + " " + pellet.getY() + " " + pellet.getRadius() + " " + pellet.getColor().getRed() + " " + pellet.getColor().getGreen() + " " + pellet.getColor().getBlue());
                                    }
                                }
                            }
                            // Send ball info
                            synchronized (ballLock) {
                                for (Ball ball: this.game.balls.values()) {
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
            Game game;
            SocketCleanerThread(Game game) {
                this.game = game;
            }
            public void run() {
                while (true) {
                    try {
                        this.game.cleanSockets();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("Clean!");
                    try {Thread.sleep(5000);} catch (Exception e) {};
                }
            }
        }
        class AnalyticsThread extends Thread {
            Game game;
            AnalyticsThread(Game game) {
                this.game = game;
            }
            public void run() {
                while (true) {
                    System.out.println("------------------------------------------------------------------------------");
                    // How many players connected
                    System.out.println(this.game.handlers.size() + " players connected");
                    // How many balls
                    System.out.println(this.game.balls.size() + " balls");
                    // How many pellets
                    System.out.println(this.game.pellets.size() + " pellets");
                    // HandlerMap size
                    System.out.println(this.game.handlerMap.size() + " HandlerMap size");
                    System.out.println("------------------------------------------------------------------------------");
                    try {Thread.sleep(5000);} catch (Exception e) {};
                }
            }
        }
    }
}