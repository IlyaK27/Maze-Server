/**
 * Final Game HealAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that has an instant effect
 * This ability heals the player a certian portion of their total health instantly
 */

public class HealAbility extends InstantAbility{
    public HealAbility(Player player){
        super(player, Const.HEAL_COOLDOWN);
    }

    public boolean doEffect(){
        boolean healed = false;
        if(player.getHealth() < player.getMaxHealth()){
            this.player.heal((int)(player.getMaxHealth() * 0.30));
            healed = true;
        }
        return healed;
    }
}
