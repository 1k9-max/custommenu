package jok.moon.custommenu.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CommandButtonManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<CommandButtonEntry>>() {}.getType();

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("custommenu");
    private static final Path FILE = CONFIG_DIR.resolve("buttons.json");

    private static List<CommandButtonEntry> cache = new ArrayList<>();

    private CommandButtonManager() {
    }

    public static void init() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            if (!Files.exists(FILE)) {
                writeDefault();
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể tạo thư mục/file config cho custommenu", e);
        }
        reload();
    }

    // Để danh sách rỗng, không tạo nút mặc định
    private static void writeDefault() throws IOException {
        List<CommandButtonEntry> defaults = new ArrayList<>();
        try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
            GSON.toJson(defaults, LIST_TYPE, w);
        }
    }

    public static synchronized void reload() {
        try {
            if (!Files.exists(FILE)) {
                cache = new ArrayList<>();
                return;
            }
            try (Reader r = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                List<CommandButtonEntry> loaded = GSON.fromJson(r, LIST_TYPE);
                cache = loaded != null ? loaded : new ArrayList<>();
            }
        } catch (IOException e) {
            cache = new ArrayList<>();
        }
    }

    public static synchronized List<CommandButtonEntry> getEntries() {
        return Collections.unmodifiableList(cache);
    }

    // Thêm nút mới không cần ID
    public static synchronized void addAndSave(String name, String command) {
        CommandButtonEntry entry = new CommandButtonEntry(name, command);
        cache.add(entry);
        save();
    }

    // Xóa nút theo vị trí (index) trong danh sách
    public static synchronized void deleteAndSave(int index) {
        if (index >= 0 && index < cache.size()) {
            cache.remove(index);
            save();
        }
    }

    // Hoặc xóa nút theo đối tượng Entry
    public static synchronized void deleteAndSave(CommandButtonEntry entry) {
        if (cache.remove(entry)) {
            save();
        }
    }

    // Thay toàn bộ danh sách (dùng cho màn hình Cài đặt: xóa / đổi vị trí) rồi lưu file
    public static synchronized void replaceAllAndSave(List<CommandButtonEntry> newList) {
        cache = new ArrayList<>(newList);
        save();
    }

    public static synchronized void save() {
        try {
            if (!Files.exists(CONFIG_DIR)) {
                Files.createDirectories(CONFIG_DIR);
            }
            try (Writer w = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(cache, LIST_TYPE, w);
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file buttons.json", e);
        }
    }
}
