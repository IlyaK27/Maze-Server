import java.util.HashMap;
public class Const {
    public static final int PORT = 5001;
    public static final int LOBBY_SIZE = 4;
    
    public static final int HEARTBEAT_RATE = 60000;
    public static final int COUNTDOWN = 3000; // 3 seconds

    // Ability names
    public static final String MAX_HEATH_NAME = "MAX_HEALTH";
    public static final String CLOAKED_NAME = "CLOAKED";
    public static final String LIFE_STEAL_NAME = "LIFE_STEAL";
    public static final String SHARPENED_NAME = "SHARPENED";
    public static final String HEALTH_REGEN_NAME = "HEALTH_REGEN";

    public static final String HEAL_NAME = "HEAL";
    public static final String INVESTIGATE_NAME = "INVESTIGATE";
    public static final String SAVAGE_BLOW_NAME = "SAVAGE_BLOW";
    public static final String SWIFT_MOVES_NAME = "SWIFT_MOVES";

    public static final String TIME_STOP_NAME = "TIME_STOP";
    public static final String FORTIFY_NAME = "FORTIFY";
    public static final String FLAMING_RAGE_NAME = "FLAMING_RAGE";

    // Ability cooldowns and durations and values
    public static final int HEALING_INTERVAL = 30;
    public static final int SWIFT_MOVES_BUFF = 7;
    public static final int SWIFT_MOVES_COOLDOWN = 8500;
    public static final int SWIFT_MOVES_DURATION = 3000;
    public static final int PASSIVE_HEALING_AMOUNT = 1;
    public static final int HEAL_COOLDOWN = 7000;
    public static final int SAVAGE_BLOW_COOLDOWN = 6000;
    public static final int INVESTIGATE_RANGE = 3;
    public static final int INVESTIGATE_COOLDOWN = 12000;
    public static final int INVESTIGATE_DURATION = 2500;
    public static final int RAGE_ATTACK_BUFF = 30;
    public static final double RAGE_ATTACK_SPEED_BUFF = 0.75;
    public static final int RAGE_COOLDOWN = 19000;
    public static final int RAGE_DURATION = 5000;
    public static final double FORTIFY_AMOUNT = 0.75;
    public static final int FORTIFY_COOLDOWN = 20000;
    public static final int FORTIFY_DURATION = 4500;
    public static final int TIME_STOP_COOLDOWN = 25000;
    public static final int TIME_STOP_DURATION = 3500;

    // Maze tiles
    public static final char START_TILE = 'S';
    public static final char PATH = 'P';
    public static final char OPTIMAL_PATH = 'O';
    public static final char WALL = 'W';
    public static final char END_TILE = 'E';
    // Maze info
    public static final int STARTING_ROWS = 20;
    public static final int STARTING_COLS = 20;
    public static final int MIN_DISTANCE = 10;
    public static final int PATH_CHANCE = 55;
    public static final int TILE_DIMENSIONS = 150;
    public static final int PLAYER_DIMENSIONS = 110;
    public static final int PLAYER_MAX_HEALTH = 100;
    public static final int PLAYER_ATTACK_SPEED = 500; // Can attack twice per second
    public static final int ATTACK_RANGE = 40; // How far the attack goes from the player
    public static final int BIG_ATTACK_RANGE = 60; // How far the attack goes from the player
    public static final int BIG_ATTACK_MULTIPLIER = 3; // How much stronger than normal attack
    public static final int PLAYER_DAMAGE = 30;
    public static final int CLOAK_REDUCTION = TILE_DIMENSIONS * 2;
    public static final int SHARPENED_DAMAGE = 10;
    public static final double LIFE_STEAL_AMOUNT = 0.25;
    public static final int ENEMY_DIMENSIONS = 120;
    public static final int MAX_ENEMIES = 8;
    public static final int ENEMIES_INCREMENT = 4;
    public static final int ENEMY_MOVEMENT_SPEED = 4;
    public static final int ENEMY_HEALTH = 200;
    public static final int ENEMY_HEALTH_INCREMENT = 50;
    public static final int ENEMY_DAMAGE = 25;
    public static final int ENEMY_DAMAGE_INCREMENT = 10;
    public static final int ENEMY_ATTACKS_SPEED = 125;
    public static final int ENEMY_RANGE = TILE_DIMENSIONS * 5;
    public static final int ENEMY_MOVEMENT_BREAK = 45;
    public static final int MAZE_INCREASE = 5; // Difference in dimensions of maze after reach round
    public static final int BLEEDING_OUT_TIME = 10000; // 10 seconds
    public static final int REVIVE_INTERVAL = 300;

    // Commands (See shared doc for more info)
    public static final String PING = "PING"; // Making sure client is still connected
    // Client to Server commands
    public static final String LOBBIES_LIST = "LOBBIES";
    public static final String NEW_LOBBY = "NEW";
    public static final String NAME = "NAME"; 
    public static final String JOIN_LOBBY = "JOIN";

