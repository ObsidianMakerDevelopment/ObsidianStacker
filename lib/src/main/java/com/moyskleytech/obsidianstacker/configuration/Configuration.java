package com.moyskleytech.obsidianstacker.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import com.moyskleytech.obsidian.material.ObsidianMaterial;
import com.moyskleytech.obsidianstacker.api.StackerAPI;

public class Configuration {

    private static Configuration instance;
    private boolean init = false;
    private String hologramFormat;

    public String getHologramFormat() {
        return hologramFormat;
    }

    public static Configuration getInstance() {
        if (instance != null) {
            if (!instance.init)
                instance.read();
        }
        return instance;
    }

    public Configuration() {
        instance = this;
    }

    private SortedMap<String, Integer> maximums = new TreeMap<>();

    public void read() {
        File warps = new File(StackerAPI.getInstance().getPlugin().getDataFolder(), "config.yml");

        FileConfiguration file = StackerAPI.getInstance().getPlugin().getConfig();
        if (warps.exists())
            try {
                file.load(warps);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        if (!file.contains("blocks")) {
            file.createSection("blocks", maximums);
        }
        if (!file.contains("hologram-format")) {
            file.set("hologram-format", "&c{count}x {translated}");
        }
        hologramFormat = file.getString("hologram-format");
        ConfigurationSection blocks = file.getConfigurationSection("blocks");

        ObsidianMaterial.values().stream().forEach(material -> {
            if (material.normalizedName().contains("xmaterial"))
                return;
            //if (material.normalizedName().contains("spawner"))
            //    return;

            ObsidianMaterial currentMaterial = ObsidianMaterial.valueOf(material.normalizedName());
            String normalizedCurrentMaterialName = (currentMaterial != null) ? currentMaterial.normalizedName()
                    : "null";
            if (!blocks.contains(normalizedCurrentMaterialName)) {
                blocks.set(normalizedCurrentMaterialName, 1);
            }
        });

        blocks.getKeys(false).stream().forEach(key -> {
            ObsidianMaterial currentMaterial = ObsidianMaterial.valueOf(key);
            String normalizedCurrentMaterialName = (currentMaterial != null) ? currentMaterial.normalizedName()
                    : "null";
            if (currentMaterial == null && !key.contains("slimefun")) {
                StackerAPI.getInstance().getPlugin().getLogger().warning("Invalid key present in config file :" + key);
                blocks.set(key, null);
            } else if (!normalizedCurrentMaterialName.equals(key)) { // Normalize config file to new name
                String newKey = normalizedCurrentMaterialName;
                StackerAPI.getInstance().getPlugin().getLogger()
                        .warning("Deprecated key present in config file :" + key + ", newKey=" + newKey);
                blocks.set(key, null);
            } else if (normalizedCurrentMaterialName.contains("xmaterial")) { // Normalize config file to new name
                String newKey = normalizedCurrentMaterialName;
                StackerAPI.getInstance().getPlugin().getLogger()
                        .warning("Deprecated key present in config file :" + key + ", newKey=" + newKey);
                blocks.set(key, null);
            } else
                maximums.put(key, blocks.getInt(key));
        });
        file.createSection("blocks", maximums);
        try {
            file.save(warps);
        } catch (IOException e) {
            e.printStackTrace();
        }
        init = true;
    }

    public int getMax(ObsidianMaterial mat) {
        String norm = mat.normalizedName();
        if (maximums.containsKey(norm))
            return maximums.get(norm).intValue();
        else
            return 1;
    }

    public void reload() {
        init = false;
        maximums.clear();
        read();
    }
}
