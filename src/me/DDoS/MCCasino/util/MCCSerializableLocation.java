package me.DDoS.MCCasino.util;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

/**
 *
 * @author DDoS
 */
public class MCCSerializableLocation implements Serializable {
    
    private double x;
    private double y;
    private double z;
    private String worldName;
    
    public MCCSerializableLocation(double x, double y, double z, String worldName) {
        
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldName = worldName;
    
    }
    
    public Location getLocation(Server server) {
        
        World world = server.getWorld(worldName);
        
        if (world != null) {
            
            return new Location(world, x, y, z);
        
        }
        
        return null;
        
    }
}
