package com.czqwq.Torcherino;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.czqwq.Torcherino.init.GTRecipes;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = Torcherino.MODID,
    version = Torcherino.VERSION,
    name = "Torcherino",
    acceptedMinecraftVersions = "[1.7.10]")
public class Torcherino {

    public static final String MODID = "Torcherino";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static final String VERSION = "1.0";

    public static boolean hasGregTech = false;

    /**
     * True when NH-Utilities is installed. NH-Utilities ships acceleration mixins for exactly the
     * same targets as this mod (GregTech {@code BaseMetaTileEntity}, {@code MTEAdvAssLine},
     * TecTech {@code MTEResearchStation}, EnderIO {@code TileEntityEnder} and
     * {@code AbstractPoweredMachineEntity}). Mixin keeps one copy of each duplicated member
     * (Torcherino's, see the mixin javadoc) and logs a {@code Skipping method} warning for the
     * other side, which is expected and harmless.
     */
    public static boolean hasNHUtilities = false;

    @Mod.Instance(MODID)
    public static Torcherino instance;

    @SidedProxy(clientSide = "com.czqwq.Torcherino.ClientProxy", serverSide = "com.czqwq.Torcherino.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        // 检测GregTech是否加载
        hasGregTech = Loader.isModLoaded("gregtech");
        // 检测NH-Utilities是否加载 (同源加速mixin, 共存说明见 hasNHUtilities 的 javadoc)
        hasNHUtilities = Loader.isModLoaded("NHUtilities");
        if (hasNHUtilities) {
            LOG.info(
                "NH-Utilities detected: both mods mix into the GregTech/EnderIO acceleration hooks. Torcherino keeps the "
                    + "shared duplicate members; NH-Utilities' copies are skipped with 'Method overwrite conflict ... "
                    + "Skipping method.' warnings during startup. This is the expected coexistence mode.");
        }
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    @Mod.EventHandler
    public void completeInit(FMLLoadCompleteEvent event) {
        GTRecipes.loadRecipes();
    }
}
