package com.moyskleytech.obsidianstacker.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.moyskleytech.obsidianstacker.api.Stack;
import com.moyskleytech.obsidianstacker.api.StackType;
import com.moyskleytech.obsidianstacker.api_impl.StackImpl;

public class StackUtils {
    public static Stack makeBlockStack(Block b) {
        Location loc = b.getLocation().clone();
        loc = loc.add(1/16. + 0.5, 1/16.+1, 1/16.+0.5);
        BlockDisplay displayEntity = b.getWorld().spawn(loc, BlockDisplay.class);
        displayEntity.setBlock(b.getBlockData());
        PersistentDataContainer pdc = displayEntity.getPersistentDataContainer();
        displayEntity.setGravity(false);
        displayEntity.setPersistent(true);
        displayEntity.setDisplayHeight(1);
        displayEntity.setBrightness(new Brightness(15,15));

        Transformation transformation = displayEntity.getTransformation();
        transformation.getTranslation().set(new float[]{
            -0.5f,-1,-0.5f
        });
        transformation.getScale().set(14./16);
        displayEntity.setTransformation(transformation);
        
        pdc.set(Stack.countKey, PersistentDataType.INTEGER, 1);
        pdc.set(Stack.stackOfKey, PersistentDataType.STRING, StackType.BLOCK.name());

        StackImpl stack= new StackImpl(displayEntity);
        stack.setHologram();
        return stack;
    }
}
