package org.pedro0508.always_at_night;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.Map;

public class Always_at_night implements ModInitializer {
    // Tiempos en ticks (20 ticks = 1 segundo)
    private static final long PEACE_DURATION = 10 * 60 * 20;
    private static final long MONSTER_DURATION = 5 * 60 * 20;
    
    // Mapa para almacenar el estado de cada mundo
    private final Map<ServerWorld, WorldState> worldStates = new HashMap<>();

    @Override
    public void onInitialize() {
        System.out.println("[Always at Night] Inicializando mod...");
        
        // Registrar eventos
        ServerWorldEvents.LOAD.register(this::onWorldLoad);
        ServerTickEvents.START_WORLD_TICK.register(this::onWorldTick);
    }

    private void onWorldLoad(MinecraftServer server, ServerWorld world) {
        // Solo manejar el mundo normal (Overworld)
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        System.out.println("[Always at Night] Mundo cargado: " + world.getRegistryKey().getValue());
        
        // Configurar reglas del mundo
        world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
        world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
        world.setTimeOfDay(18000);
        
        // Inicializar estado del mundo
        WorldState state = new WorldState();
        state.nextCycleTime = world.getTime() + PEACE_DURATION;
        worldStates.put(world, state);
        
        // Mensaje a jugadores
        server.getPlayerManager().broadcast(
            Text.literal("§e[Always at Night] §f¡Mod activado! Los monstruos aparecerán pronto..."),
            false
        );
        
        System.out.println("[Always at Night] Mundo configurado. Próximo ciclo en: " + 
                          (state.nextCycleTime - world.getTime()) / 20 + " segundos");
    }

    private void onWorldTick(ServerWorld world) {
        // Solo manejar el mundo normal (Overworld)
        if (world.getRegistryKey() != World.OVERWORLD) {
            return;
        }

        WorldState state = worldStates.get(world);
        if (state == null) {
            System.out.println("[Always at Night] Estado no encontrado para el mundo, esperando a que se cargue...");
            return;
        }

        // Mantener siempre de noche
        if (world.getTime() % 200 == 0) { // Solo actualizar cada 10 segundos para mejorar rendimiento
            world.setTimeOfDay(18000);
        }

        // Comprobar si es hora de cambiar el ciclo
        if (world.getTime() >= state.nextCycleTime) {
            state.isMonsterTime = !state.isMonsterTime;
            MinecraftServer server = world.getServer();
            
            if (state.isMonsterTime) {
                // Iniciar fase de monstruos
                state.nextCycleTime = world.getTime() + MONSTER_DURATION;
                world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(true, server);
                
                // Mensaje a jugadores
                server.getPlayerManager().broadcast(
                    Text.literal("§c[Always at Night] §f¡The night grows darker... Monsters are now roaming around..."),
                    false
                );
                
                System.out.println("[Always at Night] Fase de monstruos activada");
            } else {
                // Iniciar fase de paz
                state.nextCycleTime = world.getTime() + PEACE_DURATION;
                world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, server);
                
                // Mensaje a jugadores
                server.getPlayerManager().broadcast(
                    Text.literal("§a[Always at Night] §fThe night becomes calmer... Monsters have stopped appearing."),
                    false
                );
                
                System.out.println("[Always at Night] Fase pacífica activada. Próxima fase de monstruos en: " + 
                                 (state.nextCycleTime - world.getTime()) / 20 + " segundos");
            }
        }
    }
    
    // Clase para almacenar el estado de cada mundo
    private static class WorldState {
        public long nextCycleTime = 0;
        public boolean isMonsterTime = false;
    }
}
