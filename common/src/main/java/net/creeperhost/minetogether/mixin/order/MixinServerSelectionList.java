package net.creeperhost.minetogether.mixin.order;

import net.creeperhost.minetogether.config.Config;
import net.creeperhost.minetogether.connect.LanServerInfoConnect;
import net.creeperhost.minetogether.connect.OurServerListEntryLanDetected;
import net.creeperhost.minetogether.orderform.CreeperHostServerEntry;
import net.creeperhost.minetogether.serverlist.gui.JoinMultiplayerScreenPublic;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin (ServerSelectionList.class)
public class MixinServerSelectionList {

    @Shadow
    @Final
    private JoinMultiplayerScreen screen;

    @Inject (at = @At ("RETURN"), method = "refreshEntries()V")
    private void afterRefreshEntries(CallbackInfo info) {
        if (!Config.instance().mpMenuEnabled || screen instanceof JoinMultiplayerScreenPublic) return;
        ServerSelectionList thisFake = (ServerSelectionList) (Object) this;
        int size = thisFake.children().size();
        for (int i = 0; i < size; i++) {
            if (thisFake.children().get(i) instanceof ServerSelectionList.NetworkServerEntry realEntry) {
                if (realEntry.getServerData() instanceof LanServerInfoConnect) {
                    thisFake.children().set(i, new OurServerListEntryLanDetected(screen, (LanServerInfoConnect) realEntry.getServerData(), thisFake));
                }
            }
        }
        if (Config.instance().mpMenuEnabled) {
            try {
                thisFake.children().add(size, new CreeperHostServerEntry(thisFake));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
