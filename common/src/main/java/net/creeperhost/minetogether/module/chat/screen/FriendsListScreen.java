package net.creeperhost.minetogether.module.chat.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.MineTogetherClient;
import net.creeperhost.minetogether.handler.ToastHandler;
import net.creeperhost.minetogether.module.chat.ChatFormatter;
import net.creeperhost.minetogether.module.chat.ScrollingChat;
import net.creeperhost.minetogether.module.chat.screen.listentries.ListEntryFriend;
import net.creeperhost.minetogether.screen.MineTogetherScreen;
import net.creeperhost.minetogethergui.lists.ScreenList;
import net.creeperhost.minetogethergui.widgets.ButtonMultiple;
import net.creeperhost.minetogethergui.widgets.ButtonString;
import net.creeperhost.minetogetherlib.chat.ChatCallbacks;
import net.creeperhost.minetogetherlib.chat.ChatHandler;
import net.creeperhost.minetogetherlib.chat.KnownUsers;
import net.creeperhost.minetogetherlib.chat.MineTogetherChat;
import net.creeperhost.minetogetherlib.chat.data.Profile;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FriendsListScreen extends MineTogetherScreen
{
    private final Screen parent;
    private ScreenList<ListEntryFriend> list;
    private ScrollingChat chat;
    private EditBox chatBox;
    private EditBox searchEntry;
    private int ticks;
    private Profile targetProfile = null;
    private Button removeFriend;
    private Button blockButton;
    private Button partyButton;

    public FriendsListScreen(Screen parent)
    {
        super(new TranslatableComponent("minetogether.friendscreen.title"));
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        if (list == null)
        {
            list = new ScreenList(this, minecraft, 100, height - 90, 32, this.height - 55, 28, 100);
        } else
        {
            list.updateSize(100, height - 90, 28, this.height - 55);
        }
        chat = new ScrollingChat(this, width - list.getRowWidth() - 22, this.height - 90, 32, this.height - 55, 110);
        chat.setLeftPos(list.getRowRight());

        chatBox = new EditBox(this.font, list.getRowRight() + 2, this.height -50, width - list.getRowWidth() - 7, 20, new TranslatableComponent(""));
        chatBox.setMaxLength(256);

        searchEntry = new EditBox(this.font, 1, this.height -50, list.width, 20, new TranslatableComponent(""));
        searchEntry.setSuggestion("Search");

        addButtons();
        children.add(list);
        children.add(searchEntry);
        children.add(chatBox);
        children.add(chat);
        refreshFriendsList();
    }

    public void addButtons()
    {
        addButton(new Button(5, height - 26, 100, 20, new TranslatableComponent("Cancel"), p -> minecraft.setScreen(parent)));

        addButton(new ButtonString( width - 105, 5, 120, 20, new TranslatableComponent(MineTogetherChat.profile.get().getFriendCode()), p ->
        {
            minecraft.keyboardHandler.setClipboard(MineTogetherChat.profile.get().getFriendCode());
            MineTogetherClient.toastHandler.displayToast(new TranslatableComponent("Copied to clipboard."), width - 160, 0, 5000, ToastHandler.EnumToastType.DEFAULT, null);
        }));

        addButton(removeFriend = new ButtonMultiple(width - 20, 32, 2, new TranslatableComponent("minetogether.friendscreen.tooltip.removebutton"), (button) ->
        {

        }));

        addButton(blockButton = new ButtonMultiple(width - 20, 52, 3, new TranslatableComponent("minetogether.friendscreen.tooltip.block"), (button) ->
        {
        }));

        addButton(partyButton = new ButtonMultiple(width - 20, 72, 4, new TranslatableComponent("minetogether.friendscreen.tooltip.partytime"), (button) ->
        {
        }));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f)
    {
        renderDirtBackground(1);
        list.render(poseStack, i, j, f);
        searchEntry.render(poseStack, i, j, f);
        chatBox.render(poseStack, i, j, f);
        chat.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
        drawCenteredString(poseStack, font, this.getTitle(), width / 2, 12, 0xFFFFFF);
        if(list.children().isEmpty()) drawCenteredString(poseStack, font, new TranslatableComponent("minetogether.friendslist.empty"), width / 2, (this.height / 2) - 20, -1);
    }

    @Override
    public void tick()
    {
        ticks++;
        if(ticks % 600 == 0)
        {
            refreshFriendsList();
        }
        if(list.getCurrSelected() != null && targetProfile != null && !targetProfile.equals(list.getCurrSelected().getProfile()))
        {
            targetProfile = list.getCurrSelected().getProfile();
            chat.updateLines(targetProfile.getMediumHash());
        }

        if(targetProfile != null)
        {
            chatBox.setSuggestion(targetProfile.isOnline() ? "" : "Friend is offline");
            chatBox.setEditable(targetProfile.isOnline());
            if (ChatHandler.hasNewMessages(targetProfile.getMediumHash()))
            {
                chat.updateLines(targetProfile.getMediumHash());
                ChatHandler.setMessagesRead(targetProfile.getMediumHash());
            }
        }
        toggleInteractionButtons(list.getCurrSelected() != null);
    }

    public void toggleInteractionButtons(boolean value)
    {
        removeFriend.active = value;
        blockButton.active = value;
        partyButton.active = value;
    }

    public static ArrayList<Profile> removedFriends = new ArrayList<>();

    protected boolean refreshFriendsList()
    {
        List<Profile> friends = new ArrayList<Profile>();
        List<Profile> onlineFriends = KnownUsers.getFriends().stream().filter(Profile::isOnline).collect(Collectors.toList());
        onlineFriends.sort(NameComparator.INSTANCE);
        List<Profile> offlineFriends = KnownUsers.getFriends().stream().filter(profile -> !profile.isOnline()).collect(Collectors.toList());
        offlineFriends.sort(NameComparator.INSTANCE);

        friends.addAll(onlineFriends);
        friends.addAll(offlineFriends);

        list.clearList();
        if (friends != null)
        {
            for (Profile friendProfile : friends)
            {
                ListEntryFriend friendEntry = new ListEntryFriend(this, list, friendProfile);
                if(searchEntry != null && !searchEntry.getValue().isEmpty())
                {
                    String s = searchEntry.getValue();
                    if(friendProfile.friendName.toLowerCase().contains(s.toLowerCase()))
                    {
                        if(!removedFriends.contains(friendProfile)) list.add(friendEntry);
                    }
                }
                else
                {
                    if(!removedFriends.contains(friendProfile)) list.add(friendEntry);
                }
                if(targetProfile != null && friendProfile.getFriendName().equals(targetProfile.getFriendName())) list.setSelected(friendEntry);
            }
            List<Profile> removedCopy = new ArrayList<Profile>(removedFriends);
            for(Profile removed : removedCopy)
            {
                boolean isInList = false;
                for(Profile friend : friends)
                {
                    if(friend.friendCode.equalsIgnoreCase(removed.friendCode))
                    {
                        isInList=true;
                        break;
                    }
                }
                if(!isInList)
                {
                    removedFriends.remove(removed);
                }
            }
        }
        return true;
    }

    @SuppressWarnings("all")
    public void removeFriend(Profile profile)
    {
        ConfirmScreen confirmScreen = new ConfirmScreen(t ->
        {
            if(t)
            {
                CompletableFuture.runAsync(() ->
                {
                   removedFriends.add(profile);
                   refreshFriendsList();
                   if(!ChatCallbacks.removeFriend(profile.getFriendCode(), MineTogetherClient.getUUID()))
                   {
                       profile.setFriend(false);
                       refreshFriendsList();
                   }
                });
            }
            minecraft.setScreen(new FriendsListScreen(parent));
        }, new TranslatableComponent("minetogether.removefriend.sure1"), new TranslatableComponent("minetogether.removefriend.sure2"));
        minecraft.setScreen(confirmScreen);
    }

    @Override
    public boolean charTyped(char c, int i)
    {
        if(searchEntry.isFocused())
        {
            boolean flag = searchEntry.charTyped(c, i);
            refreshFriendsList();
            return flag;
        }
        if(chatBox.isFocused())
        {
            return chatBox.charTyped(c, i);
        }
        return super.charTyped(c, i);
    }

    @Override
    public boolean keyPressed(int i, int j, int k)
    {
        if(searchEntry.isFocused())
        {
            searchEntry.setSuggestion("");
            boolean flag = searchEntry.keyPressed(i, j, k);
            refreshFriendsList();
            return flag;
        }
        if(targetProfile != null && chatBox.isFocused())
        {
            if ((i == GLFW.GLFW_KEY_ENTER || i == GLFW.GLFW_KEY_KP_ENTER) && !chatBox.getValue().trim().isEmpty())
            {
                ChatHandler.sendMessage(targetProfile.getMediumHash(), ChatFormatter.getStringForSending(chatBox.getValue()));
                chatBox.setValue("");
            }
            return chatBox.keyPressed(i, j, k);
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean mouseClicked(double d, double e, int i)
    {
        if(list.getCurrSelected() != null)
        {
            boolean flag = targetProfile == null || !targetProfile.equals(list.getCurrSelected().getProfile());
            if (flag) {
                Profile profile = list.getCurrSelected().getProfile();
                if (profile != null && profile.isFriend()) {
                    targetProfile = profile;
                    chat.updateLines(profile.getMediumHash());
                }
                return flag;
            }
        }
        return super.mouseClicked(d, e, i);
    }

    public static class NameComparator implements Comparator<Profile>
    {
        public static final NameComparator INSTANCE = new NameComparator();

        @Override
        public int compare(Profile profile1, Profile profile2)
        {
            String str1 = profile1.friendName;
            String str2 = profile2.friendName;

            int res = String.CASE_INSENSITIVE_ORDER.compare(str1, str2);
            if (res == 0)
            {
                res = str1.compareTo(str2);
            }
            return res;
        }
    }
}
