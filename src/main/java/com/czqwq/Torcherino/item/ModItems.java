package com.czqwq.Torcherino.item;

import net.minecraft.item.Item;

import com.czqwq.Torcherino.Torcherino;
import com.czqwq.Torcherino.entity.EntityTimeAccelerator;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class ModItems {

    public static Item timeVial;
    public static Item imperfectTimeTwister;
    public static Item perfectTimeTwister;

    public static void init() {
        timeVial = new ItemTimeVial();
        GameRegistry.registerItem(timeVial, "timeVial");

        imperfectTimeTwister = new ItemImperfectTimeTwister();
        GameRegistry.registerItem(imperfectTimeTwister, "imperfectTimeTwister");

        perfectTimeTwister = new ItemPerfectTimeTwister();
        GameRegistry.registerItem(perfectTimeTwister, "perfectTimeTwister");

        // Register EntityTimeAccelerator
        EntityRegistry.registerModEntity(
            EntityTimeAccelerator.class,
            "EntityTimeAccelerator",
            0,
            Torcherino.instance,
            80,
            3,
            true);
    }
}
