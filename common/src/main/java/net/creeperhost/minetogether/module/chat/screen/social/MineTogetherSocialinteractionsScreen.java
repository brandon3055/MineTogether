package net.creeperhost.minetogether.module.chat.screen.social;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.module.chat.screen.social.MineTogetherSocialInteractionsPlayerList;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class MineTogetherSocialinteractionsScreen extends Screen
{
    public static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
    private MineTogetherSocialInteractionsPlayerList socialInteractionsPlayerList;
    private Button allButton;
    private Button friendsButton;
    private Button blockedButton;
    private EditBox searchBox;
    private String lastSearch = "";
    private Page page;

    private boolean initialized;

    public MineTogetherSocialinteractionsScreen()
    {
        super(new TranslatableComponent("minetogether.socialscreee.title"));
        this.page = Page.ALL;
    }

    @Override
    public void init()
    {
        minecraft.keyboardHandler.setSendRepeatsToGui(true);
        if (initialized)
        {
            socialInteractionsPlayerList.updateSize(width, height, 88, listEnd());
        }
        else
        {
            socialInteractionsPlayerList = new MineTogetherSocialInteractionsPlayerList(this, minecraft, width, height, 88, listEnd(), 36);
        }

        int i = socialInteractionsPlayerList.getRowWidth() / 3;
        int j = socialInteractionsPlayerList.getRowLeft();
        int k = socialInteractionsPlayerList.getRowRight();

        allButton = addButton(new Button(j, 45, i, 20, new TranslatableComponent("gui.socialInteractions.tab_all"), (button) -> {
            showPage(Page.ALL);
        }));
        friendsButton = addButton(new Button((j + k - i) / 2 + 1, 45, i, 20, new TranslatableComponent("minetogether.socialInteractions.tab_friends"), (button) -> {
            showPage(Page.FRIENDS);
        }));
        blockedButton = addButton(new Button(k - i + 1, 45, i, 20, new TranslatableComponent("gui.socialInteractions.tab_blocked"), (button) -> {
            showPage(Page.BLOCKED);
        }));

        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = new EditBox(font, marginX() + 28, 78, 196, 16, (new TranslatableComponent("gui.socialInteractions.search_hint")).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
        searchBox.setMaxLength(16);
        searchBox.setBordered(false);
        searchBox.setVisible(true);
        searchBox.setTextColor(16777215);
        searchBox.setValue(string);
        children.add(searchBox);
        children.add(socialInteractionsPlayerList);
        initialized = true;

        socialInteractionsPlayerList.updateList(searchBox.getValue(), page);
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderBackground(poseStack);

        if(!socialInteractionsPlayerList.list.isEmpty()) socialInteractionsPlayerList.render(poseStack, i, j, f);

        if (!searchBox.isFocused() && searchBox.getValue().isEmpty())
        {
            drawString(poseStack, minecraft.font, (new TranslatableComponent("gui.socialInteractions.search_hint")).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY), searchBox.x, searchBox.y, -1);
        }
        else
        {
            searchBox.render(poseStack, i, j, f);
        }
        super.render(poseStack, i, j, f);
    }

    @Override
    public void tick()
    {
        super.tick();
        searchBox.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if(searchBox.isFocused())
        {
            boolean flag = searchBox.keyPressed(i, j, k);
            socialInteractionsPlayerList.updateList(searchBox.getValue(), page);
            return flag;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if(searchBox.isFocused())
        {
            boolean flag = searchBox.charTyped(c, i);
            socialInteractionsPlayerList.updateList(searchBox.getValue(), page);
            return flag;
        }
        return super.charTyped(c, i);
    }

    public void showPage(Page page)
    {
        this.page = page;
        socialInteractionsPlayerList.updateList(searchBox.getValue(), page);
        socialInteractionsPlayerList.setScrollAmount(0);
    }

    public void renderBackground(PoseStack poseStack)
    {
        int i = marginX() + 3;
        super.renderBackground(poseStack);
        minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
        blit(poseStack, i, 64, 1, 1, 236, 8);
        int j = backgroundUnits();

        for(int k = 0; k < j; ++k)
        {
            blit(poseStack, i, 72 + 16 * k, 1, 10, 236, 16);
        }

        blit(poseStack, i, 72 + 16 * j, 1, 27, 236, 8);
        blit(poseStack, i + 10, 76, 243, 1, 12, 12);
    }

    private int marginX()
    {
        return (width - 238) / 2;
    }

    private int backgroundUnits()
    {
        return windowHeight() / 16;
    }

    private int windowHeight()
    {
        return Math.max(52, height - 128 - 16);
    }

    private int listEnd()
    {
        return 80 + backgroundUnits() * 16 - 8;
    }

    public Page getPage()
    {
        return page;
    }

    @Environment(EnvType.CLIENT)
    public static enum Page {
        ALL,
        FRIENDS,
        BLOCKED;

        private Page() {
        }
    }
}
