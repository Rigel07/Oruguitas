package com.orugitas;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Orugita supertierna.
 * - Se doma con semillas (a veces hay que insistir).
 * - Una vez domada es INMORTAL: nada puede matarla.
 * - Clic derecho con la mano vacia: "sigueme" / "quieta".
 * - Agachado + clic derecho con la mano vacia: se sube a tu hombro (hasta 2).
 *   Para bajarla: agachate con las manos vacias durante 1 segundo.
 * - Comida (semillas, bayas, manzana...) la cura; basura (carne podrida, palos...) se la come y a veces deja abono.
 */
public class OrugaEntity extends TamableAnimal {
	private static final EntityDataAccessor<Integer> DATA_VARIANT =
		SynchedEntityData.defineId(OrugaEntity.class, EntityDataSerializers.INT);

	/** Evento de entidad propio para la animacion de masticar (solo cliente). */
	private static final byte EVENT_EAT = 100;

	public int eatAnim;
	private int dismountTicks;

	public OrugaEntity(EntityType<? extends OrugaEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MAX_HEALTH, 10.0D)
			.add(Attributes.MOVEMENT_SPEED, 0.22D);
	}

	// ---------------------------------------------------------------- objetos
	private static boolean isSeed(ItemStack stack) {
		Item i = stack.getItem();
		return i == Items.WHEAT_SEEDS || i == Items.MELON_SEEDS || i == Items.PUMPKIN_SEEDS
			|| i == Items.BEETROOT_SEEDS || i == Items.TORCHFLOWER_SEEDS || i == Items.PITCHER_POD;
	}

	private static boolean isTreat(ItemStack stack) {
		Item i = stack.getItem();
		return isSeed(stack) || i == Items.SWEET_BERRIES || i == Items.GLOW_BERRIES || i == Items.APPLE
			|| i == Items.CARROT || i == Items.MELON_SLICE || i == Items.BEETROOT;
	}

	private static boolean isTrash(ItemStack stack) {
		return !stack.isEmpty() && stack.is(Orugitas.TRASH);
	}

	// ------------------------------------------------------------------- IA
	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		goalSelector.addGoal(2, new PanicGoal(this, 1.5D) {
			@Override
			public boolean canUse() {
				return !OrugaEntity.this.isTame() && super.canUse();
			}
		});
		goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT_SEEDS, Items.MELON_SEEDS,
			Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD), false) {
			@Override
			public boolean canUse() {
				return !OrugaEntity.this.isTame() && super.canUse();
			}
		});
		goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.2D, 5.0F, 2.0F, false));
		goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9D));
		goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	// ------------------------------------------------------------- variantes
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(DATA_VARIANT, 0);
	}

	public int getVariant() {
		return entityData.get(DATA_VARIANT);
	}

	public void setVariant(int variant) {
		entityData.set(DATA_VARIANT, Mth.clamp(variant, 0, 2));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("Variant", getVariant());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setVariant(tag.getInt("Variant"));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason,
			SpawnGroupData data, CompoundTag tag) {
		setVariant(random.nextInt(3));
		return super.finalizeSpawn(world, difficulty, reason, data, tag);
	}

	// ------------------------------------------------------------ interaccion
	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		boolean client = level().isClientSide;

		// --- salvaje: solo se doma con semillas
		if (!isTame()) {
			if (isSeed(stack)) {
				if (!client) {
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}
					playSound(SoundEvents.GENERIC_EAT, 0.8F, 1.4F);
					level().broadcastEntityEvent(this, EVENT_EAT);
					if (random.nextInt(3) == 0) {
						tame(player);
						getNavigation().stop();
						setTarget(null);
						setOrderedToSit(false);
						setInSittingPose(false);
						setHealth(getMaxHealth());
						level().broadcastEntityEvent(this, (byte) 7); // corazones
					} else {
						level().broadcastEntityEvent(this, (byte) 6); // humito: sigue intentando
					}
				}
				return InteractionResult.sidedSuccess(client);
			}
			return InteractionResult.PASS;
		}

		// --- domada: comer
		if (isTreat(stack)) {
			eat(player, stack, 4.0F, false);
			return InteractionResult.sidedSuccess(client);
		}
		if (isTrash(stack)) {
			eat(player, stack, 2.0F, true);
			return InteractionResult.sidedSuccess(client);
		}

		// --- domada: ordenes del dueno (mano principal vacia)
		if (hand == InteractionHand.MAIN_HAND && stack.isEmpty() && isOwnedBy(player)) {
			ItemStack off = player.getOffhandItem();
			if (isTreat(off) || isTrash(off)) {
				return InteractionResult.PASS; // se procesara con la otra mano
			}
			if (!client) {
				if (player.isSecondaryUseActive()) {
					putOnShoulder(player);
				} else {
					boolean sit = !isOrderedToSit();
					setOrderedToSit(sit);
					setInSittingPose(sit);
					getNavigation().stop();
					setTarget(null);
					player.displayClientMessage(Component.translatable(sit ? "msg.orugitas.stay" : "msg.orugitas.follow"), true);
					playSound(SoundEvents.AXOLOTL_IDLE_AIR, 0.8F, 1.6F);
				}
			}
			return InteractionResult.sidedSuccess(client);
		}
		return InteractionResult.PASS;
	}

	private void eat(Player player, ItemStack stack, float healAmount, boolean trash) {
		ItemStack shown = new ItemStack(stack.getItem());
		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}
		if (level() instanceof ServerLevel server) {
			heal(healAmount);
			level().broadcastEntityEvent(this, EVENT_EAT);
			playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.5F);
			server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, shown),
				getX(), getY() + 0.3D, getZ(), 8, 0.2D, 0.1D, 0.2D, 0.05D);
			server.sendParticles(ParticleTypes.HEART,
				getX(), getY() + getBbHeight() + 0.2D, getZ(), 3, 0.25D, 0.15D, 0.25D, 0.02D);
			if (trash) {
				playSound(SoundEvents.PLAYER_BURP, 0.6F, 1.8F);
				if (random.nextInt(4) == 0) {
					spawnAtLocation(Items.BONE_MEAL); // agradece la basura con abono
				}
			}
		}
	}

	private void putOnShoulder(Player player) {
		if (player.getPassengers().size() >= 2) {
			player.displayClientMessage(Component.translatable("msg.orugitas.shoulder_full"), true);
			return;
		}
		setOrderedToSit(false);
		setInSittingPose(false);
		getNavigation().stop();
		if (startRiding(player, true)) {
			player.displayClientMessage(Component.translatable("msg.orugitas.shoulder"), true);
			playSound(SoundEvents.AXOLOTL_IDLE_AIR, 0.8F, 1.8F);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == EVENT_EAT) {
			eatAnim = 30;
		} else {
			super.handleEntityEvent(id);
		}
	}

	// ------------------------------------------------------- en el hombro
	@Override
	public void rideTick() {
		super.rideTick();
		Entity vehicle = getVehicle();
		if (vehicle instanceof Player player) {
			int idx = Math.max(0, player.getPassengers().indexOf(this));
			double side = (idx % 2 == 0) ? 1.0D : -1.0D; // derecho / izquierdo
			double yaw = Math.toRadians(player.yBodyRot);
			double sin = Math.sin(yaw);
			double cos = Math.cos(yaw);
			double x = player.getX() + (-cos * side * 0.30D) + (-sin * 0.02D);
			double z = player.getZ() + (-sin * side * 0.30D) + (cos * 0.02D);
			double y = player.getY() + (player.isCrouching() ? 1.22D : 1.45D);
			setPos(x, y, z);
		}
	}

	@Override
	public boolean isPickable() {
		return super.isPickable() && !isPassenger();
	}

	@Override
	public boolean isPushable() {
		return super.isPushable() && !isPassenger();
	}

	@Override
	public void tick() {
		super.tick();
		if (eatAnim > 0) {
			eatAnim--;
		}
		if (!level().isClientSide) {
			if (getVehicle() instanceof Player player) {
				boolean wantsDown = player.isShiftKeyDown() && player.getMainHandItem().isEmpty()
					&& player.getOffhandItem().isEmpty();
				dismountTicks = wantsDown ? dismountTicks + 1 : 0;
				if (dismountTicks >= 20 || !isTame() || !isOwnedBy(player)) {
					dismountTicks = 0;
					stopRiding();
					moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0.0F);
					playSound(SoundEvents.AXOLOTL_IDLE_AIR, 0.8F, 1.4F);
				}
			} else {
				dismountTicks = 0;
			}
		}
	}

	// ------------------------------------------------------------ inmortal
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		return isTame() || super.isInvulnerableTo(source);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Si cae al vacio, vuelve con su dueno en lugar de perderse.
		if (!level().isClientSide && isTame() && getY() < level().getMinBuildHeight() - 16) {
			LivingEntity owner = getOwner();
			if (owner != null && owner.level() == level()) {
				moveTo(owner.getX(), owner.getY(), owner.getZ(), getYRot(), 0.0F);
			} else {
				BlockPos spawn = level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, level().getSharedSpawnPos());
				moveTo(spawn.getX() + 0.5D, spawn.getY() + 1.0D, spawn.getZ() + 0.5D, getYRot(), 0.0F);
			}
			setDeltaMovement(0.0D, 0.0D, 0.0D);
			fallDistance = 0.0F;
		}
	}

	// --------------------------------------------------------------- varios
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
		return null; // no se reproducen
	}

	@Override
	public boolean canMate(Animal other) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.AXOLOTL_IDLE_AIR;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.AXOLOTL_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.AXOLOTL_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(SoundEvents.WOOL_STEP, 0.15F, 1.4F);
	}
}
