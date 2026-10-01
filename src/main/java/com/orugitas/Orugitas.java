package com.orugitas;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Orugitas.MOD_ID)
public class Orugitas {
	public static final String MOD_ID = "orugitas";

	/** Objetos que la orugita se come como "basura" (editable en data/orugitas/tags/items/trash.json). */
	public static final TagKey<Item> TRASH = TagKey.create(Registries.ITEM, new ResourceLocation(MOD_ID, "trash"));

	public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

	public static final RegistryObject<EntityType<OrugaEntity>> ORUGA = ENTITIES.register("oruga",
		() -> EntityType.Builder.of(OrugaEntity::new, MobCategory.CREATURE)
			.sized(0.6F, 0.5F).clientTrackingRange(10).build(MOD_ID + ":oruga"));

	public static final RegistryObject<Item> ORUGA_EGG = ITEMS.register("oruga_spawn_egg",
		() -> new ForgeSpawnEggItem(ORUGA, 0x8FD98B, 0xF7E27A, new Item.Properties()));

	public Orugitas() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ENTITIES.register(bus);
		ITEMS.register(bus);
		bus.addListener(Orugitas::addToTabs);
	}

	private static void addToTabs(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			event.accept(ORUGA_EGG);
		}
	}
}
