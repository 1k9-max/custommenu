package jok.moon.custommenu.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * "Client command" tự định nghĩa của mod, KHÔNG bắt buộc phải là lệnh /command của server.
 *
 * Cú pháp field "command" trong buttons.json:
 *   chat:<nội dung>   -> gõ <nội dung> vào chat y như người chơi tự gõ (Enter luôn).
 *                        Dùng cho cả lệnh game (/gamemode...) lẫn chat thường.
 *   key:<keycode>     -> giả lập bấm 1 lần phím vật lý, keycode dạng "key.keyboard.e",
 *                        "key.keyboard.f5", v.v. (theo tên định danh phím của Minecraft/LWJGL).
 *   (không tiền tố)   -> coi như chat:<toàn bộ chuỗi>
 */
public final class CommandExecutor {

    private CommandExecutor() {
    }

    /** Các phím đang được "giả lập nhấn", sẽ được nhả ra ở tick client kế tiếp. */
    private static final List<InputUtil.Key> PENDING_RELEASE = new ArrayList<>();

    /** Gọi từ ClientTickEvents.END_CLIENT_TICK mỗi tick để nhả các phím đã bấm giả lập. */
    public static void releasePending() {
        if (PENDING_RELEASE.isEmpty()) return;
        for (InputUtil.Key key : PENDING_RELEASE) {
            KeyBinding.setKeyPressed(key, false);
        }
        PENDING_RELEASE.clear();
    }

    public static void run(String rawCommand) {
        if (rawCommand == null || rawCommand.isBlank()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        if (rawCommand.startsWith("chat:")) {
            sendChat(client, rawCommand.substring("chat:".length()));
        } else if (rawCommand.startsWith("key:")) {
            tapKey(client, rawCommand.substring("key:".length()));
        } else {
            sendChat(client, rawCommand);
        }
    }

    private static void sendChat(MinecraftClient client, String message) {
        if (message.isBlank()) return;
        if (message.startsWith("/")) {
            client.player.networkHandler.sendChatCommand(message.substring(1));
        } else {
            client.player.networkHandler.sendChatMessage(message);
        }
    }

    /**
     * Giả lập bấm nhanh 1 phím vật lý (nhấn rồi nhả ngay ở tick kế tiếp).
     * keyName ví dụ: "key.keyboard.e", "key.keyboard.f5"
     */
    private static void tapKey(MinecraftClient client, String keyName) {
        InputUtil.Key key = InputUtil.fromTranslationKey(keyName.trim());
        KeyBinding.setKeyPressed(key, true);
        // Nhả phím ra ở tick client kế tiếp (xem CustomMenuClient#onEndTick).
        PENDING_RELEASE.add(key);
    }
}