    // Client to Lobby commands
    public static final String SELECTED = "SELECTED"; // Player has selected abilities
    public static final String RESELECT = "RESELECT"; // Player has went back to choose different abilities
    public static final String MY_ABILITIES = "MY_ABILITIES"; // Players abilities after this command
    public static final String READY = "READY"; // Player has selected abilities and is ready to play
    public static final String UNREADY = "UNREADY"; // Player is not ready. Automatically happens when player goes to reselect abilities
    public static final String MOVE = "MOVE"; // Client gives lobby what direction they just went
    public static final String ATTACK = "ATTACK"; // Client used normal attack 
    public static final String ABILITY = "ABILITY"; // Client used ability  
    public static final String ULTIMATE = "ULTIMATE"; // Client used ultimate ability
    public static final String DRAWN = "DRAWN"; // Client has drawn their map so send new map update
    public static final String SPECTATE = "SPECTATE"; // Client tells server the want to spectate the provided player
    public static final String LEAVE = "LEAVE"; // Client left lobby

    // Server to Client commands
    public static final String CLEAR_LOBBIES = "CLEAR"; // Client clears its list of lobbies
    public static final String LOBBY = "LOBBY"; // Server sends name and current player count of specified lobby 
    public static final String LOBBY_SELECT = "SELECT"; // Tells the client to switch to lobby select screen
    public static final String JOINED = "JOINED"; // Server tells client they have joined the lobby they tried to join
    public static final String NO_LOBBY = "NO_LOBBY"; // Server tells the client it couldn't find a lobby with the name the client gave

    // Lobby to Client commands
    public static final String NEW_PLAYER = "NEWP"; // New player has joined lobby 
    public static final String GAME_START = "GAME_START"; // Tells players game has started and to switch to game screen
    public static final String UPDATE_MAP = "UPDATE_MAP"; // Updates a certian part of the map for the client
    public static final String DRAW_MAP = "DRAW_MAP"; // Tells the client to draw the map and lets them know the map will be updated shortly
    public static final String ABILITIES = "ABILITIES"; // Player has selected abilities
    public static final String REMOVEP = "REMOVEP"; // Player has left lobby, remove them from the player list
    public static final String NEWE = "NEWE"; // New enemy has spawned 
    public static final String PLAYER = "PLAYER"; // Updates the correspondings players information for the client
    public static final String DOWNED = "DOWNED"; // Tells the client the specified player has been put into the downed state
    public static final String REVIVED = "REVIVED"; // Tells the client the specified player has been revived
    public static final String DIED = "DIED"; // Tells the client the specified player has died
    public static final String ENEMY = "ENEMY"; // Updates the correspondings enemies information for the client
    public static final String KILLEDE = "KILLEDE"; // Tells the client this enemy has died and to remove it from the enemy list 
    public static final String DIE = "DIE"; // Client has died
    public static final String WIN = "WIN"; // Player(s) have won this round send them to the mid round screen
    public static final String LOSE = "LOSE"; // Player(s) have lost the game send them to the game over screen
    public static final String ABILITY_READY = "ABILITY_READY"; // Tells client their ability is off cooldown
    public static final String ULTIMATE_READY = "ULTIMATE_READY"; // Tells client their ultimate ability is off cooldown

    public static final int PLAYER_MOVEMENT_SPEED = 10;
    private static final Integer[] MOVE_UP = {0, -1};
    private static final Integer[] MOVE_RIGHT = {1, 0};
    private static final Integer[] MOVE_DOWN = {0, 1};
    private static final Integer[] MOVE_LEFT = {-1, 0};
    public static final HashMap<Integer, Integer[]> PLAYER_DIRECTIONS = new HashMap<Integer, Integer[]>(){ // Direction, correction
        { // x - 0, y - 1
            put(0, MOVE_UP); 
            put(1, MOVE_RIGHT);
            put(2, MOVE_DOWN);
            put(3, MOVE_LEFT);
        }
    };
    public static final Integer[][] ADJACENT_SQUARES = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};
    private static final Integer[][] UP_TILES = {ADJACENT_SQUARES[0], ADJACENT_SQUARES[1], ADJACENT_SQUARES[2]};
    private static final Integer[][] RIGHT_TILES = {ADJACENT_SQUARES[2], ADJACENT_SQUARES[3], ADJACENT_SQUARES[4]};
    private static final Integer[][] DOWN_TILES = {ADJACENT_SQUARES[4], ADJACENT_SQUARES[5], ADJACENT_SQUARES[6]};
    private static final Integer[][] LEFT_TILES = {ADJACENT_SQUARES[6], ADJACENT_SQUARES[7], ADJACENT_SQUARES[0]};
    public static final HashMap<Integer, Integer[][]> ADJACENT_TILES = new HashMap<Integer, Integer[][]>(){
        {
            put(0, UP_TILES); 
            put(1, RIGHT_TILES);
            put(2, DOWN_TILES);
            put(3, LEFT_TILES);
        }
    };  
    private Const(){}
}