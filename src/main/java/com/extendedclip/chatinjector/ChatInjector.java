package com.extendedclip.chatinjector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Version;

public final class ChatInjector extends JavaPlugin implements Listener {
    private PacketAdapter chat;

    public ChatInjector() {
        this.chat = null;
    }

    @Override
    public void onEnable() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPlugin("ProtocolLib") == null) {
            throw new RuntimeException("Failed to detect ProtocolLib.");
        }

        if (pluginManager.getPlugin("PlaceholderAPI") == null) {
            throw new RuntimeException("Failed to detect PlaceholderAPI.");
        }

        Version serverVersion = PlaceholderAPIPlugin.getServerVersion();
        if (serverVersion.isSpigot()) {
            this.chat = new SpigotChatPacketListener();
        } else {
            this.chat = new ChatPacketListener();
        }

        pluginManager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (this.chat == null) {
            return;
        }

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.removePacketListener(this.chat);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        String message = e.getMessage();

        Pattern bracketPlaceholderPattern = PlaceholderAPI.getBracketPlaceholderPattern();
        Matcher matcher = bracketPlaceholderPattern.matcher(message);

        if (player.hasPermission("chatinjector.parse")) {
            if (matcher.find()) {
                message = PlaceholderAPI.setBracketPlaceholders(player, message);
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
            format = PlaceholderAPI.setBracketPlaceholders(player, format);
            e.setFormat(format);
        }
    }
}
