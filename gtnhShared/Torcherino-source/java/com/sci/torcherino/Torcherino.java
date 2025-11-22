package com.sci.torcherino;

import com.sci.torcherino.init.ModBlocks;
import com.sci.torcherino.init.Recipes;
import com.sci.torcherino.update.IUpdatableMod;
import com.sci.torcherino.update.ModVersion;
import com.sci.torcherino.update.UpdateChecker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
@Mod(modid = Props.ID, name = Props.NAME, version = Props.VERSION)
public class Torcherino implements IUpdatableMod {
    private static Torcherino instance;

    @Mod.InstanceFactory
    public static Torcherino instance() {
        if (Torcherino.instance == null)
            Torcherino.instance = new Torcherino();
        return Torcherino.instance;
    }

    private Torcherino() {
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent evt) {
        final File folder = new File(evt.getModConfigurationDirectory(), "sci4me");

        if (!folder.exists())
            folder.mkdir();

        UpdateChecker.register(this);

        final Configuration cfg = new Configuration(new File(folder, "Torcherino.cfg"));
        try {
            cfg.load();

            ModBlocks.init();
            Recipes.init(cfg);
        } finally {
            if (cfg.hasChanged())
                cfg.save();
        }
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent evt) {
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent evt) {
    }

    @Override
    public String name() {
        return Props.NAME;
    }

    @Override
    public String updateURL() {
        return Props.UPDATE_URL;
    }

    @Override
    public ModVersion version() {
        return ModVersion.parse(Props.VERSION);
    }
}