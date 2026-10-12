package jok.moon.custommenu;

import jok.moon.custommenu.config.CommandButtonManager;
import jok.moon.custommenu.config.CommandExecutor;
import jok.moon.custommenu.gui.CustomButtonsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class CustomMenuClient implements ClientModInitializer {

    /**
     * Phím tắt trong Controls > Key Binds: bấm để mở CustomButtonsScreen trực tiếp,
     * không cần mở Pause Menu trước.
     */
    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        CommandButtonManager.init();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.custommenu.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // mặc định chưa gán phím, người chơi tự chọn trong Controls
                "key.categories.custommenu"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Nhả các phím đã giả lập bấm (macro) ở tick trước.
            CommandExecutor.releasePending();

            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new CustomButtonsScreen(null));
                }
            }
        });
    }
}
