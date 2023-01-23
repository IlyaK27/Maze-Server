/**
 * Final Game Ability Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class is very general/basic framework for an ability
 * This class uses the timer class to time tell the lobby when the abilty is ready
 */

public abstract class Ability {
    Player player;
    Timer timer;

    public Ability(Player player, int cooldown){
        this.player = player;
        this.timer = new Timer();
        timer.setTimerLength(cooldown);
    }
    public abstract boolean useAbility(); // What happens when the ability is used

    public abstract boolean doEffect(); // Actual effect of the ability

    public boolean abilityReady(){
        return this.timer.finished();
    }
}
