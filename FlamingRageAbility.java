/**
 * Final Game FlamingRageAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that lasts over a period of time
 * This ability gives the player a big boost in attack damage and attack speed
 */

public class FlamingRageAbility extends DurationAbility{
    public FlamingRageAbility(Player player, Lobby lobby){
        super(player, Const.RAGE_COOLDOWN, Const.RAGE_DURATION, lobby);
    }
    
    public boolean doEffect(){
        player.setDamage(Const.RAGE_ATTACK_BUFF);
        player.setAttackSpeed(-Const.RAGE_ATTACK_SPEED_BUFF);
        return true;
    }

    public void undoEffect(){
        player.setDamage(-Const.RAGE_ATTACK_BUFF);
        player.setAttackSpeed(Const.RAGE_ATTACK_SPEED_BUFF);
    }
}