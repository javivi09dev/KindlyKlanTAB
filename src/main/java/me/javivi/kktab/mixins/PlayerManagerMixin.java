package me.javivi.kktab.mixins;

import me.javivi.kktab.KindlyKlanTab;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerJoin(ServerPlayerEntity player, CallbackInfo ci) {
        // Actualizar el TAB cuando un jugador se conecta
        try {
            if (KindlyKlanTab.getTabManager() != null) {
                KindlyKlanTab.getTabManager().updateTabList();
            }
        } catch (Exception e) {
            KindlyKlanTab.LOGGER.error("Error actualizando TAB al conectar jugador", e);
        }
    }
    
    @Inject(method = "remove", at = @At("HEAD"))
    private void onPlayerLeave(ServerPlayerEntity player, CallbackInfo ci) {
        // Actualizar el TAB cuando un jugador se desconecta
        try {
            if (KindlyKlanTab.getTabManager() != null) {
                // Programar la actualización para después de que el jugador sea removido
                KindlyKlanTab.getTabManager().updateTabList();
            }
        } catch (Exception e) {
            KindlyKlanTab.LOGGER.error("Error actualizando TAB al desconectar jugador", e);
        }
    }
} 