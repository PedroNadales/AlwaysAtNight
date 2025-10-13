package org.pedro0508.always_at_night;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;

public class Always_at_night implements ModInitializer {

    @Override
    public void onInitialize() {
        // Evento para ajustar el tiempo del mundo
        ServerTickEvents.START_WORLD_TICK.register(this::forceNight);
    }

    private void forceNight(ServerWorld world) {
        world.setTimeOfDay(18000); // Ajusta el tiempo a la mitad de la noche
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer()); // Congela el ciclo del día
    }
}
