# Caro AI (Gomoku)

Game **Cờ Caro 15×15** có giao diện đồ họa, người chơi đấu với máy. Máy (AI) tự phân tích thế cờ và chọn nước đi bằng thuật toán **Minimax + cắt tỉa Alpha-Beta**.

> Project môn **IT3160 – Nhập môn Trí tuệ nhân tạo** (minh họa Chương 3 – Tìm kiếm có đối thủ).

## Tính năng
- Bàn cờ 15×15, thắng khi có **5 quân liên tiếp** (ngang/dọc/chéo).
- AI chơi bằng Minimax + Alpha-Beta, có **chọn độ sâu tìm kiếm** trên giao diện.
- AI chạy ở luồng nền nên giao diện không bị treo khi máy "suy nghĩ".

## Công nghệ
- **Java 23** + **JavaFX 25**
- Kiến trúc **MVC**: `model` (bàn cờ + AI), `resources` (View), `controller` (xử lý sự kiện), `app` (main).

## Cài đặt & Chạy
Yêu cầu: **JDK 23** và **JavaFX SDK 25**.

```powershell
$jdk = "C:\path\to\jdk-23"
$jfx = "C:\path\to\javafx-sdk-25\lib"

# Biên dịch
$src = Get-ChildItem .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& "$jdk\bin\javac.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -d .\bin $src

# Chạy
& "$jdk\bin\java.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -cp .\bin app.GomokuMain
```

Hoặc trong **VS Code**: sửa đường dẫn JavaFX trong `.vscode/launch.json` rồi bấm Run "Launch Gomoku".

## Cách AI hoạt động (tóm tắt)
- **Minimax + Alpha-Beta**: AI (MAX) cực đại hóa điểm, giả định người chơi (MIN) đi tối ưu; cắt tỉa các nhánh vô ích.
- **Hàm lượng giá theo mẫu**: chấm điểm thế cờ qua các "cửa sổ" 5 ô (chuỗi 4 mở, chuỗi 3 mở...), ưu tiên phòng thủ.
- **Giới hạn không gian**: chỉ xét ô trống quanh quân đã đánh (bán kính 2) + giới hạn độ sâu → khả thi trên bàn 15×15.
