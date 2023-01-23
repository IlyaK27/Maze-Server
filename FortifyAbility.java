/**
 * Final Game FortifyAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that lasts over a period of time
 * This ability makes the player incredibly tanky to all damage
 */

public class FortifyAbility extends DurationAbility{
    public FortifyAbility(Player player, Lobby lobby){
        super(player, Const.FORTIFY_COOLDOWN, Const.FORTIFY_DURATION, lobby);
    }
    
    public boolean doEffect(){
        player.setToughness(-Const.FORTIFY_AMOUNT);
        return true;
    }

    public void undoEffect(){
        player.setToughness(Const.FORTIFY_AMOUNT);
    }
}