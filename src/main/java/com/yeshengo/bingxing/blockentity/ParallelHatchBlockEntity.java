package com.yeshengo.bingxing.blockentity;

import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.gui.ParallelHatchMenu;
import com.yeshengo.bingxing.init.ModBlockEntities;
import com.yeshengo.bingxing.init.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;


import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ParallelHatchBlockEntity extends BlockEntity implements MenuProvider {

    private int parallelLevel = 1;
    private int maxParallel;
    @Nullable
    private MultiblockMachineBlockEntity controller; // 仅服务端有效

    // 客户端和服务端都维护的模型数据
    private MachineModelClientData modelData = new MachineModelClientData();
    // NBT同步的外壳ID
    @Nullable
    private ResourceLocation casingId = null;

    public ParallelHatchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PARALLEL_HATCH.get(), pos, state);
        this.maxParallel = getMaxParallelFromBlock();
    }

    private int getMaxParallelFromBlock() {
        BlockState state = getBlockState();
        if (state.is(ModBlocks.BASIC_PARALLEL_HATCH.get())) return 8;
        if (state.is(ModBlocks.ADVANCED_PARALLEL_HATCH.get())) return 64;
        if (state.is(ModBlocks.ELITE_PARALLEL_HATCH.get())) return 256;
        if (state.is(ModBlocks.ULTIMATE_PARALLEL_HATCH.get())) return 512;
        if (state.is(ModBlocks.CREATIVE_PARALLEL_HATCH.get())) return 2048;
        return 1;
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        if (controller == null && level.getGameTime() % 20 == 0) {
            findAndSetController();
        }
    }

    private void findAndSetController() {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    mutablePos.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    BlockEntity be = level.getBlockEntity(mutablePos);
                    if (be instanceof MultiblockMachineBlockEntity multi) {
                        setController(multi);
                        return;
                    }
                }
            }
        }
    }

    public int getParallelLevel() { return parallelLevel; }
    public int getMaxParallel() { return maxParallel; }

    public void setParallelLevel(int newLevel) {
        int clamped = Math.clamp(newLevel, 1, maxParallel);
        if (this.parallelLevel == clamped) return;
        this.parallelLevel = clamped;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getChunkSource().blockChanged(worldPosition);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("parallelLevel", parallelLevel);
        if (casingId != null) {
            tag.putString("casingId", casingId.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        parallelLevel = tag.getInt("parallelLevel");
        if (parallelLevel > maxParallel) parallelLevel = maxParallel;

        if (tag.contains("casingId")) {
            casingId = ResourceLocation.tryParse(tag.getString("casingId"));
        } else {
            casingId = null;
        }

        if (level != null && level.isClientSide) {
            refreshModelData();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("parallelLevel", parallelLevel);
        if (casingId != null) {
            tag.putString("casingId", casingId.toString());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        if (level != null && level.isClientSide) {
            if (tag.contains("casingId")) {
                casingId = ResourceLocation.tryParse(tag.getString("casingId"));
            } else {
                casingId = null;
            }
            refreshModelData();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ParallelHatchMenu(containerId, playerInventory, this.worldPosition);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(MachineModelClientData.KEY, modelData)
                .build();
    }

    private void refreshModelData() {
        MachineCasing casing = null;
        if (casingId != null) {
            try {
                casing = MachineCasings.get(casingId);
            } catch (Exception e) {
                BingXing.LOGGER.warn("Failed to get MachineCasing for {}", casingId);
            }
        }
        this.modelData = new MachineModelClientData(casing, null);
        requestModelDataUpdate();
        BingXing.LOGGER.info("Client refreshed modelData with casing: {}", casingId);
    }

    private void updateCasingFromController() {
        MachineCasing casing = null;
        if (controller != null) {
            // 方法1：从控制器的 ModelData 获取
            try {
                ModelData controllerModelData = controller.getModelData();
                if (controllerModelData != null && controllerModelData.has(MachineModelClientData.KEY)) {
                    MachineModelClientData data = controllerModelData.get(MachineModelClientData.KEY);
                    if (data != null && data.casing != null) {
                        casing = data.casing;
                        BingXing.LOGGER.debug("Got casing from controller ModelData: {}", casing.key);
                    }
                }
            } catch (Exception e) {
                BingXing.LOGGER.debug("Failed to get casing from ModelData: {}", e.getMessage());
            }

            // 方法2：反射获取 casingComponent 字段
            if (casing == null) {
                casing = findCasingViaField(controller, "casingComponent");
                if (casing != null) {
                    BingXing.LOGGER.debug("Got casing via casingComponent field");
                }
            }

            // 方法3：反射获取直接名为 casing 的 MachineCasing 字段
            if (casing == null) {
                casing = findCasingViaField(controller, "casing");
                if (casing != null) {
                    BingXing.LOGGER.debug("Got casing via direct 'casing' field");
                }
            }

            // 方法4：遍历所有字段，查找类型为 MachineCasing 的字段
            if (casing == null) {
                casing = findCasingByType(controller, MachineCasing.class);
                if (casing != null) {
                    BingXing.LOGGER.debug("Got casing via MachineCasing type field");
                }
            }

            // 方法5：查找 CasingComponent 并调用 getCasing()
            if (casing == null) {
                try {
                    Class<?> casingCompClass = Class.forName("aztech.modern_industrialization.machines.components.CasingComponent");
                    Object casingComp = findFieldByType(controller, casingCompClass);
                    if (casingComp != null) {
                        Method getCasing = casingComp.getClass().getMethod("getCasing");
                        casing = (MachineCasing) getCasing.invoke(casingComp);
                        BingXing.LOGGER.debug("Got casing via CasingComponent.getCasing()");
                    }
                } catch (Exception ignored) {}
            }

            // 方法6：基于方块 ID 的后备映射
            if (casing == null) {
                BlockState state = controller.getBlockState();
                ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                String blockPath = blockId.getPath();

                // 焦炉 → 红砖外壳（注意复数形式 "bricks"）
                if (blockPath.contains("coke_oven")) {
                    casing = safeGetCasing("bricks");
                    BingXing.LOGGER.debug("Got casing via block ID mapping for coke_oven: {}", casing != null ? casing.key : "null");
                }
                // 蒸汽多方块 → 青铜外壳
                else if (blockPath.contains("steam_")) {
                    casing = MachineCasings.BRONZE;
                    BingXing.LOGGER.debug("Got casing via block ID mapping for steam machine: {}", casing.key);
                }
                // 高炉 → 耐热外壳
                else if (blockPath.contains("blast_furnace") || blockPath.contains("ebf")) {
                    casing = safeGetCasing("heatproof");
                    if (casing == null) {
                        casing = safeGetCasing("steel");
                    }
                    BingXing.LOGGER.debug("Got casing via block ID mapping for blast furnace: {}", casing != null ? casing.key : "null");
                }
                // 真空冷冻机 → 耐寒外壳
                else if (blockPath.contains("vacuum_freezer")) {
                    casing = safeGetCasing("frostproof");
                    BingXing.LOGGER.debug("Got casing via block ID mapping for vacuum freezer: {}", casing != null ? casing.key : "null");
                }
            }

            // 方法7：基于类名的后备映射
            if (casing == null) {
                String className = controller.getClass().getSimpleName();
                if (className.contains("CokeOven")) {
                    casing = safeGetCasing("bricks");
                } else if (className.contains("BlastFurnace") || className.contains("EBF")) {
                    casing = safeGetCasing("heatproof");
                } else if (className.contains("Steam")) {
                    casing = MachineCasings.BRONZE;
                } else if (className.contains("VacuumFreezer")) {
                    casing = safeGetCasing("frostproof");
                } else if (className.contains("Nuclear") || className.contains("Reactor")) {
                    casing = safeGetCasing("nuclear");
                }
                if (casing != null) {
                    BingXing.LOGGER.debug("Got casing via class name mapping: {}", casing.key);
                }
            }
        }

        // 更新字段
        this.casingId = casing != null ? casing.key : null;
        this.modelData = new MachineModelClientData(casing, null);
        setChanged();

        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            BingXing.LOGGER.info("Sent block update to client, casingId: {}", casingId);
        }
    }

    // 新增辅助方法：安全获取外壳，自动尝试单复数变体
    private MachineCasing safeGetCasing(String path) {
        // 第一次尝试：直接使用传入的 ID
        try {
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", path));
        } catch (IllegalArgumentException e) {
            // 第二次尝试：如果 ID 不以 's' 结尾，尝试添加 's'
            if (!path.endsWith("s")) {
                try {
                    return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", path + "s"));
                } catch (IllegalArgumentException ignored) {}
            }
            // 第三次尝试：如果 ID 以 's' 结尾，尝试去掉 's'
            if (path.endsWith("s")) {
                try {
                    return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", path.substring(0, path.length() - 1)));
                } catch (IllegalArgumentException ignored) {}
            }
            BingXing.LOGGER.warn("Machine casing '{}' does not exist in any form", path);
            return null;
        }
    }

// ========== 辅助反射方法 ==========

    /**
     * 在对象及其所有父类中查找指定名称的字段，并尝试提取 MachineCasing
     */
    private MachineCasing findCasingViaField(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value instanceof MachineCasing) {
                    return (MachineCasing) value;
                } else if (value != null) {
                    try {
                        Method getCasing = value.getClass().getMethod("getCasing");
                        Object casing = getCasing.invoke(value);
                        if (casing instanceof MachineCasing) {
                            return (MachineCasing) casing;
                        }
                    } catch (Exception ignored) {}
                }
            } catch (NoSuchFieldException e) {
                // 当前类没有，继续向父类查找
            } catch (Exception e) {
                BingXing.LOGGER.debug("Error accessing field {}: {}", fieldName, e.getMessage());
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 在对象及其所有父类中查找第一个类型匹配的字段
     */
    private MachineCasing findCasingByType(Object obj, Class<?> targetType) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (targetType.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        return (MachineCasing) field.get(obj);
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 查找指定类型的字段对象（不一定是 MachineCasing）
     */
    private Object findFieldByType(Object obj, Class<?> targetType) {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if (targetType.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        return field.get(obj);
                    } catch (Exception ignored) {}
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 根据控制器类名映射到 MI 已知外壳。
     */
    private MachineCasing getCasingByClassName(String className) {
        if (className.contains("Steam")) {
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "bronze"));
        }
        if (className.contains("BlastFurnace") || className.contains("EBF")) {
            MachineCasing heatproof = MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "heatproof"));
            return heatproof != null ? heatproof : MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "steel"));
        }
        if (className.contains("CokeOven")) {
            MachineCasing brick = MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "brick"));
            return brick != null ? brick : MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "fireclay_brick"));
        }
        if (className.contains("VacuumFreezer")) {
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "frostproof"));
        }
        if (className.contains("Nuclear") || className.contains("Reactor")) {
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "nuclear"));
        }
        if (className.contains("Titanium")) {
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "titanium"));
        }
        if (className.contains("Stainless")) {
            // 尝试多种可能的ID
            MachineCasing stainless = MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "stainless_steel"));
            if (stainless != null) return stainless;
            return MachineCasings.get(ResourceLocation.fromNamespaceAndPath("modern_industrialization", "clean_stainless_steel"));
        }
        return null;
    }

    public void setController(MultiblockMachineBlockEntity controller) {
        this.controller = controller;
        BingXing.LOGGER.info("Controller set for parallel hatch at {}: {}", worldPosition, controller);
        updateCasingFromController();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            refreshModelData();
        }
    }

    @Nullable
    public MultiblockMachineBlockEntity getController() {
        return controller;
    }

    @Nullable
    public ResourceLocation getCasingId() {
        return casingId;
    }

    // 在 ParallelHatchBlockEntity 中添加
    public void findAndSetControllerNow(Level world) {
        if (world.isClientSide) return;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    mutablePos.set(worldPosition.getX() + dx, worldPosition.getY() + dy, worldPosition.getZ() + dz);
                    BlockEntity be = world.getBlockEntity(mutablePos);
                    if (be instanceof MultiblockMachineBlockEntity multi) {
                        setController(multi);
                        return;
                    }
                }
            }
        }
    }

}