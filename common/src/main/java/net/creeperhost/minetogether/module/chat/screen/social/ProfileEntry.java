package net.creeperhost.minetogether.module.chat.screen.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.module.chat.ChatModule;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.List;

public class ProfileEntry extends Entry<ProfileEntry>
{
    private final ResourceLocation resourceLocation = new ResourceLocation("textures/gui/social_interactions.png");
    private final Profile profile;
    private final List<GuiEventListener> children;
    private final Minecraft minecraft = Minecraft.getInstance();
    private final Button removeButton;
    private final Button muteButton;
    private final Button openDMButton;

    private final MineTogetherSocialinteractionsScreen mineTogetherSocialinteractionsScreen;

    public ProfileEntry(Profile profile, MineTogetherSocialinteractionsScreen mineTogetherSocialinteractionsScreen)
    {
        this.profile = profile;
        this.mineTogetherSocialinteractionsScreen = mineTogetherSocialinteractionsScreen;

        removeButton = new ButtonString(0, 0, 20, 20, new TranslatableComponent(ChatFormatting.RED + new String(Character.toChars(10006))),button ->
        {
            switch (this.mineTogetherSocialinteractionsScreen.getPage())
            {
                case BLOCKED:
                    ChatModule.unmuteUser(profile.getLongHash());
                    refreshPage();
                    break;
                case FRIENDS:
                    ChatCallbacks.removeFriend(profile.getFriendCode(), MineTogetherClient.getUUID());
                    profile.setFriend(false);
                    ChatHandler.knownUsers.update(profile);
                    refreshPage();
                    break;
            }
        });
        this.muteButton = new ImageButton(0, 0, 20, 20, 0, 38, 20, resourceLocation, 256, 256, (button) ->
        {
            ChatModule.muteUser(profile.getLongHash());
            refreshPage();
        });

        this.openDMButton = new ImageButton(0, 0, 20, 20, 20, 38, 20, resourceLocation, 256, 256, (button) ->
        {
            //TODO
        });

        this.children = ImmutableList.of(removeButton, muteButton);
    }

    public void refreshPage()
    {
        this.mineTogetherSocialinteractionsScreen.showPage(this.mineTogetherSocialinteractionsScreen.getPage());
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f)
    {
        int p = k + 4;
        int q = j + (m - 24) / 2;
        int r = p + 24 + 4;
        Component component = new TranslatableComponent(this.profile.getUserDisplay());
        int t;
        if (component == TextComponent.EMPTY) {
            GuiComponent.fill(poseStack, k, j, k + l, j + m, FastColor.ARGB32.color(255, 74, 74, 74));
            this.minecraft.font.getClass();
            t = j + (m - 9) / 2;
        } else {
            GuiComponent.fill(poseStack, k, j, k + l, j + m, FastColor.ARGB32.color(255, 48, 48, 48));
            this.minecraft.font.getClass();
            this.minecraft.font.getClass();
            t = j + (m - (9 + 9)) / 2;
            this.minecraft.font.draw(poseStack, component, (float)r, (float)(t + 12), FastColor.ARGB32.color(140, 255, 255, 255));
        }

        this.minecraft.getTextureManager().bind(new ResourceLocation("textures/entity/steve.png"));
        GuiComponent.blit(poseStack, p, q, 24, 24, 8.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.enableBlend();
        GuiComponent.blit(poseStack, p, q, 24, 24, 40.0F, 8.0F, 8, 8, 64, 64);
        RenderSystem.disableBlend();
        this.minecraft.font.draw(poseStack, this.profile.getUserDisplay(), (float)r, (float)t, FastColor.ARGB32.color(255, 255, 255, 255));

        if (this.removeButton != null && this.mineTogetherSocialinteractionsScreen.getPage() != MineTogetherSocialinteractionsScreen.Page.ALL)
        {
            this.removeButton.x = k + (l - this.removeButton.getWidth() - 4);
            this.removeButton.y = j + ((m - this.removeButton.getHeight()) / 2) - 10;
            this.removeButton.render(poseStack, n, o, f);
        }
        else
        {
            if(this.removeButton != null)
            {
                this.removeButton.x = 0;
                this.removeButton.y = 0;
            }
            if(this.openDMButton != null && this.muteButton != null)
            {
                this.muteButton.x = k + (l - this.muteButton.getWidth() - 4) - 20;
                this.muteButton.y = j + (m - this.muteButton.getHeight()) / 2;
                if(profile.isMuted()) muteButton.active = false;
                this.muteButton.render(poseStack, n, o, f);
                this.openDMButton.x = k + (l - this.openDMButton.getWidth() - 4);
                this.openDMButton.y = j + (m - this.openDMButton.getHeight()) / 2;
                this.openDMButton.render(poseStack, n, o, f);
            }
        }
    }

    @Override
    public List<? extends GuiEventListener> children()
    {
        return this.children;
    }


    public Profile getProfile()
    {
        return profile;
    }
}
