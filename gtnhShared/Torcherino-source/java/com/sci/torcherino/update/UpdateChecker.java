package com.sci.torcherino.update;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import tv.twitch.chat.ChatMessage;

import static net.minecraft.util.EnumChatFormatting.*;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
public final class UpdateChecker {
    private IUpdatableMod mod;

    private UpdateChecker(final IUpdatableMod mod) {
        this.mod = mod;
        this.thread = new UpdateCheckThread(mod);
        this.thread.start();
    }

    private int lastPoll = 100;
    private boolean displayed = false;

    private UpdateCheckThread thread;

    @SubscribeEvent
    public void tickStart(final TickEvent.PlayerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.START)
            return;

        if (this.lastPoll > 0) {
            this.lastPoll--;
            return;
        }
        this.lastPoll = 400;

        if (!this.displayed && this.thread.checkComplete()) {
            this.displayed = true;
            FMLCommonHandler.instance().bus().unregister(this);

            if (this.thread.newVersionAvailable()) {
                final EntityPlayer player = evt.player;

                player.addChatMessage(new ChatComponentText(GOLD + "[" + this.mod.name() + "] " + WHITE + "A new version is available: " + AQUA + this.thread.latest().toString()));
                player.addChatMessage(new ChatComponentText(GRAY + this.thread.description()));
            }

            try {
                this.thread.interrupt();
                this.thread.join();
            } catch (final Throwable ignored) {
                // don't care
            } finally {
                this.thread = null;
            }
        }
    }

    public static UpdateChecker register(final IUpdatableMod updatableMod) {
        final UpdateChecker checker = new UpdateChecker(updatableMod);
        FMLCommonHandler.instance().bus().register(checker);
        return checker;
    }
}