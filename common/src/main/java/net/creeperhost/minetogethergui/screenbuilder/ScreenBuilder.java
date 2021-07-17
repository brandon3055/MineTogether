package net.creeperhost.minetogethergui.screenbuilder;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ScreenBuilder
{
    private final ResourceLocation resourceLocation;

    public ScreenBuilder(ResourceLocation resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public void drawDefaultBackground(ContainerScreen screen, PoseStack poseStack, int x, int y, int width, int height, int textureXSize, int textureYSize)
    {
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        GuiComponent.blit(poseStack, x, y, 0, 0, width / 2, height / 2, textureXSize, textureYSize);
        GuiComponent.blit(poseStack, x + width / 2, y, 150 - width / 2, 0, width / 2, height / 2, textureXSize, textureYSize );
        GuiComponent.blit(poseStack, x, y + height / 2, 0, 150 - height / 2, width / 2, height / 2, textureXSize, textureYSize);
        GuiComponent.blit(poseStack, x + width / 2, y + height / 2, 150 - width / 2, 150 - height / 2, width / 2, height / 2, textureXSize, textureYSize);
    }

    public void drawPlayerSlots(ContainerScreen screen, PoseStack poseStack, int posX, int posY, boolean center, int textureXSize, int textureYSize)
    {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        if (center)
        {
            posX -= 81;
        }
        for (int y = 0; y < 3; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                GuiComponent.blit(poseStack, posX + x * 18, posY + y * 18, 150, 0, 18, 18, textureXSize, textureYSize);
            }
        }
        for (int x = 0; x < 9; x++)
        {
            GuiComponent.blit(poseStack, posX + x * 18, posY + 58, 150, 0, 18, 18, textureXSize, textureYSize);
        }
    }

    public void drawSlot(ContainerScreen gui, PoseStack poseStack, int posX, int posY, int textureXSize, int textureYSize)
    {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        GuiComponent.blit(poseStack, posX, posY, 150, 0, 18, 18, textureXSize, textureYSize);
    }

    public boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY)
    {
        return ((mouseX >= x && mouseX <= x + xSize) && (mouseY >= y && mouseY <= y + ySize));
    }

    public void drawBigBlueBar(PoseStack poseStack, int x, int y, int value, int max, int mouseX, int mouseY, String suffix, int textureXSize, int textureYSize)
    {
        Minecraft.getInstance().getTextureManager().bind(resourceLocation);
        if (!suffix.equals(""))
        {
            suffix = " " + suffix;
        }
        Screen.blit(poseStack, x, y, 0, 218, 256, 18, textureXSize, textureYSize);
        int j = (int) ((double) value / (double) max * 256);
        if (j < 0)
            j = 0;
        Screen.blit(poseStack, x, y + 4, 0, 236, j, 10, textureXSize, textureYSize);
        Screen.drawCenteredString(poseStack, Minecraft.getInstance().font,  suffix, x + 131, y + 5, 0xFFFFFF);
        if (isInRect(x, y, 114, 18, mouseX, mouseY))
        {
            GlStateManager._disableLighting();
            GlStateManager._color4f(1, 1, 1, 1);
        }
    }

    public int percentage(int MaxValue, int CurrentValue)
    {
        if (CurrentValue == 0) return 0;
        return (int) ((CurrentValue * 100.0f) / MaxValue);
    }
}
