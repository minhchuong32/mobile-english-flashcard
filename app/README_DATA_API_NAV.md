README - Data Layer & API / Navigation (Repository, API services, Navigation)

Mục đích

Tóm tắt luồng dữ liệu giữa API, Repository, ViewModel và UI; đồng thời mô tả luồng điều hướng (navigation) chính của app.

Vị trí tham khảo

- DI: di/AppModule.kt
- API: data/api/*ApiService.kt (AuthApiService, DeckApiService, CardApiService, UserApiService, DictionaryApiService, StudySessionApiService)
- Repositories: data/repository/*Repository.kt
- Navigation: navigation/AppNavGraph.kt, navigation/Screen.kt

Luồng dữ liệu tổng quan

1) API layer
- Retrofit interfaces định nghĩa các endpoint (suspend functions) trả về Response<T>.
- Thêm OkHttp interceptors để add Authorization header, logging, retry cơ bản.

2) Repository layer
- Mỗi repository đóng vai trò trung gian: gọi API, xử lý mapping giữa DTO và domain model, lưu cache local (Room) nếu cần.
- Repository xử lý policy: cache-first, network-first, hoặc hybrid.

3) ViewModel layer
- ViewModel tương tác với repository, expose StateFlow/LiveData cho UI.
- Xử lý trạng thái loading / success / error, và chuyển đổi lỗi thành thông điệp người dùng.

4) UI
- Compose screens đọc từ ViewModel và gửi sự kiện (intent) ngược lại.

Điều hướng (Navigation)

- `AppNavGraph` định nghĩa các routes (Screen.kt) cho các feature: auth (login/register), home, deck detail, study, profile, analytics.
- Luồng phổ biến: Launch -> if authenticated -> HomeShell -> tính năng chính; else -> Auth flow.
- Deep links: được cấu hình trong NavGraph cho notification / share link -> mở DeckDetail hoặc Exercise.

Error handling & Retry

- Repository centralizes error parsing: chuyển Response.errorBody() thành AppError domain.
- Retry policy cho các request idempotent (GET) hoặc dùng exponential backoff.

Gợi ý tổ chức code

- Mỗi feature có folder riêng (feature/*) với screen + viewmodel.
- Repositories inject các API services thông qua DI (AppModule).
- Tạo BaseRepository để tái sử dụng parsing error/response helpers.


