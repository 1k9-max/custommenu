package jok.moon.custommenu.gui;

import jok.moon.custommenu.config.CommandButtonEntry;
import jok.moon.custommenu.config.CommandButtonManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Cài đặt: sắp xếp / xóa nút.
 *  - Giữ chuột trái lên 1 nút rồi kéo  -> đổi vị trí (chèn vào vị trí thả).
 *  - Bấm chuột trái nhanh (không giữ)   -> xóa nút (chỉ xóa trong bản nháp).
 *  - Lưu     -> ghi bản nháp vào buttons.json rồi về menu chính.
 *  - Quay lại -> bỏ mọi thay đổi chưa lưu, về menu chính.
 */
public class SettingsScreen extends Screen {

    private static final int COLUMNS = 3;
    private static final int BTN_HEIGHT = 20;
    private static final int H_GAP = 8;
    private static final int V_GAP = 6;
    private static final int SIDE_PANEL_WIDTH = 110;
    private static final int SCROLLBAR_WIDTH = 6;
    private static final long HOLD_MS = 250;      // giữ lâu hơn mức này = chế độ kéo, không xóa
    private static final double DRAG_START_PX = 4; // di chuyển quá mức này = bắt đầu kéo

    private final CustomButtonsScreen parent;
    private final List<CommandButtonEntry> workingCopy;

    private int viewX, viewY, viewW, viewH, colWidth;
    private double scrollOffset = 0;
    private double maxScroll = 0;

    private ButtonWidget scratch; // nút "vẽ mẫu", không thêm vào children

    private int pressedIndex = -1;
    private long pressTime;
    private double pressX, pressY;
    private boolean dragging = false;
    private int dropIndex = -1;
    private double dragMouseX, dragMouseY;

    public SettingsScreen(CustomButtonsScreen parent) {
        super(Text.translatable("custommenu.settings.title"));
        this.parent = parent;
        this.workingCopy = new ArrayList<>(CommandButtonManager.getEntries());
    }

