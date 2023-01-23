/**
 * Final Game InstantAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class is very general framework for an ability that after used does a simple action and thats all it does
 */

public abstract class InstantAbility extends Ability{
    public InstantAbility(Player player, int cooldown){
        super(player, cooldown);
    }

    public abstract boolean doEffect();

    public boolean useAbility(){
        boolean used = doEffect();
        if(used){this.timer.start();}
        return used;
    }
}
