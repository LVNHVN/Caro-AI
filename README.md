# Caro AI (Gomoku) — Trí tuệ nhân tạo chơi Cờ Caro

> Project môn **IT3160 – Nhập môn Trí tuệ nhân tạo**
> Đề tài: Xây dựng AI chơi Cờ Caro 15×15 bằng thuật toán **Minimax** kết hợp **cắt tỉa Alpha-Beta**.

**Nền tảng:** Java 23 + JavaFX 25 · **Giao diện:** JavaFX GUI

---

## Mục lục
- [1. Giới thiệu chung về Project](#1-giới-thiệu-chung-về-project)
- [2. Cài đặt & Chạy thử](#2-cài-đặt--chạy-thử)
- [3. Phương pháp](#3-phương-pháp)
- [4. Kết quả thực hiện](#4-kết-quả-thực-hiện)
- [5. Phân tích kết quả](#5-phân-tích-kết-quả)
- [6. Đối chiếu với lý thuyết đã học (IT3160)](#6-đối-chiếu-với-lý-thuyết-đã-học-it3160)
- [7. Kết luận và hướng phát triển](#7-kết-luận-và-hướng-phát-triển)

---

## 1. Giới thiệu chung về Project

### 1.1. Mục tiêu
Project xây dựng một chương trình **Cờ Caro 15×15 có giao diện đồ họa**, cho phép người chơi (quân X) đấu với máy (quân O). Trọng tâm không nằm ở giao diện mà ở **"bộ não" AI**: máy phải tự phân tích thế cờ và chọn nước đi hợp lý trong thời gian chấp nhận được, minh họa trực tiếp cho nội dung **Chương 3 – Tìm kiếm có đối thủ** của môn học.

### 1.2. Mô tả bài toán
- **Bàn cờ:** lưới 15×15 ô.
- **Luật thắng:** bên nào tạo được **5 quân liên tiếp** (ngang, dọc, hoặc chéo) thì thắng.
- **Lượt đi:** hai bên luân phiên; người đi trước (X), máy đi sau (O).
- **Tính chất bài toán:** trò chơi đối kháng 2 người, **tổng bằng không** (một bên thắng ⇔ bên kia thua), **thông tin đầy đủ**, **xác định** (deterministic) — đúng lớp bài toán mà phương pháp tìm kiếm đối kháng nhắm tới.

### 1.3. Kiến trúc tổng quan
Chương trình tổ chức theo mô hình **MVC** với 4 lớp, ánh xạ tự nhiên sang khái niệm **tác tử** (Chương 2):

| Lớp | File | Vai trò |
|---|---|---|
| **Model** | `src/model/BoardModel.java` | Trạng thái bàn cờ + **toàn bộ AI** (Minimax, α-β, hàm lượng giá) |
| **View** | `src/resources/BoardView.java` | Vẽ bàn cờ, cập nhật quân, chọn độ sâu |
| **Controller** | `src/controller/BoardController.java` | Nhận sự kiện chuột → điều phối lượt người/máy |
| **Main** | `src/app/GomokuMain.java` | Khởi tạo ứng dụng JavaFX |

> Theo ngôn ngữ tác tử: Controller là **cảm biến + cơ cấu chấp hành**, Model là **chương trình tác tử** ra quyết định, View là **môi trường** mà tác tử quan sát và tác động.

---

## 2. Cài đặt & Chạy thử

### 2.1. Yêu cầu
- **JDK 23** (cần `javac` để biên dịch)
- **JavaFX SDK 25** (bản SDK cho Windows/macOS/Linux)

### 2.2. Biên dịch & chạy bằng dòng lệnh (Windows / PowerShell)
```powershell
# Đặt đường dẫn JDK và thư mục lib của JavaFX SDK
$jdk = "C:\path\to\jdk-23"
$jfx = "C:\path\to\javafx-sdk-25\lib"

# Biên dịch src -> bin
$src = Get-ChildItem -Path .\src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
& "$jdk\bin\javac.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -d .\bin $src

# Chạy
& "$jdk\bin\java.exe" --module-path $jfx --add-modules javafx.controls,javafx.fxml -cp .\bin app.GomokuMain
```

### 2.3. Chạy bằng VS Code
File `.vscode/launch.json` đã cấu hình sẵn. Cập nhật đường dẫn `--module-path` trong `vmArgs` về thư mục `lib` của JavaFX SDK trên máy bạn, rồi bấm **Run "Launch Gomoku"**.

---

## 3. Phương pháp

### 3.1. Mô hình hóa bài toán trò chơi
AI mô hình hóa bài toán theo 5 thành phần chuẩn của một bài toán trò chơi đối kháng:

| Thành phần | Hiện thực trong code |
|---|---|
| Trạng thái đầu (Initial state) | `reset()` — bàn trống, lượt thuộc về người chơi |
| Hàm sinh trạng thái kế tiếp (Successor) | `getOrderedCandidates()` sinh các nước hợp lệ |
| Kiểm tra kết thúc (Terminal test) | `checkWin()` (đủ 5 quân) và cờ `gameOver` |
| Hàm lượng giá (Utility/Evaluation) | `currentBoardScore` tính qua từ điển mẫu |
| Cây trò chơi (Game tree) | duyệt đệ quy bằng `minimax()` |

### 3.2. Thuật toán quyết định: Minimax
`getBestMove()` đóng vai trò hàm quyết định: với mỗi nước đi khả dĩ của AI, nó gọi đệ quy `minimax()` để ước lượng giá trị, rồi chọn nước có giá trị lớn nhất. Trong `minimax()`:
- **Lượt AI (MAX):** chọn giá trị lớn nhất trong các nhánh con.
- **Lượt người (MIN):** chọn giá trị nhỏ nhất (giả định đối thủ chơi tối ưu).

### 3.3. Cắt tỉa Alpha-Beta
Vì cây trò chơi caro bùng nổ theo hàm mũ, thuật toán dùng hai biên `alpha` (giá trị tốt nhất MAX đã có) và `beta` (giá trị tốt nhất MIN đã có). Khi `beta <= alpha`, nhánh còn lại chắc chắn vô ích và bị **cắt** (`break`) — không ảnh hưởng kết quả nhưng giảm mạnh số nút phải xét.

### 3.4. Tri thức bổ sung để khả thi trên bàn 15×15
Minimax + α-β thuần vẫn quá lớn với caro, nên project bổ sung 3 cơ chế then chốt:

1. **Giới hạn độ sâu (depth cutoff):** dừng đệ quy tại `depth == 0` và trả về giá trị lượng giá — biến tìm kiếm tới-tận-cùng thành tìm kiếm **xấp xỉ**.
2. **Hàm lượng giá theo mẫu (heuristic):** mỗi "cửa sổ" 5 ô được chấm điểm bằng từ điển mẫu trong `getPrecisePatternScore()` (ví dụ `MMMMM` = 100000 điểm thắng; chuỗi 4 mở giá trị rất cao…). Hàm này **bất đối xứng** — thế nguy hiểm của đối thủ bị trừ điểm gần ngang với thế lợi của AI, giúp AI **ưu tiên phòng thủ sống sót**.
3. **Tập ứng viên + sắp xếp nước đi (Candidate Set + Move Ordering):** chỉ xét các ô trống trong **bán kính 2** quanh quân đã đánh (`candidateSet`), và sắp xếp chúng theo "trọng số chiến thuật" để xét nước nóng trước → α-β cắt tỉa hiệu quả hơn.

### 3.5. Tối ưu hiệu năng
- **Lượng giá gia tăng** (`evaluateIncrementalMove`): chỉ tính lại điểm của các đường đi qua ô vừa thay đổi thay vì quét cả bàn.
- **Delta-tracking** tập ứng viên: làm/hoàn tác nước đi giả định trong đệ quy mà không sao chép lại toàn bộ trạng thái.
- **Ưu tiên thắng nhanh/thua chậm:** cộng/trừ `depth` vào điểm ở trạng thái sát thủ để AI chọn đòn kết liễu sớm nhất.

---

## 4. Kết quả thực hiện

### 4.1. Môi trường & biên dịch
- Cấu hình: **JDK 23.0.2 (Temurin)** + **JavaFX SDK 25.0.3**, Windows 11.
- Biên dịch toàn bộ `src` → `bin`: **thành công, 5 lớp, không lỗi/cảnh báo**.

### 4.2. Chạy thử
- Cửa sổ game "Đánh Cờ 5 Nước" (800×640) khởi động ổn định, không phát sinh exception.
- AI chạy trên **luồng nền** (`new Thread` + `Platform.runLater`) nên giao diện không bị treo trong lúc máy "suy nghĩ".
- Giao diện cho phép **chọn độ sâu tìm kiếm**, qua đó quan sát trực tiếp được sự đánh đổi giữa độ mạnh của AI và thời gian phản hồi.

### 4.3. Hành vi quan sát được
- Ở độ sâu thấp, AI phản hồi tức thì nhưng chơi yếu (chủ yếu phản ứng cục bộ).
- Tăng độ sâu, AI biết **chặn chuỗi 4 / chuỗi 3 mở** của người chơi và tự tạo nước đôi — thể hiện hàm lượng giá hoạt động đúng — nhưng thời gian mỗi nước tăng nhanh.

---

## 5. Phân tích kết quả

### 5.1. Vì sao AI khả thi trên không gian lớn
Bản chất caro có hệ số phân nhánh rất lớn. Project khống chế cả hai chiều của độ phức tạp:
- **Giảm bề rộng b:** candidate set chỉ giữ vài chục ô liên quan thay vì 225 ô.
- **Giảm chiều sâu m:** depth cutoff.
- **Cắt tỉa:** α-β kết hợp move ordering tốt loại bỏ phần lớn nhánh vô ích.

Ba yếu tố cộng hưởng là lý do AI vừa đủ mạnh vừa phản hồi nhanh.

### 5.2. Điểm mạnh
- Sắp xếp nước "nóng" lên trước giúp α, β hội tụ sớm ⇒ tỉ lệ cắt tỉa cao.
- Hàm lượng giá bất đối xứng tạo hành vi phòng thủ chắc chắn, hiếm khi "để thủng" chuỗi 4 của đối thủ.
- Lượng giá gia tăng giúp tăng độ sâu mà chi phí mỗi nút vẫn thấp.

### 5.3. Hạn chế
- **Chỉ xấp xỉ, không tối ưu tuyệt đối:** do cutoff giữa chừng, chất lượng phụ thuộc hoàn toàn vào hàm heuristic.
- **Hiệu ứng đường chân trời (horizon effect):** depth cố định, không có *quiescence search* nên mối đe dọa ngay sau tầng cuối có thể bị bỏ qua.
- **Lặp tính toán:** chưa có *transposition table* nên cùng một thế cờ đến từ thứ tự nước khác nhau bị đánh giá lại.
- **Lượng giá tách đường độc lập:** các thế phức hợp (ví dụ "song tam") chưa được nhận diện hoàn hảo vì điểm các trục được cộng riêng rẽ.

---

## 6. Đối chiếu với lý thuyết đã học (IT3160)

Bài giảng trực tiếp liên quan là **L5 – Tìm kiếm đối kháng**. Project ánh xạ gần như 1–1:

| Khái niệm trong bài giảng | Thể hiện trong Project | Nhận xét |
|---|---|---|
| Biểu diễn bài toán trò chơi (L5-6): initial state, successor, terminal test, utility | `reset` / `getOrderedCandidates` / `checkWin` / `currentBoardScore` | ✅ Đầy đủ 5 thành phần |
| Giá trị & giải thuật **Minimax** (L5-8→10) | `getBestMove` + `minimax` (MAX/MIN) | ✅ Đúng công thức `MAX(v, MIN-VALUE(s))` |
| Đặc điểm độ phức tạp **O(bᵐ)** (L5-11) | Lý do phải cắt tỉa & giới hạn | ✅ Nhận diện đúng vấn đề bùng nổ |
| **Cắt tỉa α-β** (L5-12, 18→20): α=tốt nhất cho MAX, β=tốt nhất cho MIN | `alpha`, `beta`, `if (beta<=alpha) break` | ✅ Khớp pseudo-code `if v≥β return v` |
| **Giới hạn không gian bằng heuristic; heuristic đóng vai trò như h(n) của A\*** (L5-21, liên hệ L4) | Hàm lượng giá theo mẫu + depth cutoff + candidate set | ✅ Đúng khuyến nghị thực tế của slide |
| "Thường không thể tối ưu → **xấp xỉ**" (L5-4) | Cutoff giữa chừng ⇒ kết quả xấp xỉ | ✅ Đúng dự báo của lý thuyết |
| Tác tử cảm biến–hành động (Chương 2) | Kiến trúc MVC | ◐ Liên hệ gián tiếp |
| CSP (L6), Logic (L8–L11), Học máy/KNN/Naïve Bayes (L12–L14) | — | ✗ Không dùng (project thuần tìm kiếm, không học từ dữ liệu) |

**Nhận định:** phần "AI chỉ chơi xấp xỉ chứ không tối ưu tuyệt đối" **không phải khuyết điểm thiết kế** mà chính là hệ quả tất yếu đã được L5 (slide 4 & 21) dự báo cho bài toán có không gian trạng thái lớn — và project đã xử lý đúng bằng bộ ba *cutoff + heuristic + cắt tỉa*.

---

## 7. Kết luận và hướng phát triển

**Kết luận.** Project là một hiện thực **chỉn chu và bám sát giáo trình** của Chương 3 – Tìm kiếm có đối thủ: mô hình hóa đúng bài toán trò chơi, cài đặt chính xác Minimax và cắt tỉa Alpha-Beta, đồng thời áp dụng đúng khuyến nghị của bài giảng về việc dùng tri thức heuristic để giới hạn không gian tìm kiếm (vai trò tương tự h(n) trong A\*). Chương trình biên dịch và chạy ổn định trên Java 23 + JavaFX 25, AI thể hiện hành vi phòng thủ/tấn công hợp lý.

**Hướng phát triển** (đều là kỹ thuật nâng cao trên nền lý thuyết đã có):
1. **Quiescence search** để khử hiệu ứng đường chân trời.
2. **Transposition table** (bảng băm Zobrist) để tránh tính lại thế cờ trùng.
3. **Iterative deepening** kết hợp giới hạn thời gian thực sự.
4. **Tinh chỉnh / học trọng số hàm lượng giá** — cầu nối sang Chương 5 (Học máy) để AI tự tối ưu bộ điểm mẫu.
