package com.extendedclip.chatinjector;

import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class ChatInjector extends JavaPlugin implements Listener {
    private PacketAdapter chat;

    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null)
            throw new RuntimeException("Failed to detect ProtocolLib");
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            throw new RuntimeException("Failed to detect PlaceholderAPI");
        }
        if (PlaceholderAPIPlugin.getServerVersion().isSpigot()) {
            this.chat = new SpigotChatPacketListener();
        } else {
            this.chat = new ChatPacketListener();
        }
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    public void onDisable() {
        if (this.chat != null) {
            ProtocolLibrary.getProtocolManager().removePacketListener(this.chat);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        String message = e.getMessage();

        Matcher matcher = PlaceholderAPI.getBracketPlaceholderPattern().matcher(message);

        if (p.hasPermission("chatinjector.parse")) {
            if (matcher.find()) {
                message = PlaceholderAPI.setBracketPlaceholders(p, message);
            }

        } else {
            while (matcher.find()) {
                message = matcher.replaceAll("");
            }
        }

        if (message.isEmpty()) {
            e.setCancelled(true);
            return;
        }

        e.setMessage(message);

        String format = e.getFormat();

        matcher = PlaceholderAPI.getBracketPlaceholderPattern().matcher(format);

        if (matcher.find()) {
            format = PlaceholderAPI.setBracketPlaceholders(p, format);
            e.setFormat(format);
        }
    }
}
