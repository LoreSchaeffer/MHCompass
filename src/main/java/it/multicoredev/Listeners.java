package it.multicoredev;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.multicoredev.mbcore.spigot.chat.Chat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

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
public class Listeners implements Listener {
    private final MHCompass plugin;

    public Listeners(MHCompass plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isRunning()) return;

        List<ItemStack> drops = event.getDrops();
        drops.removeIf(item -> new NBTItem(item).hasKey("mhc"));
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.isRunning()) return;

        if (plugin.getHunters().contains(event.getPlayer())) return;
        plugin.giveCompass(event.getPlayer());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.isRunning()) return;

        ItemStack item = event.getItem();
        if (item == null) return;
        if (!item.getType().equals(Material.COMPASS)) return;
        if (!new NBTItem(item).hasKey("mhc")) return;

        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        List<Player> runners = plugin.getRunners();
        Player nearest = null;
        double distance = Double.MAX_VALUE;

        for (Player runner : runners) {
            Location location = runner.getLocation();
            if (location.getWorld() != playerLocation.getWorld()) continue;

            double d = location.distance(playerLocation);
            if (d < distance) {
                distance = d;
                nearest = runner;
            }
        }

        if (nearest == null) {
            Chat.send(plugin.config.getString("messages.cannot-track"), player);
            player.setCompassTarget(player.getWorld().getSpawnLocation());
        } else {
            Chat.send(plugin.config.getString("messages.tracking-nearest").replace("{player}", nearest.getName()).replace("{distance}", String.valueOf((long) distance)), player);
            player.setCompassTarget(nearest.getLocation());
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!plugin.isRunning()) return;

        if (new NBTItem(event.getItemDrop().getItemStack()).hasKey("mhc")) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickUpItem(EntityPickupItemEvent event) {
        if (!plugin.isRunning()) return;
        if (!event.getItem().getItemStack().getType().equals(Material.COMPASS)) return;


        if (new NBTItem(event.getItem().getItemStack()).hasKey("mhc")) event.setCancelled(true);
    }
}
