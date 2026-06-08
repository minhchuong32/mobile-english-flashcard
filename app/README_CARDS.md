README - Cards / Flashcards (Thẻ)

Mục đích

Mô tả luồng nghiệp vụ cho các thẻ trong bộ thẻ: thêm thẻ, sửa, xóa, duyệt thẻ trong chế độ học (flashcard view), và cập nhật tiến độ thẻ.

Vị trí tham khảo

- UI: feature/flashcard/FlashcardScreen.kt, AddCardScreen.kt, DeckDetailScreen.kt
- ViewModel: feature/flashcard/FlashcardViewModel.kt
- Repository: data/repository/CardRepository.kt
- API: data/api/CardApiService.kt
- Model: model/Card.kt, CardProgress.kt

Luồng chính

1) Thêm thẻ (AddCard)
- Từ `AddCardScreen`, người dùng cung cấp front (câu hỏi/tiếng Anh), back (dịch/giải thích), hình ảnh/âm thanh.
- UI gọi ViewModel -> CardRepository.addCard(deckId, cardDto).
- Repository gửi request API tạo thẻ. Trả về card mới hoặc lỗi.
- UI chuyển về chi tiết deck và cập nhật danh sách thẻ.

2) Chỉnh sửa & xóa thẻ
- Tương tự: gọi updateCard/deleteCard trên repository.
- Nếu thẻ đang được sử dụng trong study session, cần kiểm tra và xử lý (ví dụ: không cho xóa hoặc cảnh báo).

3) Duyệt thẻ trong chế độ học (FlashcardScreen)
- ViewModel lấy danh sách thẻ cần học (có thể từ SRS logic trong SrsRepository/SrsRepository.getCardsToReview).
- UI hiển thị một thẻ: front -> flip -> người dùng đánh dấu nhớ/không nhớ -> gửi feedback để cập nhật progress.
- Khi người dùng chọn 'I remembered' hoặc 'I forgot', gọi CardRepository.updateProgress(cardId, result).
- Repository tính toán và lưu progress (local + API): cập nhật CardProgress (lần cuối học, interval, easiness, nextDue).

Edge cases & handling

- Đồng bộ progress: nếu offline, queue các cập nhật progress để sync.
- Xung đột khi cùng thẻ bị chỉnh sửa trên nhiều thiết bị: ưu tiên server hoặc triển khai sự hợp nhất.
- Nội dung đa phương tiện: xử lý upload media trước khi gửi yêu cầu tạo thẻ nếu cần.

Testing

- Thêm thẻ mới chứa ảnh/âm thanh.
- Học qua nhiều thẻ, xác nhận progress tăng/giảm đúng quy tắc SRS.

Gợi ý kỹ thuật

- Tách rõ model Card và CardProgress.
- Dùng Transaction khi cập nhật nhiều bảng trong DB local.


