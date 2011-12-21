package me.DDoS.MCCasino.slotmachine;

import java.util.ArrayList;
import java.util.List;

import me.DDoS.MCCasino.bet.MCCBet;
import me.DDoS.MCCasino.bet.MCCBets;
import me.DDoS.MCCasino.util.MCCDropCleaner;
import me.DDoS.MCCasino.util.MCCUtil;
import me.DDoS.MCCasino.MCCasino;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author DDoS
 */
public class MCCSlotMachine {

    private List<MCCReel> reels = new ArrayList<MCCReel>();
    private List<Location> reelLocations = new ArrayList<Location>();
    private List<MCCReward> rewards = new ArrayList<MCCReward>();
    private List<Item> itemsToRemove = new ArrayList<Item>();
    private MCCBets bets;
    private boolean active;
    private MCCasino plugin;

    public MCCSlotMachine(List<Location> reelLocations, List<MCCReel> reels, List<MCCReward> rewards, MCCBets betHandler,
            boolean active, MCCasino plugin) {

        this.reels = reels;
        this.reelLocations = reelLocations;
        this.rewards = rewards;
        this.bets = betHandler;
        this.active = active;
        this.plugin = plugin;

    }

    public boolean checkReels() {

        if (reels.size() > reelLocations.size()) {

            active = false;
            return false;

        }

        active = true;
        return true;

    }

    public boolean addReelLocation(Location loc) {

        if (!reelLocations.contains(loc)) {

            reelLocations.add(loc);
            return true;

        }

        return false;

    }

    public void removeReelLocation(Location loc) {

        if (reelLocations.contains(loc)) {

            reelLocations.remove(loc);
            checkReels();

        }
    }

    public List<Location> getReels() {

        return reelLocations;

    }

    public void setActive(boolean active) {

        this.active = active;

    }

    public void clearItems() {

        for (Item item : itemsToRemove) {

            item.remove();

        }
    }

    public boolean checkItem(Item item1) {

        for (Item item2 : itemsToRemove) {

            if (item1.getEntityId() == item2.getEntityId()) {

                return true;

            }
        }

        return false;

    }
    
    private List<Integer> spinReels() {
        
        itemsToRemove.clear();
        int i = 0;
        List<Integer> results = new ArrayList<Integer>();

        for (MCCReel reel : reels) {

            Location loc1 = reelLocations.get(i);
            ItemStack item = reel.getRandomItem();

            byte data = getDataFromSign(loc1);
            Location loc2 = getOffsetLocation(data, loc1);

            Item droppedItem = loc2.getWorld().dropItem(loc2, item);

            Vector vect = getVelocity(data);
            droppedItem.setVelocity(vect);

            results.add(item.getTypeId());
            itemsToRemove.add(droppedItem);

            i++;

        }
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MCCDropCleaner(this), 100L);
        
        return results;
        
    }
    
    public void testRun(Player player) {
        
        if (!active) {
            
            MCCUtil.tell(player, "This machine is not active.");
            return;
            
        }
        
        active = false;
        List<Integer> results = spinReels();

        for (MCCReward reward : rewards) {

            int multiplier = reward.get(results);

            if (multiplier > 0) {

                MCCUtil.tell(player, "You won " + multiplier + " time(s) your bet.");
                return;

            }
        }

        MCCUtil.tell(player, "You lost!");
        
    }

    public void run(Player player) {

        if (!active) {
            
            MCCUtil.tell(player, "This machine is not active.");
            return;
            
        }

        MCCBet bet = bets.getBet(player.getItemInHand(), player);

        if (bet == null) {

            return;

        }

        active = false;
        List<Integer> results = spinReels();

        for (MCCReward reward : rewards) {

            int multiplier = reward.get(results);

            if (multiplier > 0) {

                bet.applyMultiplier(multiplier);
                MCCUtil.tell(player, "You won " + multiplier + " time(s) your bet.");
                bet.giveReward(player);
                return;

            }
        }

        MCCUtil.tell(player, "You lost!");

    }

    private byte getDataFromSign(Location loc) {

        Block block = loc.getBlock();

        if (checkForSign(block)) {

            return ((Sign) block.getState()).getData().getData();

        }

        return 0x0;

    }

    private Vector getVelocity(byte d) {

        switch (d) {

            case 0x2://North, z goes down
                return new Vector(0, 0, -0.2);

            case 0x3://South, z goes up
                return new Vector(0, 0, 0.2);

            case 0x4://West, x goes down
                return new Vector(-0.2, 0, 0);

            case 0x5://East, x goes up
                return new Vector(0.2, 0, 0);

            default:
                return new Vector(0, 0, 0);

        }
    }

    private Location getOffsetLocation(byte d, Location loc) {

        Location loc2 = loc.clone();

        switch (d) {

            case 0x2://North, add to x, add to z
                return loc2.add(0.5, 0, 1);

            case 0x3://South, add to x
                return loc2.add(0.5, 0, 0);

            case 0x4://West, add to z, add to x
                return loc2.add(1, 0, 0.5);

            case 0x5://East, add to z
                return loc2.add(0, 0, 0.5);

            default:
                return loc2;

        }
    }

    private boolean checkForSign(Block block) {

        switch (block.getType()) {

            case WALL_SIGN:
                return true;

            default:
                return false;

        }
    }
}