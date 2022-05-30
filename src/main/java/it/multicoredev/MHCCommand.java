package it.multicoredev;

import de.tr7zw.changeme.nbtapi.NBTItem;
import it.multicoredev.mbcore.spigot.chat.Chat;
import it.multicoredev.mbcore.spigot.util.TabCompleterUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class MHCCommand implements CommandExecutor, TabExecutor {
    private final MHCompass plugin;

    public MHCCommand(MHCompass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if ((sender instanceof Player) && !sender.isOp()) {
            Chat.send(plugin.config.getString("messages.usage"), sender);
            return true;
        }

        if (args.length < 1) {
            Chat.send(plugin.config.getString("messages.usage"), sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length < 2) {
                Chat.send(plugin.config.getString("messages.usage"), sender);
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                Chat.send(plugin.config.getString("messages.player-not-found"), sender);
                return true;
            }

            plugin.addRunner(player);
            Chat.send(plugin.config.getString("messages.player-added").replace("{player}", player.getName()), sender);
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length < 2) {
                Chat.send(plugin.config.getString("messages.usage"), sender);
                return true;
            }

            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                Chat.send(plugin.config.getString("messages.player-not-found"), sender);
                return true;
            }

            if (plugin.getRunners().contains(player)) {
                plugin.removeRunner(player);
                Chat.send(plugin.config.getString("messages.player-removed").replace("{player}", player.getName()), sender);
            }
        } else if (args[0].equalsIgnoreCase("clear")) {
            plugin.clearRunners();
            Chat.send(plugin.config.getString("messages.runner-cleared"), sender);
        } else if (args[0].equalsIgnoreCase("start")) {
            if (!plugin.isRunning()) {
                plugin.start();
                plugin.getHunters().forEach(plugin::giveCompass);
                Chat.send(plugin.config.getString("messages.listeners-started"), sender);
            } else {
                Chat.send(plugin.config.getString("messages.listeners-already-running"), sender);
            }
        } else if (args[0].equalsIgnoreCase("stop")) {
            if (plugin.isRunning()) {
                plugin.stop();

                Bukkit.getOnlinePlayers().forEach(player -> {
                    Inventory inventory = player.getInventory();
                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (item == null) continue;
                        if (!item.getType().equals(Material.COMPASS)) continue;
                        if (!new NBTItem(item).hasKey("mhc")) continue;

                        inventory.setItem(i, null);
                    }
                });

                Chat.send(plugin.config.getString("messages.listeners-stopped"), sender);
            } else {
                Chat.send(plugin.config.getString("messages.listeners-not-running"), sender);
            }
        } else {
            Chat.send(plugin.config.getString("messages.usage"), sender);
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return TabCompleterUtil.getCompletions(args[0], "add", "remove", "clear", "start", "stop");
        } else if (args.length == 2 && !args[0].equalsIgnoreCase("clear") && !args[0].equalsIgnoreCase("start") && !args[0].equalsIgnoreCase("stop")) {
            return TabCompleterUtil.getPlayers(args[1]);
        } else {
            return null;
        }
    }
}
