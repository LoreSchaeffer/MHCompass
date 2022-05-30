package it.multicoredev;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.multicoredev.mclib.yaml.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyright © 2022 by Lorenzo Magni
 * This file is part of untitled.
 * untitled is under "The 3-Clause BSD License", you can find a copy <a href="https://opensource.org/licenses/BSD-3-Clause">here</a>.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
public class MHCompass extends JavaPlugin {
    public Configuration config;
    private final List<Player> runners = new ArrayList<>();
    private boolean running = false;
    
    //TODO non permettere l'uso della seconda mano per la bussola

    @Override
    public void onEnable() {
        config = new Configuration(new File(getDataFolder(), "config.yml"), getResource("config.yml"));

        if (!getDataFolder().exists() || !getDataFolder().isDirectory()) {
            if (!getDataFolder().mkdirs()) {
                onDisable();
                return;
            }
        }

        try {
            config.autoload();
        } catch (IOException e) {
            e.printStackTrace();
            onDisable();
            return;
        }

        getServer().getPluginManager().registerEvents(new Listeners(this), this);

        MHCCommand cmd = new MHCCommand(this);
        getCommand("manhuntcompass").setExecutor(cmd);
        getCommand("manhuntcompass").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {

    }

    public void addRunner(Player player) {
        runners.add(player);
    }

    public void removeRunner(Player player) {
        runners.remove(player);
    }

    public void clearRunners() {
        runners.clear();
    }

    public List<Player> getRunners() {
        return runners;
    }

    public List<Player> getHunters() {
        return Bukkit.getOnlinePlayers().stream().filter(p -> !runners.contains(p)).collect(Collectors.toList());
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void giveCompass(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        NBTItem nbti = new NBTItem(compass);
        nbti.setBoolean("mhc", true);
        compass = nbti.getItem();

        if (!player.getInventory().addItem(compass).isEmpty()) {
            Location location = player.getLocation();
            if (location.getWorld() == null) return;

            location.getWorld().dropItem(location, compass);
        }
    }
}