    @Override
    protected void init() {
        viewX = 10;
        viewY = 30;
        viewW = this.width - SIDE_PANEL_WIDTH - 30 - SCROLLBAR_WIDTH;
        viewH = this.height - viewY - 15;
        colWidth = (viewW - (COLUMNS - 1) * H_GAP) / COLUMNS;

        scratch = ButtonWidget.builder(Text.empty(), b -> {
        }).dimensions(0, 0, colWidth, BTN_HEIGHT).build();

        recalcScroll();

        int px = this.width - SIDE_PANEL_WIDTH - 10;
        int py = 30;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("custommenu.add.save"), b -> {
            CommandButtonManager.replaceAllAndSave(workingCopy);
            this.client.setScreen(parent);
        }).dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build());

        py += BTN_HEIGHT + 6;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("custommenu.button.back"), b -> close())
                .dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build());
    }

    /** ESC / nút Quay lại: bỏ thay đổi, về menu chính. */
    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    private void recalcScroll() {
        int rows = (int) Math.ceil(workingCopy.size() / (double) COLUMNS);
        maxScroll = Math.max(0, rows * (BTN_HEIGHT + V_GAP) - viewH);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
    }

    private int cellX(int index) {
        return viewX + (index % COLUMNS) * (colWidth + H_GAP);
    }

    private int cellY(int index) {
        return viewY + (index / COLUMNS) * (BTN_HEIGHT + V_GAP) - (int) scrollOffset;
    }

    /** Trả về index của nút dưới con trỏ (trong workingCopy), hoặc -1. */
    private int indexAt(double mx, double my) {
        if (mx < viewX || mx > viewX + viewW || my < viewY || my > viewY + viewH) return -1;
        double rx = mx - viewX;
        int col = (int) (rx / (colWidth + H_GAP));
        if (col >= COLUMNS || rx - col * (colWidth + H_GAP) > colWidth) return -1;
        double ry = my - viewY + scrollOffset;
        int row = (int) (ry / (BTN_HEIGHT + V_GAP));
        if (ry - row * (BTN_HEIGHT + V_GAP) > BTN_HEIGHT) return -1;
        int idx = row * COLUMNS + col;
        return idx >= 0 && idx < workingCopy.size() ? idx : -1;
    }

    /** Ô gần con trỏ nhất để thả (luôn hợp lệ, kể cả khi thả vào khe hở). */
    private int dropIndexAt(double mx, double my) {
        int col = (int) Math.max(0, Math.min(COLUMNS - 1, (mx - viewX) / (colWidth + H_GAP)));
        int row = (int) Math.max(0, (my - viewY + scrollOffset) / (BTN_HEIGHT + V_GAP));
        return Math.max(0, Math.min(workingCopy.size() - 1, row * COLUMNS + col));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int idx = indexAt(mouseX, mouseY);
            if (idx >= 0) {
                pressedIndex = idx;
                pressTime = Util.getMeasuringTimeMs();
                pressX = mouseX;
                pressY = mouseY;
                dragging = false;
                dropIndex = idx;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (pressedIndex >= 0 && button == 0) {
            if (!dragging && Math.hypot(mouseX - pressX, mouseY - pressY) > DRAG_START_PX) {
                dragging = true;
            }
            if (dragging) {
                dragMouseX = mouseX;
                dragMouseY = mouseY;
                dropIndex = dropIndexAt(mouseX, mouseY);
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (pressedIndex >= 0 && button == 0) {
            long held = Util.getMeasuringTimeMs() - pressTime;
            if (dragging) {
                CommandButtonEntry moved = workingCopy.remove(pressedIndex);
                workingCopy.add(Math.max(0, Math.min(workingCopy.size(), dropIndex)), moved);
            } else if (held < HOLD_MS && indexAt(mouseX, mouseY) == pressedIndex) {
                workingCopy.remove(pressedIndex); // bấm nhanh = xóa
                recalcScroll();
            }
            // giữ lâu mà không kéo -> không làm gì (tránh xóa nhầm)
            pressedIndex = -1;
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= viewX && mouseX <= viewX + viewW && mouseY >= viewY && mouseY <= viewY + viewH) {
            scrollOffset -= verticalAmount * (BTN_HEIGHT + V_GAP);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void drawCell(DrawContext context, CommandButtonEntry e, int x, int y, int w, int mouseX, int mouseY, float delta) {
        scratch.setMessage(Text.literal(e.getName()));
        scratch.setPosition(x, y);
        scratch.setWidth(w);
        scratch.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        // Tự cuộn khi đang kéo sát mép trên/dưới khung.
        if (dragging) {
            if (mouseY < viewY + 12) scrollOffset = Math.max(0, scrollOffset - 3);
            else if (mouseY > viewY + viewH - 12) scrollOffset = Math.min(maxScroll, scrollOffset + 3);
        }

        context.drawBorder(viewX - 2, viewY - 2, viewW + 4, viewH + 4, 0xFF555555);

        // Danh sách hiển thị: khi đang kéo thì bỏ nút bị kéo ra và chừa 1 ô trống ở vị trí sẽ thả.
        List<CommandButtonEntry> shown = new ArrayList<>(workingCopy);
        CommandButtonEntry dragged = null;
        if (dragging && pressedIndex >= 0 && pressedIndex < shown.size()) {
            dragged = shown.remove(pressedIndex);
        }
        int hoverIdx = dragging ? -1 : indexAt(mouseX, mouseY);

        context.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        int slot = 0;
        int total = shown.size() + (dragged != null ? 1 : 0);
        for (int i = 0; i < total; i++) {
            boolean placeholder = dragged != null && i == dropIndex;
            CommandButtonEntry e = placeholder ? null : shown.get(slot++);
            int x = cellX(i);
            int y = cellY(i);
            if (y + BTN_HEIGHT < viewY || y > viewY + viewH) {
                continue; // nằm ngoài khung nhìn
            }
            if (placeholder) {
                context.fill(x, y, x + colWidth, y + BTN_HEIGHT, 0x33F2C94C);
                context.drawBorder(x, y, colWidth, BTN_HEIGHT, 0xFFF2C94C);
                continue;
            }
            drawCell(context, e, x, y, colWidth, mouseX, mouseY, delta);
            if (!dragging && i == hoverIdx) {
                // Đỏ = bấm nhanh sẽ xóa nút này.
                context.fill(x, y, x + colWidth, y + BTN_HEIGHT, 0x66FF3030);
            }
        }
        context.disableScissor();

        if (dragged != null) {
            int w = colWidth;
            int x = (int) dragMouseX - w / 2;
            int y = (int) dragMouseY - BTN_HEIGHT / 2;
            drawCell(context, dragged, x, y, w, mouseX, mouseY, delta);
            context.drawBorder(x, y, w, BTN_HEIGHT, 0xFFF2C94C);
        }

        if (workingCopy.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("custommenu.empty"),
                    viewX + viewW / 2, viewY + viewH / 2, 0xAAAAAA);
        }

        if (maxScroll > 0) {
            int barX = viewX + viewW + 2;
            int handleH = Math.max(16, (int) (viewH * (viewH / (double) (viewH + maxScroll))));
            int handleY = viewY + (int) ((viewH - handleH) * (scrollOffset / maxScroll));
            context.fill(barX, viewY, barX + SCROLLBAR_WIDTH, viewY + viewH, 0x33FFFFFF);
            context.fill(barX, handleY, barX + SCROLLBAR_WIDTH, handleY + handleH, 0xFFCCCCCC);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
