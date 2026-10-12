package jok.moon.custommenu.gui;

import jok.moon.custommenu.config.CommandButtonEntry;
import jok.moon.custommenu.config.CommandButtonManager;
import jok.moon.custommenu.config.CommandExecutor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI chính của mod:
 *  - Phần 1 (bên trái): lưới nút lệnh đọc từ buttons.json, 3 nút / hàng, hết hàng xuống dòng,
 *    có thanh cuộn dọc khi danh sách dài hơn khung nhìn.
 *  - Phần 2 (bên phải): các nút điều khiển của mod (Quay lại, Reload, Thêm nút, Đóng).
 */
public class CustomButtonsScreen extends Screen {

    private static final int COLUMNS = 3;
    private static final int BTN_WIDTH = 120;
    private static final int BTN_HEIGHT = 20;
    private static final int H_GAP = 8;
    private static final int V_GAP = 6;
    private static final int SIDE_PANEL_WIDTH = 110;
    private static final int SCROLLBAR_WIDTH = 6;

    private final Screen parent;

    private int viewX, viewY, viewW, viewH;
    private double scrollOffset = 0;
    private double maxScroll = 0;
    private boolean draggingScrollbar = false;

    private List<CommandButtonEntry> entries;
    private final List<ButtonWidget> gridWidgets = new ArrayList<>();
    private final List<ButtonWidget> sideWidgets = new ArrayList<>();

    public CustomButtonsScreen(Screen parent) {
        super(Text.translatable("custommenu.screen.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.entries = CommandButtonManager.getEntries();

        viewX = 10;
        viewY = 30;
        viewW = this.width - SIDE_PANEL_WIDTH - 30 - SCROLLBAR_WIDTH;
        viewH = this.height - viewY - 15;

        int totalRows = (int) Math.ceil(entries.size() / (double) COLUMNS);
        int contentHeight = totalRows * (BTN_HEIGHT + V_GAP);
        maxScroll = Math.max(0, contentHeight - viewH);
        scrollOffset = Math.min(scrollOffset, maxScroll);

        rebuildButtonGrid();
        buildSidePanel();
    }

    /** Xóa hết widget cũ và thêm lại đúng các nút đang nằm trong khung nhìn (theo scrollOffset). */
    private void rebuildButtonGrid() {
        // Xóa toàn bộ rồi xây lại (bao gồm cả side panel) để đơn giản hóa vòng đời widget.
        this.clearChildren();
        gridWidgets.clear();
        sideWidgets.clear();

        int colWidth = (viewW - (COLUMNS - 1) * H_GAP) / COLUMNS;

        for (int i = 0; i < entries.size(); i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;

            int x = viewX + col * (colWidth + H_GAP);
            int y = viewY + row * (BTN_HEIGHT + V_GAP) - (int) scrollOffset;

            // Chỉ thêm nút nếu nằm trong (hoặc gần) khung nhìn để đỡ tốn widget.
            if (y + BTN_HEIGHT < viewY - 4 || y > viewY + viewH + 4) {
                continue;
            }

            CommandButtonEntry entry = entries.get(i);
            ButtonWidget btn = ButtonWidget.builder(Text.literal(entry.getName()), b -> {
                CommandExecutor.run(entry.getCommand());
            }).dimensions(x, y, colWidth, BTN_HEIGHT).build();
            this.addDrawableChild(btn);
            gridWidgets.add(btn);
        }

        buildSidePanel();
    }

    private void buildSidePanel() {
        int px = this.width - SIDE_PANEL_WIDTH - 10;
        int py = 30;
        int gap = 6;

        ButtonWidget back = ButtonWidget.builder(Text.translatable("custommenu.button.back"), b -> {
            this.client.setScreen(parent);
        }).dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build();
        this.addDrawableChild(back);
        sideWidgets.add(back);

        py += BTN_HEIGHT + gap;
        ButtonWidget reload = ButtonWidget.builder(Text.translatable("custommenu.button.reload"), b -> {
            CommandButtonManager.reload();
            this.scrollOffset = 0;
            this.init();
        }).dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build();
        this.addDrawableChild(reload);
        sideWidgets.add(reload);

        py += BTN_HEIGHT + gap;
        ButtonWidget add = ButtonWidget.builder(Text.translatable("custommenu.button.add"), b -> {
            this.client.setScreen(new AddButtonScreen(this));
        }).dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build();
        this.addDrawableChild(add);
        sideWidgets.add(add);

        py += BTN_HEIGHT + gap;
        ButtonWidget close = ButtonWidget.builder(Text.translatable("custommenu.button.close"), b -> {
            this.client.setScreen(null);
        }).dimensions(px, py, SIDE_PANEL_WIDTH, BTN_HEIGHT).build();
        this.addDrawableChild(close);
        sideWidgets.add(close);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX >= viewX && mouseX <= viewX + viewW && mouseY >= viewY && mouseY <= viewY + viewH) {
            scrollOffset -= verticalAmount * (BTN_HEIGHT + V_GAP);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
            rebuildButtonGrid();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (maxScroll > 0 && button == 0 && isOnScrollbar(mouseX, mouseY)) {
            draggingScrollbar = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isOnScrollbar(double mouseX, double mouseY) {
        int barX = viewX + viewW + 2;
        return mouseX >= barX && mouseX <= barX + SCROLLBAR_WIDTH && mouseY >= viewY && mouseY <= viewY + viewH;
    }

    private void updateScrollFromMouse(double mouseY) {
        double ratio = (mouseY - viewY) / (double) viewH;
        scrollOffset = Math.max(0, Math.min(maxScroll, ratio * (maxScroll + viewH) - viewH / 2.0));
        rebuildButtonGrid();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        if (entries.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("custommenu.empty"),
                    viewX + viewW / 2, viewY + viewH / 2, 0xAAAAAA);
        }

        // khung viewport
        context.drawBorder(viewX - 2, viewY - 2, viewW + 4, viewH + 4, 0xFF555555);

        context.enableScissor(viewX, viewY, viewX + viewW, viewY + viewH);
        for (ButtonWidget w : gridWidgets) {
            w.render(context, mouseX, mouseY, delta);
        }
        context.disableScissor();

        for (ButtonWidget w : sideWidgets) {
            w.render(context, mouseX, mouseY, delta);
        }

        if (maxScroll > 0) {
            int barX = viewX + viewW + 2;
            int trackHeight = viewH;
            int handleHeight = Math.max(16, (int) (trackHeight * (viewH / (double) (viewH + maxScroll))));
            int handleY = viewY + (int) ((trackHeight - handleHeight) * (scrollOffset / maxScroll));

            context.fill(barX, viewY, barX + SCROLLBAR_WIDTH, viewY + trackHeight, 0x33FFFFFF);
            context.fill(barX, handleY, barX + SCROLLBAR_WIDTH, handleY + handleHeight, 0xFFCCCCCC);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
