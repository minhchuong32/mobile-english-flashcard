README - Notifications (Thông báo & Push)

Mục đích

Mô tả luồng xử lý thông báo: push (Firebase), local notifications, và thông báo nhắc học (study reminders).

Vị trí tham khảo

- Config: config/MyFirebaseMessagingService.kt
- Repository: data/repository/NotificationRepository.kt
- Model: model/Notification.kt

Luồng chính

1) Nhận push (Firebase)
- Ứng dụng sử dụng `MyFirebaseMessagingService` để nhận tin nhắn từ FCM.
- Khi nhận notification, xử lý payload: nếu là nhắc học -> hiển thị local notification, cập nhật DB nếu cần.
- Nếu payload chứa deep link -> mở màn hình tương ứng.

2) Lịch nhắc học local
- NotificationRepository quản lý lịch nhắc (user-configurable): thời gian hàng ngày, tần suất.
- Sử dụng WorkManager/AlarmManager để lập lịch local notification.

3) Hiển thị & Lưu thông báo
- Lưu thông báo thành phần trong DB để hiển thị trong màn thông báo (notification center) trong app.

Edge cases

- Quyền thông báo bị tắt -> hiển thị UI hướng dẫn bật.
- Tin nhắn chứa payload malformed -> fallback an toàn.

Gợi ý kỹ thuật

- Kiểm tra token FCM và đăng ký server nếu backend cần.
- Tách logic xử lý payload vào repository để dễ test.


