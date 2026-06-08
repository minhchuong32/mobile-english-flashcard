README - SRS / Exercise (Hệ thống Lặp lại ngắt quãng & Bài tập)

Mục đích

Mô tả luồng học theo SRS (Spaced Repetition System) và các màn hình bài tập/Exercise.

Vị trí tham khảo

- UI: feature/exercise/ExerciseScreen.kt, ExerciseViewModel.kt
- Repository: data/repository/SrsRepository.kt, StudySessionApiService.kt
- Model: model/StudySession.kt, CardProgress.kt

Luồng chính

1) Chuẩn bị bộ thẻ để ôn
- Người dùng chọn "Học" trên một bộ thẻ hoặc từ Home/DeckDetail.
- ViewModel gọi SrsRepository.getCardsToReview(deckId, limit).
- SrsRepository áp dụng filter: nextDue <= now, trạng thái active, có progress metadata.

2) Bắt đầu Study session
- Tạo StudySession (local và gửi lên server nếu cần). Ghi lại thời gian bắt đầu, danh sách cardIds.
- UI lần lượt hiển thị thẻ theo thứ tự (theo priority hoặc random với hạn chế).

3) Người dùng phản hồi (Remember / Hard / Forgot)
- Mỗi phản hồi cập nhật CardProgress: lần cuối học, lịch tiếp theo (nextDue), interval, ease factor.
- SrsRepository.saveProgress(cardId, result) -> cập nhật local DB và gọi API cập nhật progress nếu online.

4) Kết thúc session
- Ghi lại StudySession kết thúc (thời gian, số thẻ hoàn thành, accuracy).
- Gửi báo cáo session đến server (StudySessionApiService.postSession) nếu backend hỗ trợ.

Edge cases

- Offline: lưu toàn bộ progress và session local, sync khi có mạng.
- Mid-session app killed: lưu trạng thái session định kỳ (auto-save) để resume.
- Card bị xóa giữa chừng: bỏ qua card đó và tiếp tục.

Testing

- Tạo nhiều card với nextDue khác nhau, kiểm tra lọc và thứ tự.
- Mô phỏng offline update và sync sau khi online.

Gợi ý kỹ thuật

- Sử dụng thuật toán SM-2 (hoặc biến thể) cho scheduling.
- Lưu bản ghi audit cho mỗi thay đổi progress để debug.


