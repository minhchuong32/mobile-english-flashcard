# Android Coding Practices: English Learning Platform

Chào mừng team! Tài liệu này định nghĩa các quy tắc lập trình để dự án của chúng ta luôn sạch sẽ, dễ bảo trì và có hiệu suất cao.

## 1. Kiến trúc tổng thể
Chúng ta tuân thủ kiến trúc **MVVM (Model-View-ViewModel)**.
- **Data Layer:** Chỉ làm việc với network/database.
- **ViewModel:** Xử lý logic và trạng thái màn hình (State).
- **UI (Compose):** Chỉ hiển thị dữ liệu và gửi sự kiện người dùng (event).

## 2. Quy tắc Code (Golden Rules)

### A. ViewModel & State Management
- **Luôn dùng `StateFlow`:** Để quản lý trạng thái UI. Tránh dùng `LiveData` nếu dự án là Kotlin thuần.
- **Stateless UI:** Các hàm Composable nên nhận dữ liệu qua tham số (state hoisting).
- **Sealed Classes:** Sử dụng cho các trạng thái màn hình (Loading, Success, Error).

### B. Dependency Injection (Hilt)
- **Không khởi tạo thủ công:** Tuyệt đối không dùng `Retrofit.Builder()...` trong class. Mọi thứ phải được cung cấp qua `@Module` trong file `AppModule.kt`.
- **Constructor Injection:** Luôn dùng Hilt để inject các thành phần vào Repository và ViewModel.

### C. Networking & Repository
- **Repository Pattern:** Repository là cầu nối duy nhất giữa API và phần còn lại của ứng dụng.
- **Suspend Functions:** Mọi hàm network/database bắt buộc phải là `suspend` để chạy bất đồng bộ bằng Coroutines.
- **Không interface thừa:** Chỉ tạo interface cho Repository khi thực sự cần Unit Test (Mô phỏng dữ liệu). Nếu không, hãy dùng `class` trực tiếp.

## 3. Best Practices cho UI (Jetpack Compose)
- **Tách Component:** Nếu một hàm Composable dài quá 50 dòng, hãy tách nhỏ thành các sub-components.
- **Preview:** Luôn tạo `@Preview` cho các màn hình để dễ dàng kiểm tra UI mà không cần chạy app.
- **Reusability:** Các nút bấm, text field dùng chung phải đặt trong `ui/components/`.

## 4. Coding Standard (Quy ước đặt tên)
| Component | Naming Pattern | Ví dụ |
| :--- | :--- | :--- |
| **Screen** | `[Feature]Screen.kt` | `LessonScreen.kt` |
| **ViewModel** | `[Feature]ViewModel.kt` | `LessonViewModel.kt` |
| **Repository** | `[Feature]Repository.kt` | `LessonRepository.kt` |
| **DTO (Model)** | `[Name]Dto.kt` | `LessonDto.kt` |
| **UI State** | `[Feature]UiState.kt` | `LessonUiState.kt` |

## 5. Quy trình làm việc (Workflow)
1. **Model:** Tạo DTO khớp với JSON từ Backend.
2. **API:** Thêm method vào `ApiService.kt`.
3. **Repository:** Gọi API và xử lý lỗi (nếu có).
4. **ViewModel:** Gọi Repository, cập nhật `UiState`.
5. **UI:** Quan sát `UiState` và cập nhật giao diện.

## 6. Checklist trước khi Merge Code
- [ ] Code có tuân thủ cấu trúc thư mục không?
- [ ] Có sử dụng `StateFlow` thay vì `LiveData` không?
- [ ] Mọi dependency đã được inject qua Hilt không?
- [ ] Đã handle các trường hợp Loading/Error trong UI chưa?
- [ ] Có hardcode (chuỗi cứng) trong code không? (Hãy dùng `strings.xml`)

---
*"Code dễ đọc hơn là code thông minh. Hãy giữ mọi thứ đơn giản."*