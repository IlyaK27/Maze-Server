public class Const {
    public static final int PORT = 5001;
    public static final int LOBBY_SIZE = 4;
    // Map width and height
    public static final int WIDTH = 1000;
    public static final int HEIGHT = 1000;

    public static final int CLIENT_VIEW_RADIUS = 1500;

    public static final int HEARTBEAT_RATE = 60000;
    
    public static final int TPS = 50;

    public static final double radians(int angle) {
        return angle / 180d * Math.PI;
    }
    // Conditional speed formula
    public static final int speed(int radius) {
        if (radius < 50) return 20;
        else if (radius < 100) return 25;
        else return 30;
    }
    /* // exponential speed formula
    public static final int speed(int radius) {
        return (int)(5 * Math.pow(0.9, (radius - 30))) + 3;
    }
    */
    public static final int xChange(int angle, int speed) {
        return (int)(Math.cos(radians(angle)) * speed);
    }
    public static final int yChange(int angle, int speed) {
        return (int)(Math.sin(radians(angle)) * speed);
    }

    // Commands (See shared doc for more info)
    public static final String PING = "PING"; // Making sure client is still connected
    // Client to Server commands
    public static final String LOBBIES_LIST = "LOBBIES";
    public static final String NEW_LOBBY = "NEW";
    public static final String JOIN_LOBBY = "JOIN";

    // Client to Lobby commands
    public static final String SELECTED = "SELECTED"; // Player has selected abilities
    public static final String RESELECT = "RESELECT"; // Player has gone to reselect abilities
    public static final String READY = "READY"; // Player has selected abilities and is ready to play
    public static final String UNREADY = "UNREADY"; // Player is not ready. Automatically happens when player goes to reselect abilities
    public static final String MOVE = "MOVE"; // Client gives lobby what direction they just went
    public static final String ATTACK = "ATTACK"; // Client used normal attack 
    public static final String ABILITY1 = "ABILITY1"; // Client used first ability  
    public static final String ABILITY2 = "ABILITY2"; // Client used second ability
    public static final String ULTIMATE = "ULTIMATE"; // Client used ultimate ability
    public static final String LEAVE = "LEAVE"; // Client left lobby

    // Server to Client commands
    public static final String LOBBY = "LOBBY"; // Server sends name and current player count of specified lobby 
    public static final String LOBBY_SELECT = "SELECT"; // Tells the client to switch to lobby select screen
    public static final String JOINED = "JOINED"; // Server tells client they have joined the lobby they tried to join
    public static final String NO_LOBBY = "NO_LOBBY"; // Server tells the client it couldn't find a lobby with the name the client gave

    // Lobby to Client commands
    public static final String ABILITIES = "ABILITIES"; // Player has selected abilities
    public static final String REMOVEP = "REMOVEP"; // Player has left lobby, remove them from the player list
    public static final String NEWE = "NEWE"; // New enemy has spawned 
    public static final String PLAYER = "PLAYER"; // Updates the correspondings players information for the client
    public static final String ENEMY = "ENEMY"; // Updates the correspondings enemies information for the client
    public static final String KILLEDE = "KILLEDE"; // Tells the client this enemy has died and to remove it from the enemy list 
    public static final String DIE = "DIE"; // Client has died
    public static final String WIN = "WIN"; // Player(s) have won this round send them to the mid round screen
    public static final String LOSE = "LOSE"; // Player(s) have lost the game send them to the game over screen
    public static final String ABILITY1_READY = "ABILITY1"; // Tells client their first ability is off cooldown
    public static final String ABILITY2_READY = "ABILITY2"; // Tells client their second ability is off cooldown
    public static final String ULTIMATE_READY = "ULTIMATE"; // Tells client their ultimate ability is off cooldown
    public static final String BOUNDS = "BOUNDS"; // Tells client the size of the play area
    public static final String START = "START"; // Tells client there is a start tile here
    public static final String END = "END"; // Tells client there is a end tile here
    public static final String WALl = "WALL"; //Tells client there is a wall tile here

    private Const(){}
}
