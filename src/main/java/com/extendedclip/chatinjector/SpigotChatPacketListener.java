package com.extendedclip.chatinjector;

import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;

public class SpigotChatPacketListener extends PacketAdapter {
    public SpigotChatPacketListener() {
        super(PlaceholderAPIPlugin.getInstance(), ListenerPriority.HIGHEST, Server.CHAT);
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void onPacketSending(PacketEvent e) {
        Player player = e.getPlayer();
        if (player == null) {
            return;
        }

        StructureModifier<WrappedChatComponent> chat = e.getPacket().getChatComponents();
        WrappedChatComponent component = chat.read(0);
        if (component == null) {
            StructureModifier<BaseComponent[]> modifier = e.getPacket().getSpecificModifier(BaseComponent[].class);
            BaseComponent[] components = modifier.readSafely(0);

            if (components == null) {
                return;
            }

            String messageString = ComponentSerializer.toString(components);
            if (messageString == null) {
                return;
            }

            if (!PlaceholderAPI.getBracketPlaceholderPattern().matcher(messageString).find()) {
                return;
            }

            messageString = PlaceholderAPI.setBracketPlaceholders(player, messageString);

            modifier.write(0, ComponentSerializer.parse(messageString));

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
