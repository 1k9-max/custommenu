# Custom Menu Buttons (Fabric 1.21.4)

Mod client-side thêm 1 nút **"Custom Menu"** ngay phía trên nút **Thoát trò chơi**
trong Pause Menu (nhấn ESC trong game). Bấm vào đó sẽ mở 1 GUI gồm:

- **Phần 1 (bên trái):** lưới nút lệnh đọc từ file `buttons.json`, xếp 3 nút mỗi
  hàng, hết hàng thì xuống hàng dưới, có thanh cuộn dọc khi danh sách dài.
- **Phần 2 (bên phải):** 4 nút điều khiển của mod:
  1. **Quay lại** – về lại Pause Menu.
  2. **Reload** – đọc lại danh sách nút từ file `buttons.json` (không cần khởi
     động lại game khi bạn sửa file này).
  3. **Thêm nút** – mở màn hình nhập **Tên nút** + **Command sẽ chạy**, bên dưới
     có nút **Lưu** và **Hủy**. Lưu xong sẽ tự ghi vào `buttons.json` và quay
     lại danh sách.
  4. **Đóng** – đóng hẳn GUI, về lại trong game.

Ngoài ra mod có đăng ký thêm 1 **Key Bind** trong `Options > Controls > Key
Binds > Custom Menu` (mặc định chưa gán phím) — gán phím tùy ý để mở thẳng GUI
này mà không cần mở Pause Menu trước.

## File cấu hình

Đường dẫn: `.minecraft/config/custommenu/buttons.json`

```json
[
  {
    "id": "a1b2c3d4",
    "name": "Ban ngay",
    "command": "chat:/time set day"
  }
]
```

Cú pháp trường `command` (đây là "client command" do mod tự định nghĩa, KHÔNG
bắt buộc phải là lệnh server):

- `chat:<nội dung>` — gõ `<nội dung>` vào ô chat y như bạn tự gõ rồi Enter.
  Dùng được cho cả lệnh game (`chat:/gamemode creative`) lẫn chat thường
  (`chat:hello`).
- `key:<translation_key>` — giả lập bấm nhanh 1 phím vật lý, ví dụ
  `key:key.keyboard.f5` (đổi góc nhìn), `key:key.keyboard.e` (mở túi đồ).
  `translation_key` lấy theo tên định danh phím kiểu Minecraft/LWJGL
  (`key.keyboard.<phím>`).
- Không ghi tiền tố → mặc định coi như `chat:<toàn bộ chuỗi>`.

Bạn có thể tự sửa tay file JSON này rồi bấm **Reload** trong game, hoặc dùng
nút **Thêm nút** trong GUI để mod tự ghi giúp.

## Build trên Lubuntu

Yêu cầu: **JDK 21** (Fabric 1.21.4 dùng Java 21). Nếu máy bạn đang có sẵn JDK
khác (ví dụ JDK 25 do Ubuntu 24.04.2 cài mặc định), **không cần gỡ** — cài
thêm JDK 21 song song rồi trỏ Gradle dùng đúng bản đó.

```bash
sudo apt update
sudo apt install openjdk-21-jdk -y

# Xem đường dẫn cài đặt của từng JDK trên máy
update-alternatives --list java
# Ví dụ sẽ thấy: /usr/lib/jvm/java-21-openjdk-amd64/bin/java
#                /usr/lib/jvm/java-25-openjdk-amd64/bin/java (hoặc tương tự)
```

Mở `gradle.properties`, bỏ dấu `#` ở dòng `org.gradle.java.home` và sửa lại
đúng đường dẫn JDK 21 trên máy bạn (KHÔNG kèm `/bin/java`, chỉ tới thư mục
gốc JDK), ví dụ:

```properties
org.gradle.java.home=/usr/lib/jvm/java-21-openjdk-amd64
```

Việc này ép **Gradle daemon** chạy bằng JDK 21 mà không đụng tới JDK mặc định
của cả hệ thống (dùng cho việc khác vẫn chạy JDK 25 bình thường). Nếu build
mà không set dòng này trong khi `java -version` ra JDK 25, Gradle/Loom sẽ báo
lỗi kiểu `Unsupported class file major version` hoặc không nhận diện được
Java, không build được.

```bash
cd custommenu
```

Dự án này **chưa kèm sẵn Gradle Wrapper** (do môi trường tạo file không có
mạng để tải wrapper). Cách đơn giản nhất trên Lubuntu:

