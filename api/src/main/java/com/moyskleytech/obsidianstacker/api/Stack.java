package com.moyskleytech.obsidianstacker.api;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.moyskleytech.obsidian.material.ObsidianMaterial;

public interface Stack {

    public static final NamespacedKey countKey = new NamespacedKey("obsidianstacker", "count");
    public static final NamespacedKey stackOfKey = new NamespacedKey("obsidianstacker", "stackof");

    public StackType getType() ;

    public Display getEntity() ;
    public Block getBlock() ;
    public Location getLocation() ;

    public int getCount();

    public CompletableFuture<Integer> setCount(int count) ;

    public ObsidianMaterial getBlockMaterial();

    public void refresh();

    public void destroy();
}
