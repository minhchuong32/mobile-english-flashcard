README - Decks (Bộ thẻ)

Mục đích

Mô tả luồng nghiệp vụ liên quan đến quản lý bộ thẻ (deck): tạo, chỉnh sửa, xóa, duyệt, xem chi tiết.

Vị trí tham khảo

- UI: feature/deck/*.kt (DecksScreen, CreateDeckScreen, DeckDetailScreen, AddCardScreen, DeckExplorerScreen)
- Repository: data/repository/DeckRepository.kt
- API: data/api/DeckApiService.kt
- Model: model/FlashcardSet.kt, FlashcardSetDtos.kt

Luồng chính

1) Lấy danh sách bộ thẻ (Explorer / Home)
- UI gọi ViewModel (nếu có) để load danh sách.
- ViewModel gọi DeckRepository.getDecks(query/filter).
- DeckRepository gọi DeckApiService hoặc DB local (tùy cấu trúc) để lấy dữ liệu.
- UI hiển thị danh sách; hỗ trợ pagination, tìm kiếm, lọc theo chủ đề.

2) Tạo bộ thẻ mới
- Người dùng mở `CreateDeckScreen` và điền: tiêu đề, mô tả, chế độ công khai/riêng tư.
- UI gửi dữ liệu tới ViewModel -> gọi DeckRepository.createDeck(request).
- Nếu API trả về thành công, repository trả về đối tượng mới; UI chuyển về danh sách hoặc chi tiết.
- Edge-case: validation title rỗng, trùng tên, lỗi mạng.

3) Xem chi tiết bộ thẻ (DeckDetail)
- UI gọi DeckRepository.getDeckById(id).
- Hiển thị thông tin bộ thẻ, số thẻ, thẻ nổi bật, lịch sử học.
- Cho phép chỉnh sửa metadata hoặc xóa (call updateDeck/deleteDeck).

4) Xóa/Chỉnh sửa
- Trước khi xóa, confirm dialog.
- Chỉnh sửa gọi update API; sau thành công cập nhật local state.

Thứ tự dữ liệu & cache

- Nếu có cache local (DB/Room), ưu tiên hiển thị nhanh từ cache và đồng bộ với server.
- Giải quyết xung đột: server là nguồn chân lý; nếu chỉnh sửa offline, lưu operation queue để sync khi có mạng.

Xử lý lỗi

- Validation trên client trước khi gửi.
- Giải thích lỗi backend cho user (ví dụ: thiếu quyền truy cập khi chỉnh sửa bộ thẻ công khai của người khác).

Testing

- Tạo -> kiểm tra hiển thị chi tiết.
- Sửa -> xác nhận thay đổi đồng bộ lên server.
- Xóa -> đảm bảo removed from list.

Gợi ý triển khai

- Sử dụng DeckRepository làm single source of truth cho UI.
- ViewModel exposes StateFlow/LiveData.


