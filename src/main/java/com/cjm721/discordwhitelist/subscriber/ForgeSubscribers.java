package com.cjm721.discordwhitelist.subscriber;

import com.cjm721.discordwhitelist.DiscordWhitelist;
import com.cjm721.discordwhitelist.config.DiscordWhitelistConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import javax.annotation.Nonnull;
import java.util.*;

import static com.cjm721.discordwhitelist.config.DiscordWhitelistConfig.guildID;

public class ForgeSubscribers {

    Map<UUID,ITextComponent> disconnectMap = new Hashtable<>();

    @SubscribeEvent
    public void connectFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        INetHandlerPlayServer handler = event.getHandler();

        if (!(handler instanceof NetHandlerPlayServer))
            return;

        EntityPlayerMP player = ((NetHandlerPlayServer) handler).player;
        UUID uuid = player.getGameProfile().getId();

        String discordID = DiscordWhitelist.roleRepository.getDiscordId(uuid);

        if(discordID == null) {
            disconnectMap.put(uuid, new TextComponentString(DiscordWhitelistConfig.notRegistered));
            return;
        }

        new Thread(() -> {
            IUser user = DiscordWhitelist.discordClient.getUserByID(Long.parseLong(discordID));

            List<IRole> roles = user.getRolesForGuild(DiscordWhitelist.discordClient.getGuildByID(Long.parseLong(guildID)));

            if(!isWhiteListed(roles)) {
                disconnectMap.put(uuid, new TextComponentString(DiscordWhitelistConfig.notAuthorized));
            }
        },"CheckUser" + discordID).start();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if(event.phase != TickEvent.Phase.END)
            return;

        if(disconnectMap.size() == 0)
            return;

        Collection<UUID> toRemove = new ArrayList<>();
        for(Map.Entry<UUID,ITextComponent> entry: disconnectMap.entrySet()) {
            MinecraftServer server = FMLServerHandler.instance().getServer();

            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entry.getKey());

            // If you kick to soon the client will just say disconnected
            if(player == null || player.ticksExisted < 40)
                continue;

            DiscordWhitelist.logger.info(player.ticksExisted);

            player.connection.disconnect(entry.getValue());
            toRemove.add(entry.getKey());
        }

        for(UUID id: toRemove)
            disconnectMap.remove(id);
    }

    private boolean isWhiteListed(@Nonnull List<IRole> roles) {
        return roles.stream()
                .map(IRole::getName)
                .anyMatch(name ->
                        Arrays.asList(DiscordWhitelistConfig.whitelistGroups).contains(name));
    }
}
