package com.extendedclip.chatinjector;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class ChatPacketListener extends PacketAdapter {
    public ChatPacketListener(PlaceholderAPIPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST, Server.CHAT);
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(this);
    }

    public void onPacketSending(PacketEvent e) {
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }

        PacketContainer packet = e.getPacket();
        StructureModifier<WrappedChatComponent> chat = packet.getChatComponents();
        WrappedChatComponent component = chat.read(0);

        if (component == null) {
            return;
        }

        String messageJson = component.getJson();
        if (messageJson == null) {
            return;
        }

        if (!PlaceholderAPI.getBracketPlaceholderPattern().matcher(messageJson).find()) {
            return;
        }

        messageJson = PlaceholderAPI.setBracketPlaceholders(player, messageJson);
        chat.write(0, WrappedChatComponent.fromJson(messageJson));
    }
}
