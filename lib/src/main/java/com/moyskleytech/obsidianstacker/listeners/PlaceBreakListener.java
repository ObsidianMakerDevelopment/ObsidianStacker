package com.moyskleytech.obsidianstacker.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.moyskleytech.obsidianstacker.Main;
import com.moyskleytech.obsidianstacker.api.Stack;
import com.moyskleytech.obsidianstacker.api.StackerAPI;
import com.moyskleytech.obsidianstacker.configuration.Configuration;
import com.moyskleytech.obsidianstacker.utils.Scheduler;
import com.moyskleytech.obsidianstacker.utils.StackUtils;

public class PlaceBreakListener implements Listener {

    private List<Location> temporary = new ArrayList<>();
    public PlaceBreakListener(Main plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer() == null)
            return;
        if (event.getPlayer().isSneaking())
            return;
        StackerAPI api = StackerAPI.getInstance();
        Optional<Stack> maybeStack = api.getStack(event.getBlock());
        maybeStack.ifPresent((stack) -> {
            ObsidianMaterial material = stack.getBlockMaterial();
            stack.setCount(stack.getCount() - 1).thenAccept((count) -> {
                if (count > 0) {
                    boolean isBukkitMaterial = material.getClass().getName().contains("Bukkit");
                    int delay = isBukkitMaterial ? 1 : 20;
                    Scheduler.getInstance().runChunkTask(api.getPlugin(), event.getBlock().getLocation(), delay, () -> {
                        // API and place a new one I guess
                        if (isBukkitMaterial)
                            event.getBlock().setBlockData(((BlockDisplay) stack.getEntity()).getBlock());
                        else
                            material.setBlock(event.getBlock());

                        stack.refresh();
                    });
                } else {
                    stack.getEntity().remove();
                }
            });
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer() == null)
            return;
        if (event.getPlayer().isSneaking())
            return;
        Block maybeStackBlock = event.getBlockAgainst();
        if(temporary.contains(maybeStackBlock.getLocation()))
        {
            event.setCancelled(true);
            return;
        }
        Block placedBlock = event.getBlockPlaced();
        StackerAPI api = StackerAPI.getInstance();

        ObsidianMaterial itemInHand = ObsidianMaterial.match(event.getPlayer().getInventory().getItem(event.getHand()));

        // if (maybeStackBlock.getType() == Material.IRON_BLOCK) {
        ObsidianMaterial against = ObsidianMaterial.match(maybeStackBlock);
        if (itemInHand.normalizedName().equals(against.normalizedName())) {

            Optional<Stack> maybeStack = api.getStack(maybeStackBlock);
            int max = Configuration.getInstance().getMax(itemInHand);
            if(max==1) return;

            temporary.add(placedBlock.getLocation());

            Stack s = maybeStack.orElseGet(() -> StackUtils.makeBlockStack(maybeStackBlock));
            if (s.getCount() + 1 <= max || max <=0) {
                s.setCount(s.getCount() + 1).thenRun(() -> {
                    s.refresh();
                });
                placedBlock.setType(Material.AIR);
                temporary.remove(placedBlock.getLocation());
            }
        }
        // }
    }
}
