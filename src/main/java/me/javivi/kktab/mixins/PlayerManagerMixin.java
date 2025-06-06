package me.javivi.kktab.mixins;

import me.javivi.kktab.KindlyKlanTab;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    
    @Shadow @Final private List<ServerPlayerEntity> players;
    
    // Interceptar cuando se respawnea/conecta un jugador
    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void onPlayerRespawn(ServerPlayerEntity player, boolean alive, CallbackInfo ci) {
        updateTabAfterPlayerChange();
    }
    
    // Interceptar cuando se desconecta un jugador
    @Inject(method = "remove", at = @At("RETURN"))
    private void onPlayerRemove(ServerPlayerEntity player, CallbackInfo ci) {
        updateTabAfterPlayerChange();
    }
    
    // Método auxiliar para actualizar el TAB de forma segura
    private void updateTabAfterPlayerChange() {
        try {
            if (KindlyKlanTab.getTabManager() != null) {
                // Programar la actualización en el siguiente tick del servidor
                if (!players.isEmpty() && players.get(0).getServer() != null) {
                    players.get(0).getServer().execute(() -> {
                        KindlyKlanTab.getTabManager().updateTabList();
                    });
                }
            }
        } catch (Exception e) {
            KindlyKlanTab.LOGGER.debug("Error actualizando TAB después de cambio de jugador: " + e.getMessage());
        }
    }
} 