/**
 * Final Game SavageBlowAbility Class
 * @Author Ilya Kononov
 * @Date = January 22 2023
 * This class represents on of the abilities the player can choose and is an ability that has an instant effect
 * This ability preforms an enhanced version of the players attack to enemies in front of the player
 */

public class SavageBlowAbility extends InstantAbility{
    private Lobby.Game game;
    public SavageBlowAbility(Lobby.Game game, Player player){
        super(player, Const.SAVAGE_BLOW_COOLDOWN);
        this.game = game;
    }

    public boolean doEffect(){
        boolean attacked = player.heavyAttack(game.getEnemies());
        return attacked;
    }
}
