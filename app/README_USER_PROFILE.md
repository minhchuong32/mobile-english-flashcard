README - User & Profile (Người dùng, Hồ sơ)

Mục đích

Mô tả luồng liên quan đến quản lý người dùng: hiển thị profile, cập nhật thông tin, avatar, theo dõi tiến trình học.

Vị trí tham khảo

- UI: feature/profile/ProfileScreen.kt, ProfileViewModel.kt
- Repository: data/repository/UserRepository.kt
- API: data/api/UserApiService.kt
- Model: model/User.kt, UserStudyProgress.kt, UserStreak.kt

Luồng chính

1) Hiển thị Profile
- Khi mở ProfileScreen, ViewModel gọi UserRepository.getCurrentUser() -> trả user cached hoặc gọi API.
- Hiển thị avatar, tên, email, thống kê học tập (UserStudyProgress, streak).

2) Cập nhật thông tin
- Thay đổi tên, avatar: UI gọi ViewModel -> UserRepository.updateProfile(request).
- Thực hiện upload avatar (multipart) nếu cần, sau đó cập nhật thông tin user.

3) Theo dõi tiến trình học
- UserRepository / AnalyticsRepository cung cấp các metrics: thời gian học, số thẻ hoàn thành, streak.
- Hiển thị biểu đồ/summary trên ProfileScreen.

Edge cases

- Quyền truy cập: nếu token hết hạn, handle 401 -> chuyển login.
- Upload avatar lớn -> dùng progress indicator và validate kích thước/loại file.

Gợi ý kỹ thuật

- Lưu user hiện tại trong một SharedPreference/Flow để các UI khác quan sát.
- Khi cập nhật profile, làm optimistic update trên UI và rollback nếu server lỗi.


