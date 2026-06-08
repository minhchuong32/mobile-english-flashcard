# Mobile-English-FlashCard — Tổng quan dự án

Mô tả ngắn

Ứng dụng Mobile-English-FlashCard là một nền tảng học từ vựng tiếng Anh bằng flashcards, hỗ trợ:

- Quản lý bộ thẻ (deck): tạo, sửa, xóa, duyệt
- Tạo và học flashcards, kèm theo âm thanh/hình ảnh
- Hệ thống ôn tập lặp lại ngắt quãng (SRS)
- Chức năng đăng ký/đăng nhập/khôi phục mật khẩu (Auth)
- Tra cứu từ điển, lưu từ yêu thích
- Thông báo nhắc học (Firebase + local)
- Thống kê, báo cáo tiến độ học

Kiến trúc tổng quan

Dự án theo kiến trúc MVVM, thiết kế feature-first (mỗi tính năng là một module/folder):

- data/: xử lý dữ liệu (Retrofit API, repository, mapper)
- model/: DTOs và domain models
- feature/: mỗi tính năng (auth, deck, flashcard, exercise, profile, analytic, v.v.) chứa Screen + ViewModel
- di/: cấu hình Dependency Injection (Hilt)
- ui/: theme và reusable composables
- navigation/: định nghĩa NavGraph và Screen routes

Vị trí tập trung:

- APIs: app/src/main/java/com/example/englishflashcard/data/api
- Repositories: app/src/main/java/com/example/englishflashcard/data/repository
- Features (UI + ViewModel): app/src/main/java/com/example/englishflashcard/feature

Chức năng chính (chi tiết)

1) Auth
- Đăng ký, login, verify OTP, quên mật khẩu, reset password.
- Endpoint đã tổng hợp trong `AuthApiService.kt`.
- Lưu access token an toàn (EncryptedSharedPreferences/Keystore) và dùng Interceptor để thêm header Authorization.

2) Decks & Cards
- Tạo/browse/update/delete deck.
- Thêm sửa xóa flashcards (hỗ trợ media upload).

3) Học & SRS
- Chuẩn bị danh sách card ôn theo lịch (nextDue).
- Thu thập phản hồi người học và cập nhật CardProgress theo thuật toán SM-2 hoặc biến thể.

4) Dictionary
- Tra cứu từ, nhận nghĩa, ví dụ, phát âm và lưu favorite.

5) Notifications
- Nhận push từ FCM (`MyFirebaseMessagingService`), lên lịch nhắc học local.

6) Analytics
- Ghi nhận session, thống kê thời gian học, streak, accuracy.

Luồng dữ liệu (Data Flow)

1) View (Compose) gửi sự kiện tới ViewModel
2) ViewModel gọi Repository (StateFlow để expose UI state)
3) Repository gọi API (Retrofit) và/hoặc local DB (Room) để lấy/ghi dữ liệu
4) Repository trả về domain models cho ViewModel

Quy ước code & best practices

- Mỗi feature có folder riêng: Screen + ViewModel + (composables)
- Repositories là class (inject via Hilt). Tạo interface chỉ khi cần mock/replace cho test.
- ViewModel dùng StateFlow/SharedFlow để expose state/events.
- Tất cả mạng gọi bằng Coroutines (suspend) và xử lý lỗi rõ ràng.
- Lưu token an toàn, không lưu plain text.

Cấu trúc thư mục (điển hình)

app/src/main/java/com/example/englishflashcard
├── data/
│   ├── api/
│   └── repository/
├── di/
├── feature/
│   ├── auth/
│   ├── deck/
│   ├── flashcard/
│   └── exercise/
├── model/
├── navigation/
└── ui/

Chạy & phát triển (Quick start)

1) Thiết lập environment
- Cài Android Studio (Arctic Fox hoặc mới hơn), JDK 11+.

2) File cấu hình
- Thêm `google-services.json` vào `app/` (nếu sử dụng FCM/firebase features).

3) Build & run

```powershell
./gradlew clean assembleDebug; ./gradlew installDebug
```

Ghi chú: Trên Windows PowerShell, có thể cần gọi `gradlew.bat`:

```powershell
.\gradlew.bat clean assembleDebug; .\gradlew.bat installDebug
```

Kiểm thử & manual testing

- Test flows: register -> verify OTP -> login
- Forgot password -> verify OTP -> reset -> login
- Create deck -> add cards -> start study session -> verify progress update
- Simulate offline: update progress offline and sync later

Tài liệu tham khảo nội bộ

- `app/README_AUTH.md`: mô tả chi tiết luồng auth
- `app/README_DECKS.md`, `app/README_CARDS.md`, `app/README_SRS_EXERCISE.md`, `app/README_DICTIONARY.md`, `app/README_USER_PROFILE.md`, `app/README_NOTIFICATION.md`, `app/README_ANALYTICS.md`, `app/README_STUDY_SESSION.md`, `app/README_DATA_API_NAV.md`

