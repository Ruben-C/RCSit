package org.rcbox.rcsit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;

public final class Rcsit extends JavaPlugin implements Listener {

    public static HashMap<String, Location> resetLocs = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("[RCSit] Plugin has been enabled.");
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("[RCSit] Plugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (label.equalsIgnoreCase("sit")) {

                Block block = player.getTargetBlock(5);

                if (block.getType().name().toLowerCase().contains("stairs")) {
                    resetLocs.put(player.getUniqueId().toString(), player.getLocation());
                    createSeat(player, block);
                } else {
                    player.sendMessage(ChatColor.RED + "You can only sit on stairs.");
                }
                return true;
            }
        }

        return false;
    }

    private void createSeat(Player player, Block block) {
        Location loc = block.getLocation();
        Stairs stairs = (Stairs) block.getBlockData();

        String dir = stairs.getFacing().name();
        final float rot = (dir.equalsIgnoreCase("south") ? 180 :
                dir.equalsIgnoreCase("east") ? 90 :
                        dir.equalsIgnoreCase("west") ? 270 : 0);

        ArmorStand as = loc.getWorld().spawn(loc.add(0.5, -1.1, 0.5), ArmorStand.class, ArmorStand -> {
            ArmorStand.setGravity(false);
            ArmorStand.setRotation(rot, 0);
            ArmorStand.setMetadata("isChair", new FixedMetadataValue(this, Boolean.valueOf(true)));
            ArmorStand.setVisible(false);
        });

        loc.setYaw(rot);
        loc.setPitch(0);

        player.teleport(loc);
        as.addPassenger(player);
    }

    @EventHandler
    public void onExit(EntityDismountEvent event) {

        if (event.getDismounted() == null || event.getDismounted().getType() != EntityType.ARMOR_STAND)
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        ArmorStand aStand = (ArmorStand) event.getDismounted();
        Player player = (Player) event.getEntity();
        String sUDID = player.getUniqueId().toString();

        if (aStand.hasMetadata("isChair")) {
            if (resetLocs.containsKey(sUDID)) {
                aStand.remove();

                new BukkitRunnable(){
                    Player p = player;
                    Location l = resetLocs.get(sUDID);

                    @Override
                    public void run() {
                        p.teleport(l);
                    }
                }.runTaskLater(this, 1);

                resetLocs.remove(sUDID);
            }
        }
    }
}

