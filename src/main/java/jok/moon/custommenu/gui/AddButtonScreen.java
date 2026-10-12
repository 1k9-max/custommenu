package jok.moon.custommenu.gui;

import jok.moon.custommenu.config.CommandButtonManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Phần 3: nhập "tên" + "command sẽ chạy", bên dưới có nút Lưu và nút Hủy(thoát).
 */
public class AddButtonScreen extends Screen {

    private final CustomButtonsScreen parent;

    private TextFieldWidget nameField;
    private TextFieldWidget commandField;

    public AddButtonScreen(CustomButtonsScreen parent) {
        super(Text.translatable("custommenu.add.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int fieldWidth = Math.min(300, this.width - 40);
        int x = (this.width - fieldWidth) / 2;

        nameField = new TextFieldWidget(this.textRenderer, x, 60, fieldWidth, 20,
                Text.translatable("custommenu.add.name"));
        nameField.setMaxLength(64);
        this.addDrawableChild(nameField);

        commandField = new TextFieldWidget(this.textRenderer, x, 100, fieldWidth, 20,
                Text.translatable("custommenu.add.command"));
        commandField.setMaxLength(256);
        commandField.setText("chat:/");
        this.addDrawableChild(commandField);

        int btnWidth = 100;
        int gap = 10;
        int py = 140;
        int totalW = btnWidth * 2 + gap;
        int startX = (this.width - totalW) / 2;

        // Nút Lưu
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("custommenu.add.save"), b -> save())
                .dimensions(startX, py, btnWidth, 20).build());

        // Nút Hủy / Thoát
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("custommenu.add.cancel"), b -> cancel())
                .dimensions(startX + btnWidth + gap, py, btnWidth, 20).build());

        this.setInitialFocus(nameField);
    }

    private void save() {
        String name = nameField.getText().trim();
        String command = commandField.getText().trim();
        if (!name.isEmpty() && !command.isEmpty()) {
            CommandButtonManager.addAndSave(name, command);
        }
        backToParent();
    }

    private void cancel() {
        backToParent();
    }

    private void backToParent() {
        if (this.client != null) {
            this.client.setScreen(parent);
            // Xây lại lưới nút để nút mới thêm (nếu có) hiện ra ngay.
            parent.init();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("custommenu.add.name"),
                nameField.getX(), nameField.getY() - 12, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, Text.translatable("custommenu.add.command"),
                commandField.getX(), commandField.getY() - 12, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
