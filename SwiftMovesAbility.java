/**
 * Final Game SwiftMovesAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that lasts over a period of time
 * This ability gives the player a temporary boost in movement speed
 */

public class SwiftMovesAbility extends DurationAbility{
    public SwiftMovesAbility(Player player, Lobby lobby){
        super(player, Const.SWIFT_MOVES_COOLDOWN, Const.SWIFT_MOVES_DURATION, lobby);
    }
    
    public boolean doEffect(){
        player.setSpeed(Const.SWIFT_MOVES_BUFF);
        return true;
    }

    public void undoEffect(){
        player.setSpeed(-Const.SWIFT_MOVES_BUFF);
    }
}
