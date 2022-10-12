package net.creeperhost.minetogether.chat.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import net.creeperhost.minetogether.chat.ChatTarget;
import net.creeperhost.minetogether.chat.DisplayableMessage;
import net.creeperhost.minetogether.chat.MineTogetherChat;
import net.creeperhost.minetogether.lib.chat.irc.IrcChannel;
import net.creeperhost.minetogether.lib.chat.message.Message;
import net.creeperhost.minetogether.util.MessageFormatter;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by covers1624 on 20/7/22.
 */
public class MTChatComponent extends ChatComponent {

    // TODO tweak this.
    private static final int MAX_MESSAGE_HISTORY = 150;

    private final ChatTarget target;
    private final Minecraft minecraft;

    // Flag to override explicitly delegating to vanilla, set in specific cases.
    private boolean internalUpdate = false;

    private final LinkedList<Message> pendingMessages = new LinkedList<>();
    private final List<InGameDisplayableMessage> processedMessages = new ArrayList<>();
    private IrcChannel channel;

    @Nullable
    private Message clickedMessage;

    public MTChatComponent(ChatTarget target, Minecraft minecraft) {
        super(minecraft);
        this.target = target;
        this.minecraft = minecraft;
        assert target != ChatTarget.VANILLA : "MTChatComponent doesn't work this way";
    }

    public void attach(IrcChannel channel) {
        this.channel = channel;
        channel.addListener(message -> {
            synchronized (pendingMessages) {
                pendingMessages.add(message);
            }
        });
    }

    @Override
    public void render(PoseStack poseStack, int i) {
        if (!pendingMessages.isEmpty()) {
            internalUpdate = true;
            synchronized (pendingMessages) {
                for (Message pendingMessage : pendingMessages) {
                    addMessage(pendingMessage);
                }
                pendingMessages.clear();
            }
            internalUpdate = false;
        }
        super.render(poseStack, i);
    }

    @Override
    public void rescaleChat() {
        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.rescaleChat();
                case PUBLIC -> MineTogetherChat.publicChat.rescaleChat();
            }
        }

        trimmedMessages.clear();
        resetChatScroll();

        for (InGameDisplayableMessage message : processedMessages) {
            message.format();
            message.display();
        }
    }

    @Override
    public void clearMessages(boolean bl) {
        // Only delegate if we are the target to prevent loops.
        if (MineTogetherChat.target != target) {
            switch (MineTogetherChat.target) {
                case VANILLA -> MineTogetherChat.vanillaChat.clearMessages(bl);
                case PUBLIC -> MineTogetherChat.publicChat.clearMessages(bl);
            }
        }

        // Clear all messages. Do this manually as we don't use all the fields from ChatComponent.
        // This is more explicit on how we operate.
        synchronized (pendingMessages) {
            pendingMessages.clear();
        }
        for (InGameDisplayableMessage message : processedMessages) {
            message.onDead();
        }
        processedMessages.clear();
        trimmedMessages.clear();
    }

    @Override
    public void addRecentChat(String string) {
        channel.sendMessage(string);
    }

    private void addMessage(Message message) {
        InGameDisplayableMessage newMessage = new InGameDisplayableMessage(message);
        processedMessages.add(newMessage);
        newMessage.display();

        if (isChatFocused() && chatScrollbarPos > 0) {
            newMessageSinceScroll = true;
            scrollChat(1);
        }

        while (processedMessages.size() > MAX_MESSAGE_HISTORY) {
            InGameDisplayableMessage toRemove = processedMessages.remove(0);
            trimmedMessages.removeAll(toRemove.getTrimmedLines());
            toRemove.onDead();
        }
    }

    @Override
    public void removeById(int i) {
        assert !internalUpdate; // We dont use ID's

        MineTogetherChat.vanillaChat.removeById(i);
    }

    @Override
    public void addMessage(Component component, int i, int j, boolean bl) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component, i, j, bl);
    }

    @Override
    public void addMessage(Component component) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component);
    }

    @Override
    public void addMessage(Component component, int i) {
        assert !internalUpdate; // We don't use this to add messages.

        MineTogetherChat.vanillaChat.addMessage(component, i);
    }

    public boolean handleClick(double mouseX, double mouseY) {
        if (!isChatFocused()) return false;

        double x = mouseX - 2.0;
        double y = (double) minecraft.getWindow().getGuiScaledHeight() - mouseY - 40.0;
        x = Mth.floor(x / getScale());
        y = Mth.floor(y / (getScale() * (minecraft.options.chatLineSpacing + 1.0)));
        if (x < 0.0 || y < 0.0) return false;

        int i = Math.min(getLinesPerPage(), trimmedMessages.size());
        if (x <= (double) Mth.floor((double) getWidth() / getScale())) {
            Objects.requireNonNull(minecraft.font);
            if (y < (double) (9 * i + i)) {
                Objects.requireNonNull(minecraft.font);
                int j = (int) (y / 9.0 + (double) chatScrollbarPos);
                if (j >= 0 && j < trimmedMessages.size()) {
                    return handleClickedMessage(findMessageForTrimmedMessage(trimmedMessages.get(j)), x);
                }
            }
        }

        return false;
    }

    private boolean handleClickedMessage(@Nullable InGameDisplayableMessage clickedMessage, double x) {
        if (clickedMessage == null) return false;

        Message message = clickedMessage.getMessage();
        if (message.sender == null) return false;
        if (message.sender == MineTogetherChat.getOurProfile()) return false;

        Style style = minecraft.font.getSplitter().componentStyleAtWidth(clickedMessage.getBuiltMessage(), (int) x);
        if (style == null) return false;
        ClickEvent event = style.getClickEvent();
        if (event == null) return false;
        if (!event.getValue().equals(MessageFormatter.CLICK_NAME)) return false;

        this.clickedMessage = message;
        return true;
    }

    @Nullable
    private InGameDisplayableMessage findMessageForTrimmedMessage(GuiMessage<FormattedCharSequence> trimmedMessage) {
        // Little slow, realistically we should have a lookup map, but would be a pain to maintain.
        // This searches from the most recent chat message to the oldest.
        for (InGameDisplayableMessage processedMessage : processedMessages) {
            if (processedMessage.getTrimmedLines().contains(trimmedMessage)) {
                return processedMessage;
            }
        }
        return null;
    }

    @Nullable
    public Message getClickedMessage() {
        return clickedMessage;
    }

    public void clearClickedMessage() {
        clickedMessage = null;
    }

    private class InGameDisplayableMessage extends DisplayableMessage<GuiMessage<FormattedCharSequence>> {

        private InGameDisplayableMessage(Message message) {
            super(message);
        }

        @Override
        protected boolean isForward() {
            return false;
        }

        @Override
        protected GuiMessage<FormattedCharSequence> createMessage(int addTime, FormattedCharSequence message) {
            return new GuiMessage<>(addTime, message, 0);
        }

        @Override
        protected int getMessageIndex(GuiMessage<FormattedCharSequence> message) {
            return trimmedMessages.indexOf(message);
        }

        @Override
        protected void clearMessages() {
            trimmedMessages.removeAll(getTrimmedLines());
        }

        @Override
        protected void addMessage(int index, GuiMessage<FormattedCharSequence> message) {
            trimmedMessages.add(index, message);
        }

        @Override
        protected double getChatWidth() {
            return (double) getWidth() / getScale();
        }
    }
}
