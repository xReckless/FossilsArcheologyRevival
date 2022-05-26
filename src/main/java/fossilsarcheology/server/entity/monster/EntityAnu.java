package fossilsarcheology.server.entity.monster;

import fossilsarcheology.Revival;
import fossilsarcheology.client.sound.FASoundRegistry;
import fossilsarcheology.server.block.FABlockRegistry;
import fossilsarcheology.server.entity.ai.AnuAIAttackOnCollide;
import fossilsarcheology.server.entity.ai.AnuAIAvoidEntity;
import fossilsarcheology.server.entity.ai.AnuAIFireballAttack;
import fossilsarcheology.server.entity.utility.EntityAncientLightning;
import fossilsarcheology.server.entity.utility.EntityAnuDead;
import fossilsarcheology.server.entity.utility.FossilsPlayerProperties;
import fossilsarcheology.server.item.FAItemRegistry;
import fossilsarcheology.server.world.gen.SpikesBlockWorldGen;
import net.ilexiconn.llibrary.server.entity.EntityPropertiesHandler;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.List;

public class EntityAnu extends EntityMob implements IRangedAttackMob {

    private static final DataParameter<Integer> ATTACK_MODE = EntityDataManager.createKey(EntityAnu.class, DataSerializers.VARINT);
    public final int middleHealth = 400;
    public final int finalHealth = 200;
    // length of song in ticks
    public final int songLength = 4041;
    private final BossInfoServer bossInfo = (BossInfoServer) (new BossInfoServer(new TextComponentString(TextFormatting.DARK_RED + this.getDisplayName().getFormattedText()), BossInfo.Color.RED, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);
    public int allHealth = 600;
    public int songCounter = 0;
    public boolean isFlying;
    private BlockPos currentTarget;

    public EntityAnu(World world) {
        super(world);
        this.setSize(1F, 1.8F);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityLivingBase.class, 8.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        AnuAIAvoidEntity aiFear = new AnuAIAvoidEntity(this, EntityPlayer.class, 5.0F, 0.8D, 1.33D);
        this.tasks.addTask(4, aiFear);
        AnuAIFireballAttack aiFireballAttack = new AnuAIFireballAttack(this, 1.0D, 10, 20, 15.0F);
        this.tasks.addTask(5, aiFireballAttack);
        AnuAIAttackOnCollide aiAttackOnCollide = new AnuAIAttackOnCollide(this, EntityPlayer.class, 1.2D, false);
        this.tasks.addTask(5, aiAttackOnCollide);
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.isImmuneToFire = true;
        this.experienceValue = 50;
    }

    public void setFlying(boolean state) {
        isFlying = state;
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(600D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    public void addPotionEffect(PotionEffect potioneffectIn) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.getAttackMode() == 0) {
            return FASoundRegistry.ANU_LAUGH;
        } else {
            return FASoundRegistry.ANU_COUGH;
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ITEM_BREAK;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float var2) {
        Entity targetEntity = damageSource.getTrueSource();

        AxisAlignedBB chatDistance = this.getEntityBoundingBox().expand(30.0D, 30.0D, 30.0D);

        if (targetEntity instanceof EntityGhast) {
            return false;
        } else {
            if (targetEntity instanceof EntityPlayer) {

                if (this.getRNG().nextInt(10) == 0) {
                    if (var2 != 0) {
                        ItemStack itemstack = ((EntityPlayer) targetEntity).inventory.getCurrentItem();

                        if (itemstack != null) {
                            if (itemstack.getItem() != null) {
                                if (itemstack.getItem() == FAItemRegistry.ANCIENT_SWORD) {

                                    if (this.world.isRemote) {
                                        ((EntityPlayer) targetEntity).sendStatusMessage(new TextComponentTranslation("anu.mySword"), false);
                                    }

                                    return super.attackEntityFrom(damageSource, var2);
                                }

                                if (itemstack.getItem() != FAItemRegistry.ANCIENT_SWORD && itemstack.getItem() instanceof ItemSword) {

                                    if (this.world.isRemote) {
                                        ((EntityPlayer) targetEntity).sendStatusMessage(new TextComponentTranslation("anu.draw"), false);
                                    }

                                    return super.attackEntityFrom(damageSource, var2);
                                }

                                if (damageSource.damageType.equals("arrow")) {
                                    if (this.world.isRemote) {
                                        ((EntityPlayer) targetEntity).sendStatusMessage(new TextComponentTranslation("anu.coward"), false);
                                    }

                                    return super.attackEntityFrom(damageSource, var2);
                                } else {

                                }
                            }
                        }
                    }
                }
            }
        }

        return super.attackEntityFrom(damageSource, var2);
    }

    @Override
    public void updateAITasks() {
        if (this.getAttackMode() == 2 || this.getAttackMode() == 1) {
            if (this.ticksExisted % 20 == 0) {
                this.heal(2.0F);
            }
        }
        super.updateAITasks();
    }

    protected Entity findPlayerToAttack() {
        EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 16.0D);

        if (entityplayer != null && this.canEntityBeSeen(entityplayer) && world.isRemote) {
            if (this.getRNG().nextInt(1) == 0) {
                entityplayer.sendStatusMessage(new TextComponentTranslation("anu.hello"), false);
            } else {
                entityplayer.sendStatusMessage(new TextComponentTranslation("anu.fewBeaten"), false);
            }

            // super.findPlayerToAttack();
            return entityplayer;
        }

        return null;
    }

    @Override
    public void onDeath(DamageSource dmg) {
        EntityAnuDead entity = new EntityAnuDead(this.world);
        entity.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
        if (!this.world.isRemote) {
            this.world.spawnEntity(entity);
        }
        unlockDimensionAbilities();
        entity.setHealth(0F);
        EntityPlayer player = this.world.getClosestPlayerToEntity(this, 50);
        if (player != null && world.isRemote) {
            player.sendStatusMessage(new TextComponentTranslation("anu.no"), false);
        }
        super.onDeath(dmg);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (this.getRNG().nextInt(4) == 0) {
            this.world.addWeatherEffect(new EntityAncientLightning(this.world, entity.posX, entity.posY, entity.posZ));
        }
        return super.attackEntityAsMob(entity);
    }

    public void spawnMobs(EntityLiving entity) {
        if (!this.world.isRemote) {
            entity.setLocationAndAngles(this.posX + this.getRNG().nextInt(4), this.posY, this.posZ + this.getRNG().nextInt(4), this.rotationYaw, this.rotationPitch);
            this.world.spawnEntity(entity);
            if (entity instanceof EntitySkeleton) {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            }
            if (entity instanceof EntitySentryPigman) {
                this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }

        }
    }

    @Override
    protected void dropFewItems(boolean par1, int par2) {
        this.dropItem(FAItemRegistry.ANCIENT_KEY, 1);
    }

    @Override
    public void onLivingUpdate() {
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
        if (songCounter < songLength) {
            songCounter++;
        }
        if (songCounter == songLength - 1) {
            songCounter = 0;
        }
        if (songCounter == 1) {
            Revival.PROXY.playSound(FASoundRegistry.MUSIC_ANU);
        }
        if (this.isDead) {
            Revival.PROXY.stopSound(FASoundRegistry.MUSIC_ANU);
        }
        if (this.attackingPlayer != null) {
            if (this.attackingPlayer.isDead) {
                Revival.PROXY.stopSound(FASoundRegistry.MUSIC_ANU);
            }
        }
        super.onLivingUpdate();
        if (this.getAttackMode() == 1 && !this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
        if (this.getAttackMode() == 1) {
            if (!world.isRemote) {
                setFlying(true);
                if (!this.checkGround()) {
                    flyAround();
                }
                if (getAttackTarget() != null && world.getHeight(new BlockPos(this)).getY() > 10) {
                    currentTarget = new BlockPos((int) getAttackTarget().posX + rand.nextInt(20) - rand.nextInt(10), (int) ((int) getAttackTarget().posY + getAttackTarget().getEyeHeight()) + rand.nextInt(20) - rand.nextInt(10), (int) getAttackTarget().posZ + rand.nextInt(40) - rand.nextInt(10));
                    setFlying(false);
                    flyTowardsTarget();
                }
                if (world.getClosestPlayerToEntity(this, 100) != null && this.ticksExisted % 100 == 0) {
                    EntityPlayer player = world.getClosestPlayerToEntity(this, 100);
                    if (this.getEntitySenses().canSee(player) && !player.isCreative()) {
                        this.attackEntityWithRangedAttack(player, 1.0F);
                    }

                }
            }
        }
        if (this.getHealth() < middleHealth && this.getAttackMode() != 1) {
            this.setAttackMode(1);
        }
        if (this.getHealth() < finalHealth && this.getAttackMode() != 2) {
            this.setAttackMode(2);

        }
        if (this.getAttackMode() == 1) {

            for (int i = 0; i < 2; ++i) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0.0D, 0.0D, 0.0D);
            }
        }
        if (this.getAttackMode() == 2) {
            for (int i = 0; i < 2; ++i) {
                this.world.spawnParticle(EnumParticleTypes.DRIP_LAVA, this.posX + (this.rand.nextDouble() - 0.5D) * (double) this.width, this.posY + this.rand.nextDouble() * (double) this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double) this.width, 0.0D, 0.0D, 0.0D);
            }
        }
        if (this.getAttackMode() == 2) {
            int spikechoice = this.getRNG().nextInt(250);
            int defensechoice = this.getRNG().nextInt(500);
            int spawnPigmenChoice = this.getRNG().nextInt(250);
            int spawnWitherChoice = this.getRNG().nextInt(350);
            int spawnBlazeChoice = this.getRNG().nextInt(300);

            if (Revival.CONFIG_OPTIONS.anuBlockPlacing) {

                if (spikechoice == 0) {
                    this.playSound(SoundEvents.BLOCK_STONE_HIT, 1, 1);
                    new SpikesBlockWorldGen().generate(world, this.getRNG(), this.getPosition());
                }
                if (defensechoice == 0) {
                    this.playSound(SoundEvents.BLOCK_STONE_HIT, 1, 1);
                    if (!world.isRemote) {
                        this.generateDefenseHutP1((int) this.posX, (int) this.posY, (int) this.posZ);
                        this.generateDefenseHutP2((int) this.posX, (int) this.posY, (int) this.posZ);
                        this.generateDefenseHutP2((int) this.posX, (int) this.posY + 1, (int) this.posZ);
                        this.generateDefenseHutP2((int) this.posX, (int) this.posY + 2, (int) this.posZ);
                        this.generateDefenseHutP1((int) this.posX, (int) this.posY + 4, (int) this.posZ);
                    }
                }
            }
            if (spawnPigmenChoice == 0) {
                EntityPlayer player = this.world.getClosestPlayerToEntity(this, 50);
                if (player != null && world.isRemote) {

                    player.sendStatusMessage(new TextComponentTranslation("anu.trans"), false);
                }

                this.spawnMobs(new EntitySentryPigman(world));
            }
            if (spawnWitherChoice == 0) {
                EntityWitherSkeleton skeleton = new EntityWitherSkeleton(world);
                skeleton.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                this.spawnMobs(skeleton);
                EntityPlayer player = this.world.getClosestPlayerToEntity(this, 50);
                if (player != null && world.isRemote) {

                    player.sendStatusMessage(new TextComponentTranslation("anu.archers"), false);
                }
            }
            if (spawnBlazeChoice == 0) {
                this.spawnMobs(new EntityBlaze(world));
                EntityPlayer player = this.world.getClosestPlayerToEntity(this, 50);
                if (player != null && world.isRemote) {

                    player.sendStatusMessage(new TextComponentTranslation("anu.blaze"), false);
                }
            }
        }
    }

    public void flyTowardsTarget() {
        if (currentTarget != null) {
            double targetX = currentTarget.getX() + 0.5D - posX;
            double targetY = currentTarget.getY() + 1D - posY;
            double targetZ = currentTarget.getZ() + 0.5D - posZ;
            motionX += (Math.signum(targetX) * 0.5D - motionX) * 0.10000000149011612D;
            motionY += (Math.signum(targetY) * 0.699999988079071D - motionY) * 0.10000000149011612D;
            motionZ += (Math.signum(targetZ) * 0.5D - motionZ) * 0.10000000149011612D;
            float angle = (float) (Math.atan2(motionZ, motionX) * 180.0D / Math.PI) - 90.0F;
            float rotation = MathHelper.wrapDegrees(angle - rotationYaw);
            moveForward = 0.5F;
            rotationYaw += rotation;
        }

    }

    public void flyAround() {
        if (currentTarget != null) {
            if (!world.isAirBlock(new BlockPos(currentTarget.getX(), currentTarget.getY(), currentTarget.getZ())) || currentTarget.getY() < 1) {
                currentTarget = null;
            }
        }


        if (currentTarget == null || rand.nextInt(50) == 0 || currentTarget.getDistance((int) posX, (int) posY, (int) posZ) < 5F) {
            currentTarget = new BlockPos(rand.nextInt(50) - 25, (int) posY + rand.nextInt(30) - 2, rand.nextInt(50) - 25);
        }

        flyTowardsTarget();
    }

    private void generateDefenseHutP2(int x, int y, int z) {
        this.world.setBlockState(new BlockPos(x - 3, y, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 3, y, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 3, y, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 3, y, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 3, y, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 3, y, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 3, y, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 3, y, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 3, y, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 3, y, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y, z + 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y, z + 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y, z + 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y, z + 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y, z + 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y, z - 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y, z - 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y, z - 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y, z - 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y, z - 3), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
    }

    private void generateDefenseHutP1(int x, int y, int z) {
        this.world.setBlockState(new BlockPos(x, y - 1, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y - 1, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y - 1, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y - 1, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y - 1, z), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y - 1, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y - 1, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y - 1, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y - 1, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y - 1, z + 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y - 1, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y - 1, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y - 1, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y - 1, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y - 1, z + 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y - 1, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y - 1, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y - 1, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y - 1, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y - 1, z - 1), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x, y - 1, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 1, y - 1, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x + 2, y - 1, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 1, y - 1, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());
        this.world.setBlockState(new BlockPos(x - 2, y - 1, z - 2), FABlockRegistry.FAKE_OBSIDIAN.getDefaultState());

    }

    public boolean checkGround() {
        return !this.onGround && this.world.isAirBlock(new BlockPos((int) this.posX, (int) this.posY - 1, (int) this.posZ));
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(FAItemRegistry.ANCIENT_SWORD));
    }

    public void initializeMob() {
        this.setAttackMode(0);
        this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(FAItemRegistry.ANCIENT_SWORD));
        EntityPlayer entityplayer = this.world.getClosestPlayer(posX, posY, posZ, 100F, false);

        if (entityplayer != null && world.isRemote) {
            if (this.getRNG().nextInt(1) == 0) {
                entityplayer.sendStatusMessage(new TextComponentTranslation("anu.hello"), false);
            } else {
                entityplayer.sendStatusMessage(new TextComponentTranslation("anu.fewBeaten"), false);
            }
        }
    }

    // 0 == melee, 1 == flight/ranged, 2 == defense
    public int getAttackMode() {
        return this.dataManager.get(ATTACK_MODE);
    }

    // 0 == melee, 1 == flight/ranged, 2 == defense
    public void setAttackMode(int i) {
        this.dataManager.set(ATTACK_MODE, i);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("AttackMode", this.getAttackMode());

    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setAttackMode(par1NBTTagCompound.getInteger("AttackMode"));
        if (this.hasCustomName()) {
            this.bossInfo.setName(this.getDisplayName());
        }
    }

    public void setCustomNameTag(String name) {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_MODE, 0);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData par1EntityLivingData) {
        par1EntityLivingData = super.onInitialSpawn(difficulty, par1EntityLivingData);
        this.initializeMob();
        return par1EntityLivingData;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase entity, float y) {
        double d5 = entity.posX - this.posX;
        double d6 = entity.getEntityBoundingBox().minY + (double) (entity.height / 2.0F) - (this.posY + (double) (this.height / 2.0F));
        double d7 = entity.posZ - this.posZ;
        this.playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1, 1);
        EntityLargeFireball entitylargefireball = new EntityLargeFireball(this.world, this, d5, d6, d7);
        entitylargefireball.explosionPower = 2;
        double d8 = 4.0D;
        Vec3d vec3 = this.getLook(1.0F);
        entitylargefireball.posX = this.posX + vec3.x * d8;
        entitylargefireball.posY = this.posY + (double) (this.height / 2.0F) + 0.5D;
        entitylargefireball.posZ = this.posZ + vec3.z * d8;
        this.world.spawnEntity(entitylargefireball);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {

    }

    public void unlockDimensionAbilities() {
        List<EntityPlayer> players = this.world.getPlayers(EntityPlayer.class, EntitySelectors.NOT_SPECTATING);
        for (EntityPlayer player : players) {
            if (player.dimension == this.dimension) {
                FossilsPlayerProperties properties = EntityPropertiesHandler.INSTANCE.getProperties(player, FossilsPlayerProperties.class);
                if (properties != null) {
                    properties.killedAnu = true;
                }
            }
        }
    }

    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

}
