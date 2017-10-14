package com.cjm721.discordwhitelist;

import com.cjm721.discordwhitelist.listener.DiscordChatListener;
import com.cjm721.discordwhitelist.subscriber.ForgeSubscribers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.ReadyEvent;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

import static com.cjm721.discordwhitelist.config.DiscordWhitelistConfig.token;

@Mod(
        modid = DiscordWhitelist.MOD_ID,
        name = DiscordWhitelist.MOD_NAME,
        version = DiscordWhitelist.VERSION,
        acceptedMinecraftVersions = "[1.12,1.13)",
        acceptableRemoteVersions = "*",
        useMetadata = true,
        serverSideOnly = true
)
public class DiscordWhitelist {

    public static final String MOD_ID = "discordwhitelist";
    public static final String MOD_NAME = "Discord Whitelist";
    public static final String VERSION = "${mod_version}";

    /**
     * This is the instance of your mod as created by Forge. It will never be null.
     */
    @Mod.Instance(MOD_ID)
    public static DiscordWhitelist INSTANCE;
    public static Logger logger;
    public static RoleRepository roleRepository;
    public static IDiscordClient discordClient;
    public static File repositoryFile;

    private Thread clientStartThread;
    private File configFolder;

    /**
     * This is the first initialization event. Register tile entities here.
     * The registry events below will have fired prior to entry to this method.
     */
    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(new ForgeSubscribers());

        clientStartThread = new Thread(this::startDiscordClient,"StartDiscordBot");
        clientStartThread.start();

        configFolder = new File(event.getModConfigurationDirectory(), "discordData/");
        configFolder.mkdir();

        repositoryFile = new File(configFolder,"roleRepository.json");
        try {
            roleRepository = RoleRepository.create(repositoryFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to init role repository",e);
        }
    }

    private void startDiscordClient() {
        discordClient = new ClientBuilder().withToken(token).login();

        discordClient.getDispatcher().registerListener(new DiscordChatListener());

        try {
            discordClient.getDispatcher().waitFor(ReadyEvent.class);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Discord Bot Ready");
    }

    /**
     * This is the second initialization event. Register custom recipes
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    /**
     * This is the final initialization event. Register actions from other mods here
     */
    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    public void serverStarting(@Nonnull FMLServerStartingEvent event) {
        try {
            clientStartThread.join();
        } catch (InterruptedException e) { }
    }
}
