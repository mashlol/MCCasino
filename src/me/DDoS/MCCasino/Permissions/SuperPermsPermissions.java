package me.DDoS.MCCasino.permissions;

import org.bukkit.entity.Player;

/**
 *
 * @author DDoS
 */
public class SuperPermsPermissions implements Permissions {

    @Override
    public boolean hasPermission(Player player, String perm) {
        
        return player.hasPermission(perm);
        
    }   
}
