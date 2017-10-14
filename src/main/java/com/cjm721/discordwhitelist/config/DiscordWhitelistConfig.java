package com.cjm721.discordwhitelist.config;

import com.cjm721.discordwhitelist.DiscordWhitelist;
import net.minecraftforge.common.config.Config;

@Config(modid = DiscordWhitelist.MOD_ID)
public class DiscordWhitelistConfig {
    public static String token = "";
    public static String guildID = "";
    public static String[] watchedChannels = new String[] {};
    public static String[] whitelistGroups = new String[] {};
    public static String notAuthorized = "You are not authorized to connect";
    public static String notRegistered = "Register on Discord before connecting.";
}
