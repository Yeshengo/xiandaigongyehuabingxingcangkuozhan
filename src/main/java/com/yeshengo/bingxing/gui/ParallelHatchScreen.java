package com.yeshengo.bingxing.gui;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.network.UpdateParallelLevelPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ParallelHatchScreen extends AbstractContainerScreen<ParallelHatchMenu> {

    private static final ResourceLocation TEXTURE = BingXing.id("textures/gui/parallel_hatch.png");
    private EditBox levelInput;

    public ParallelHatchScreen(ParallelHatchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int maxLevel = menu.getMaxLevel();

        // 减号按钮
        addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            int newLevel = menu.getParallelLevel() - 1;
            if (newLevel >= 1) {
                updateLevel(newLevel);
            }
        }).pos(x + 30, y + 30).size(20, 20).build());

        // 加号按钮
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            int newLevel = menu.getParallelLevel() + 1;
            if (newLevel <= maxLevel) {
                updateLevel(newLevel);
            }
        }).pos(x + 120, y + 30).size(20, 20).build());

        // 输入框
        levelInput = new EditBox(font, x + 55, y + 30, 60, 20, Component.translatable("gui.bingxing.parallel_level"));
        levelInput.setValue(String.valueOf(menu.getParallelLevel()));
        levelInput.setFilter(s -> {
            if (s.isEmpty()) return true;
            try {
                int val = Integer.parseInt(s);
                return val >= 1 && val <= maxLevel;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        levelInput.setResponder(s -> {
            if (!s.isEmpty()) {
                try {
                    int val = Integer.parseInt(s);
                    if (val != menu.getParallelLevel()) {
                        updateLevel(val);
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
        addRenderableWidget(levelInput);
    }

    private void updateLevel(int newLevel) {
        int maxLevel = menu.getMaxLevel();
        int clamped = Math.clamp(newLevel, 1, maxLevel);
        if (clamped != menu.getParallelLevel()) {
            menu.setParallelLevel(clamped);
            PacketDistributor.sendToServer(new UpdateParallelLevelPacket(menu.getBlockPos(), clamped));
            levelInput.setValue(String.valueOf(clamped));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, Component.translatable("gui.bingxing.parallel_level"), 8, 34, 4210752, false);
        String levelText = menu.getParallelLevel() + "/" + menu.getMaxLevel();
        graphics.drawString(this.font, levelText, 75, 34, 0x000000, false);
    }
}