```bash
# Cài Gradle qua apt (hoặc dùng SDKMAN nếu muốn bản mới hơn)
sudo apt install gradle -y

# Sinh wrapper đúng chuẩn cho project (chỉ cần làm 1 lần)
gradle wrapper --gradle-version 8.10

# Từ giờ dùng wrapper để build cho đồng nhất
./gradlew build
```

> **Nếu bản Gradle từ apt quá cũ** (Loom 1.9 cần Gradle ≥ 8.8, lệnh
> `gradle wrapper` báo lỗi hoặc không nhận flag `--gradle-version`), cài bản
> mới qua SDKMAN thay vì apt:
> ```bash
> curl -s "https://get.sdkman.io" | bash
> source "$HOME/.sdkman/bin/sdkman-init.sh"
> sdk install gradle 8.10
> gradle wrapper --gradle-version 8.10
> ./gradlew build
> ```

Trước khi build, mở `gradle.properties` và kiểm tra lại các dòng
`yarn_mappings`, `loader_version`, `fabric_version` tại
<https://fabricmc.net/develop/> (chọn Minecraft 1.21.4) — vì môi trường tạo
mod này không có mạng để tự kiểm tra bản mới nhất tại thời điểm tạo file.

Build xong, file mod nằm ở:

```
build/libs/custommenu-1.0.0.jar
```

## Build qua GitHub Actions (khuyên dùng nếu máy local bị vướng JDK/Gradle)

Project đã có sẵn `.github/workflows/build.yml` tự động build mỗi khi bạn
push code lên GitHub — không cần lo JDK/Gradle trên máy local nữa.

```bash
cd custommenu-mod-source

# QUAN TRỌNG: nếu bạn từng bỏ dấu # dòng org.gradle.java.home trong
# gradle.properties để build local, hãy comment lại (thêm dấu # đầu dòng)
# trước khi push lên GitHub — path đó chỉ đúng trên máy bạn, không tồn tại
# trên máy chủ GitHub Actions, để nguyên sẽ làm build trên Actions bị lỗi.

git init
git add .
git commit -m "Custom menu mod"
git branch -M main
git remote add origin https://github.com/<ten-user>/<ten-repo>.git
git push -u origin main
```

Vào tab **Actions** trên GitHub repo, chờ workflow **"Build Mod"** chạy xong
(khoảng 3-6 phút), bấm vào lần chạy đó → phần **Artifacts** ở cuối trang →
tải file `custommenu-mod.zip` về, giải nén ra sẽ thấy file jar
(`custommenu-1.0.0.jar`) — bỏ vào thư mục `mods` là chơi được, không cần
build local nữa.

Nếu build trên Actions lỗi, vào log của bước **"Build with Gradle"** để xem
chi tiết, gửi log đó cho tôi.

## Cài đặt để chơi

1. Cài **Fabric Loader** cho Minecraft 1.21.4 (qua Fabric Installer).
2. Tải **Fabric API** bản tương ứng 1.21.4, bỏ vào thư mục `mods`.
3. Bỏ file `custommenu-1.0.0.jar` vừa build vào thư mục `mods`.
4. Chạy game, vào 1 thế giới, nhấn ESC để thấy nút **Custom Menu** phía trên
   nút **Save and Quit to Title / Disconnect**.

## Cấu trúc project

```
custommenu/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── README.md
└── src/main/
    ├── java/jok/moon/custommenu/
    │   ├── CustomMenuClient.java        # entrypoint client, keybinding
    │   ├── config/
    │   │   ├── CommandButtonEntry.java  # model 1 nút
    │   │   ├── CommandButtonManager.java# đọc/ghi/reload buttons.json
    │   │   └── CommandExecutor.java     # thực thi command/macro
    │   ├── gui/
    │   │   ├── CustomButtonsScreen.java # GUI chính (lưới nút + panel)
    │   │   └── AddButtonScreen.java     # GUI thêm nút mới
    │   └── mixin/
    │       └── GameMenuScreenMixin.java # chèn nút vào Pause Menu
    └── resources/
        ├── fabric.mod.json
        ├── custommenu.mixins.json
        └── assets/custommenu/lang/{en_us,vi_vn}.json
```

## Giả định đã dùng khi tạo mod

- Minecraft **1.21.4**, Fabric Loader + Fabric API, Java 21.
- Nút mới chỉ thêm ở **Pause Menu** (không thêm ở Title Screen).
- `command` chạy như **client-command tự định nghĩa** của mod (tiền tố
  `chat:` / `key:`), không gọi thẳng lệnh server có quyền OP.
- Mod chỉ chạy phía **client** (environment: "client" trong
  `fabric.mod.json`) vì không có logic server nào.
