/**
 * Final Game TimeStopAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that has an instant effect
 * This ability stops all actions of all enemies for a short time
 */

public class TimeStopAbility extends InstantAbility{
    private Lobby lobby;
    public TimeStopAbility(Lobby lobby, Player player){
        super(player, Const.TIME_STOP_COOLDOWN);
        this.lobby = lobby;
    }
    
    public boolean doEffect(){
        boolean timeStopped = lobby.getGame().stopTime();
        return timeStopped;
    }
}
