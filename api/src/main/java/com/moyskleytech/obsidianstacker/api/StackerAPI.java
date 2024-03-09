package com.moyskleytech.obsidianstacker.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

public abstract class StackerAPI {
    private static StackerAPI instance;

    public static StackerAPI getInstance() {
        return instance;
    }

    private Plugin plugin;

    public StackerAPI(Plugin p) {
        this.plugin = p;
        instance=this;
    }
    public Plugin getPlugin()
    {
        return plugin;
    }

    public abstract CompletableFuture<List<Stack>> getStacks(Chunk c);
    public abstract CompletableFuture<List<Stack>> getStacks(World w) ;
    public abstract java.util.Optional<Stack> getStack(Block b) ;

    public static boolean isStack(Entity e) {
        PersistentDataContainer pdc = e.getPersistentDataContainer();
        return pdc.has(Stack.stackOfKey);
    }
}
