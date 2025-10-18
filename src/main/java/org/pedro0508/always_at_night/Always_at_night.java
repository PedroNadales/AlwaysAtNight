package org.pedro0508.always_at_night;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameRules;
import java.util.Random;

public class Always_at_night implements ModInitializer {
    private static final long PEACE_DURATION_MIN = 10 * 60 * 20; // 10 minutes in ticks (min)
    private static final long PEACE_DURATION_MAX = 15 * 60 * 20; // 15 minutes in ticks (max)
    private static final long MONSTER_DURATION = 8 * 60 * 20; // 8 minutes in ticks
    
    private long nextCycleTime;
    private boolean isMonsterTime = false;
    private final Random random = new Random();

    @Override
    public void onInitialize() {
        // Evento para ajustar el tiempo del mundo y controlar el ciclo de monstruos
        ServerTickEvents.START_WORLD_TICK.register(this::handleWorldTick);
    }

    private void handleWorldTick(ServerWorld world) {
        // Mantener siempre de noche
        world.setTimeOfDay(18000);
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
        
        // Inicializar nextCycleTime si es la primera vez
        if (nextCycleTime == 0) {
            nextCycleTime = world.getTime() + getRandomPeaceDuration();
        }
        
        // Comprobar si es hora de cambiar el ciclo
        if (world.getTime() >= nextCycleTime) {
            isMonsterTime = !isMonsterTime;
            
            if (isMonsterTime) {
                // Iniciar fase de monstruos (8 minutos)
                nextCycleTime = world.getTime() + MONSTER_DURATION;
                world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, world.getServer());
            } else {
                // Iniciar fase de paz (10-15 minutos)
                nextCycleTime = world.getTime() + getRandomPeaceDuration();
                world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, world.getServer());
            }
        }
    }
    
    private long getRandomPeaceDuration() {
        return PEACE_DURATION_MIN + random.nextInt((int)(PEACE_DURATION_MAX - PEACE_DURATION_MIN + 1));
    }
}
