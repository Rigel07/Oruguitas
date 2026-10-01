package com.orugitas;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Orugitas.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
	@SubscribeEvent
	public static void attributes(EntityAttributeCreationEvent event) {
		event.put(Orugitas.ORUGA.get(), OrugaEntity.createAttributes().build());
	}

	@SubscribeEvent
	public static void spawns(SpawnPlacementRegisterEvent event) {
		// Aparece sobre hierba y con luz de dia, como los animales normales.
		event.register(Orugitas.ORUGA.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
			(type, level, reason, pos, random) -> Animal.checkAnimalSpawnRules(type, level, reason, pos, random),
			SpawnPlacementRegisterEvent.Operation.OR);
	}
}
