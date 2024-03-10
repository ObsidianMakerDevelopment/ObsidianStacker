package com.moyskleytech.obsidianstacker.api_impl;

import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.google.common.base.Throwables;
import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.moyskleytech.obsidianstacker.Main;
import com.moyskleytech.obsidianstacker.api.Stack;
import com.moyskleytech.obsidianstacker.api.StackType;
import com.moyskleytech.obsidianstacker.api.StackerAPI;
import com.moyskleytech.obsidianstacker.configuration.Configuration;
import com.moyskleytech.obsidianstacker.utils.Scheduler;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.Arrays;

public class StackImpl implements Stack {

    private Display displayEntity;
    private StackType type;
    private int count;

    public StackImpl(Entity e) {
        assert (e instanceof Display);
        assert (StackerAPI.isStack(e));
        displayEntity = (Display) e;
        read();
    }

    private void read() {
        PersistentDataContainer pdc = displayEntity.getPersistentDataContainer();
        type = StackType.valueOf(pdc.get(Stack.stackOfKey, PersistentDataType.STRING));
        count = pdc.get(Stack.countKey, PersistentDataType.INTEGER).intValue();
    }

    @Override
    public StackType getType() {
        return type;
    }

    @Override
    public Display getEntity() {
        return displayEntity;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CompletableFuture<Integer> setCount(int count) {
        CompletableFuture<Integer> answer = new CompletableFuture<>();
        Scheduler.getInstance().runEntityTask(StackerAPI.getInstance().getPlugin(), displayEntity, 0, () -> {
            try {
                PersistentDataContainer pdc = displayEntity.getPersistentDataContainer();
                pdc.set(Stack.countKey, PersistentDataType.INTEGER, count);
                this.count = count;
                answer.complete(count);
            } catch (Throwable t) {
                answer.completeExceptionally(t);
            }
        });
        return answer;
    }

    @Override
    public ObsidianMaterial getBlockMaterial() {
        return ObsidianMaterial.match(getBlock());
    }

    public void setHologram() {
        if (type == StackType.BLOCK) {
            ObsidianMaterial material = ObsidianMaterial.match(getBlock());

            String hologramFormat = Configuration.getInstance().getHologramFormat();
            Component customName = LegacyComponentSerializer.legacyAmpersand().deserialize(hologramFormat);

            try {
                if (material.getClass().getName().contains("Bukkit")) {
                    try {
                        String translationKey = material.toItem()
                                .translationKey();

                        customName = customName.replaceText((config) -> config.matchLiteral("{translated}")
                                .replacement(Component.translatable(translationKey)).build());
                    } catch (Throwable t) {
                        customName = customName.replaceText(
                                (config) -> config.matchLiteral("{translated}")
                                        .replacement(material.toItem().displayName())
                                        .build());
                    }
                } else {
                    customName = customName.replaceText(
                            (config) -> config.matchLiteral("{translated}").replacement(material.toItem().displayName())
                                    .build());
                }
                customName = customName.replaceText((config) -> config.matchLiteral("{count}")
                        .replacement(String.valueOf(count)).build());


                displayEntity.customName(customName);
            } catch (Throwable t) {
                String formatted_normalized_name = material.normalizedName();
                if (formatted_normalized_name.contains(":"))
                    formatted_normalized_name = formatted_normalized_name.split(":")[1];
                formatted_normalized_name = String.join(" ", Arrays.stream(formatted_normalized_name.split("_")).map(
                        word -> {
                            if (word.length() > 1) {
                                return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                            }
                            return word.toLowerCase();
                        }).toList());
                String final_formatted_normalized_name = formatted_normalized_name;
                customName = LegacyComponentSerializer.legacyAmpersand().deserialize(hologramFormat);
                customName = customName.replaceText(
                        (config) -> config.matchLiteral("{translated}").replacement(final_formatted_normalized_name)
                                .build());
                customName = customName.replaceText((config) -> config.matchLiteral("{count}")
                        .replacement(String.valueOf(count)).build());
                displayEntity.setCustomName(LegacyComponentSerializer.legacySection().serialize(customName));
            }
            displayEntity.setCustomNameVisible(true);
        }
    }

    @Override
    public void refresh() {
        Scheduler.getInstance().runEntityTask(StackerAPI.getInstance().getPlugin(), displayEntity, 0, () -> {
            setHologram();
        });
    }

    @Override
    public Block getBlock() {
        return getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
    }

    @Override
    public Location getLocation() {
        return getBlock().getLocation();
    }

    @Override
    public void destroy() {
        getEntity().remove();
    }
}
