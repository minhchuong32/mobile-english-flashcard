README - Study Session (Phiên học)

Mục đích

Mô tả luồng tạo, quản lý và kết thúc một phiên học (StudySession).

Vị trí tham khảo

- Model: model/StudySession.kt, StudySessionDtos.kt
- API: data/api/StudySessionApiService.kt
- Repositories: SrsRepository.kt, StudySessionApiService

Luồng chính

1) Bắt đầu phiên
- Người dùng chọn "Start study" trên một deck.
- Tạo StudySession object: id (UUID), userId, deckId, startTime, initialCardIds.
- Lưu local (DB) ngay lập tức và gửi createSession lên server nếu online.

2) Trong phiên
- Ghi nhận mỗi hành động: cardId, response, timestamp.
- Cập nhật progress per-card và lưu vào session record.
- Session có trạng thái: active, paused, completed.

3) Kết thúc phiên
- Khi user hoàn thành hoặc dừng, set endTime, summary (correctCount, total, accuracy).
- Gửi session summary lên server (StudySessionApiService.postSession).
- Cập nhật user analytics (AnalyticsRepository.logSession).

Edge cases

- Resume: nếu app bị kill, có thể resume session active từ DB nếu gần đây.
- Partial sync: handle trường hợp chỉ một phần dữ liệu session được gửi (retry mechanism).

Gợi ý kỹ thuật

- Mã hóa session id và userId an toàn nếu lưu trên thiết bị.
- Batching events và gửi cuối phiên để giảm request; nhưng cũng lưu cục bộ để tránh mất dữ liệu.


