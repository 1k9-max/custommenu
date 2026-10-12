package jok.moon.custommenu.mixin;

import jok.moon.custommenu.gui.CustomButtonsScreen;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chèn 1 nút "Custom Menu" ngay phía trên nút Thoát trò chơi
 * (menu.returnToMenu ở singleplayer / menu.disconnect ở multiplayer)
 * trong màn hình Pause (GameMenuScreen). Khi bấm, mở CustomButtonsScreen.
 */
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    // Constructor "giả" bắt buộc khi mixin class extends 1 class có constructor -
    // sẽ không bao giờ được gọi thật, chỉ để javac hài lòng.
    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "initWidgets", at = @At("TAIL"))
    private void custommenu$addOpenButton(CallbackInfo ci) {
        String quitSingleplayer = Text.translatable("menu.returnToMenu").getString();
        String quitMultiplayer = Text.translatable("menu.disconnect").getString();

        ClickableWidget quitButton = null;
        for (var element : this.children()) {
            if (element instanceof ClickableWidget widget) {
                String msg = widget.getMessage().getString();
                if (msg.equals(quitSingleplayer) || msg.equals(quitMultiplayer)) {
                    quitButton = widget;
                    break;
                }
            }
        }

        if (quitButton == null) {
            return; // Không tìm thấy (đổi bản MC?) -> bỏ qua, tránh crash.
        }

        int x = quitButton.getX();
        int y = quitButton.getY();
        int w = quitButton.getWidth();
        int h = quitButton.getHeight();
        int spacing = 4;

        // Đẩy nút Thoát xuống 1 khoảng để nhường chỗ cho nút mới phía trên nó.
        quitButton.setY(y + h + spacing);

        ButtonWidget openButton = ButtonWidget.builder(Text.translatable("custommenu.button.open"), b -> {
            this.client.setScreen(new CustomButtonsScreen((GameMenuScreen) (Object) this));
        }).dimensions(x, y, w, h).build();

        this.addDrawableChild(openButton);
    }
}
