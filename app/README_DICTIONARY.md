README - Dictionary (Từ điển & Tra cứu)

Mục đích

Mô tả luồng chức năng tra cứu từ, lấy nghĩa, ví dụ, phát âm và lưu từ yêu thích.

Vị trí tham khảo

- UI: có thể dùng các màn hình trong feature/deck hoặc feature/flashcard khi tra từ
- Repository: data/repository/DictionaryRepository.kt
- API: data/api/DictionaryApiService.kt
- Model: model/Dictionary.kt

Luồng chính

1) Tìm kiếm từ
- Người dùng nhập từ vào ô tìm kiếm.
- ViewModel gọi DictionaryRepository.search(term).
- Repository gọi DictionaryApiService.search(term) (hoặc local DB cache) và trả về danh sách kết quả: nghĩa, loại từ, ví dụ, âm thanh.
- UI hiển thị kết quả, hỗ trợ xem chi tiết và nghe phát âm.

2) Lưu từ yêu thích
- Từ chi tiết có nút "Lưu" -> DictionaryRepository.saveFavorite(word).
- Lưu vào DB local (Room) và sync với server nếu cần.

3) Tự động hoàn thành & gợi ý
- Khi người dùng gõ, gọi suggestions endpoint hoặc tìm trong cache để gợi ý.

Edge cases

- Từ không tồn tại -> hiển thị thông báo và gợi ý tương tự.
- Lỗi mạng -> fallback: nếu có cache, trả cache.

Gợi ý kỹ thuật

- Throttle/debounce input trước khi gọi API để giảm request.
- Cache kết quả tìm kiếm phổ biến.


