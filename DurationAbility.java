/**
 * Final Game DurationAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class is very general framework for an ability that after used has an effect that lasts for a certian amount of time after pressed
 * This class uses a thread to manage the effect of the ability that happens after used
 * A thread is used because the lobby shouldnt be keeping track of when the abilities effect runs out
 */

public abstract class DurationAbility extends Ability{
    private int timeDuration;
    private DurationThread abilityDuration;
    protected Lobby lobby;
    public DurationAbility(Player player, int cooldown, int duration, Lobby lobby){
        super(player, cooldown);
        this.timeDuration = duration;
        this.abilityDuration = new DurationThread(duration);
        this.lobby = lobby;
    }

    public boolean useAbility(){
        boolean used = doEffect();
        if(used){
            this.abilityDuration.start();
            this.timer.start();
        }
        return used;
    }
    
    public abstract boolean doEffect();

    public abstract void undoEffect();

    private void resetAbility(){
        this.abilityDuration = new DurationThread(timeDuration);
    }
    protected class DurationThread extends Thread{
        int duration;
        public DurationThread(int duration){
            this.duration = duration;
        }

        public void run(){
            try {
                Thread.sleep(duration);
            } catch (Exception e) {}
            if(lobby.playing()){undoEffect();} // No need to undo effect if players are no longer playing
            resetAbility();
        }
    }
}
