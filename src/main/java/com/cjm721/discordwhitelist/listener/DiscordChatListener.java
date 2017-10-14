package com.cjm721.discordwhitelist.listener;

import com.cjm721.discordwhitelist.DiscordWhitelist;
import com.cjm721.discordwhitelist.config.DiscordWhitelistConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.FMLServerHandler;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageEvent;
import sx.blah.discord.handle.obj.IUser;

import java.util.Arrays;
import java.util.UUID;

public class DiscordChatListener implements IListener<MessageEvent> {

    private static final String prefix = "!register";

    /**
     * Invoked when the {@link EventDispatcher} this listener is registered with fires an event of type {@link MessageEvent}.
     *
     * @param event The event object.
     */
    @Override
    public void handle(MessageEvent event) {
        if(!Arrays.asList(DiscordWhitelistConfig.watchedChannels).contains(event.getChannel().getStringID())) {
            return;
        }

        String message = event.getMessage().getContent();
        if(!message.startsWith(prefix))
            return;

        String[] args = message.split(" ");

        IUser author = event.getMessage().getAuthor();

        if(args.length != 2) {
            event.getChannel().sendMessage("Improper command usage. !register <minecraft user name>");
            return;
        }

        String minecraftUsername = args[1];

        MinecraftServer server = FMLServerHandler.instance().getServer();
        GameProfile gameprofile = server.getPlayerProfileCache().getGameProfileForUsername(minecraftUsername);

        if(gameprofile == null) {
            event.getChannel().sendMessage("Could not find a Minecraft user by that name.");
            return;
        }

        UUID currentUUID = gameprofile.getId();
        UUID previousUUID = DiscordWhitelist.roleRepository.addEntry(author.getStringID(),gameprofile.getId());
        if(previousUUID == null)
            event.getChannel().sendMessage("Added to Repository");
        else if(previousUUID != null) {
            if(currentUUID.equals(previousUUID)) {
                event.getChannel().sendMessage("Already in Repository");
            } else {
                event.getChannel().sendMessage("Added to Repository. This replaces UUID of " + previousUUID);
            }
        }
    }
}
