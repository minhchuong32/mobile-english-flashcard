# Hướng Dẫn Tài Liệu - Mobile English Flashcard

Dự án này có ba tài liệu chính giải thích các khái niệm kỹ thuật và cách hoạt động của các thành phần:

---

## 📚 Các Tài Liệu

### 1. **README_REPORT.md** - Báo cáo Dự án Chi tiết
**Vị trí**: `README_REPORT.md`

**Nội dung**:
- Tổng quan dự án
- Cấu trúc chính
- Luồng khởi động ứng dụng
- Luồng điều hướng (Navigation flows)
- Báo cáo từng chức năng chính (đăng nhập, đăng ký, deck, flashcard, exercise, v.v.)
- Các khái niệm cơ bản:
  - Jetpack Compose, Composable, Composition, Recomposition
  - ViewModel, Repository, StateFlow
  - Retrofit, OkHttp, Gson
  - Firebase Cloud Messaging (FCM)
  - Navigation & Route
  - Dependency Injection & AppModule
  - Coroutine & suspend functions
  - Data Class, Sealed Class
  - Lifecycle & Context
  - SharedPreferences
  - AndroidManifest.xml & Permissions
- Design Patterns & Best Practices:
  - MVVM Architecture
  - Repository Pattern
  - Singleton Pattern
  - Builder Pattern
  - Feature-First Architecture
  - Declarative UI
  - Reactive Programming

**Dành cho**: 
- Người muốn hiểu toàn bộ project
- Báo cáo và tài liệu chính thức
- Overview các tính năng và luồng xử lý

**Cách sử dụng**:
- Đọc từ trên xuống
- Tìm kiếm tính năng cụ thể (Ctrl+F)

---

### 2. **TECHNICAL_CONCEPTS.md** - Giải Thích Chi Tiết Các Khái Niệm
**Vị trí**: `TECHNICAL_CONCEPTS.md`

**Nội dung**:
- Jetpack Compose & UI Framework (chi tiết)
- Architecture Patterns (MVVM, Repository)
- Networking (Retrofit, OkHttp, Gson) - cách hoạt động
- Firebase & Push Notifications
- Navigation & Routing - routing strategy
- Dependency Injection & AppModule - DI patterns
- Kotlin Coroutines & Async Programming
- Local Storage & State Management
- Build System & Dependencies
- Project Structure & Organization

**Đặc điểm**:
- Giải thích **từng khái niệm sâu**
- Code examples chi tiết
- So sánh cách làm sai vs cách làm đúng
- Kỹ thuật và best practices

**Dành cho**:
- Developer muốn hiểu kỹ từng technology
- Người học Android/Kotlin
- Những ai muốn biết "tại sao" không chỉ "cách làm"

**Cách sử dụng**:
- Chọn chủ đề quan tâm từ Mục Lục
- Đọc chi tiết với code examples
- Tìm kiếm technology cụ thể

---

### 3. **IMPLEMENTATION_GUIDE.md** - Hướng Dẫn Cách Thành Phần Hoạt Động Với Nhau
**Vị trí**: `IMPLEMENTATION_GUIDE.md`

**Nội dung**:
- Luồng Đăng Nhập (Complete Flow) - step-by-step
- Luồng Tạo & Xem Deck
- Luồng Học Flashcard & SRS
- Quản Lý State trong Compose
- Calling APIs & Error Handling
- Firebase Notifications Flow
- Dependency Injection Flow

**Đặc điểm**:
- Trình bày **end-to-end flows**
- Cho thấy cách các component tương tác
- Diagrams và step-by-step explanations
- Practical code examples

**Dành cho**:
- Developer muốn hiểu flow hoàn chỉnh
- Người debugging issue
- Những ai muốn biết luồng dữ liệu

**Cách sử dụng**:
- Chọn feature cần hiểu (đăng nhập, SRS, notifications, v.v.)
- Đọc flow từ UI → ViewModel → Repository → API
- Xem code examples ở mỗi layer

---

## 🎯 Quick Links

### Tìm hiểu về tính năng cụ thể:

**Đăng nhập:**
- README_REPORT.md → Section 5.1
- IMPLEMENTATION_GUIDE.md → Luồng Đăng Nhập

**Deck Management:**
- README_REPORT.md → Section 5.4, 5.5
- IMPLEMENTATION_GUIDE.md → Luồng Tạo & Xem Deck

**Flashcard Learning:**
- README_REPORT.md → Section 5.6, 5.7
- IMPLEMENTATION_GUIDE.md → Luồng Học Flashcard & SRS

**SRS (Spaced Repetition):**
- IMPLEMENTATION_GUIDE.md → Luồng Học Flashcard & SRS

**Push Notifications:**
- README_REPORT.md → Section 5.9, 6.12
- IMPLEMENTATION_GUIDE.md → Firebase Notifications Flow

**Navigation:**
- README_REPORT.md → Section 4, 6.13
- TECHNICAL_CONCEPTS.md → Section 5

---

## 📖 Hướng Dẫn Theo Kỹ Năng

### Bắt đầu học Android/Compose:
1. Đọc README_REPORT.md sections 6.1-6.7
2. Xem TECHNICAL_CONCEPTS.md Section 1-2

### Muốn hiểu Networking:
1. TECHNICAL_CONCEPTS.md Section 3
2. IMPLEMENTATION_GUIDE.md → Calling APIs

### Muốn hiểu State Management:
1. TECHNICAL_CONCEPTS.md Section 8
2. IMPLEMENTATION_GUIDE.md → Quản Lý State

### Muốn hiểu complete flow:
1. Chọn tính năng trong IMPLEMENTATION_GUIDE.md
2. Đọc lại từng layer
3. Cross-reference với TECHNICAL_CONCEPTS.md

### Debug issue:
1. Tìm feature ở IMPLEMENTATION_GUIDE.md
2. Xác định layer có issue
3. Xem code examples ở layer đó
4. Tìm kiếm technology ở TECHNICAL_CONCEPTS.md

---

## 🔍 Tìm Kiếm Các Keyword Phổ Biến

| Keyword | Tài Liệu | Section |
|---------|---------|---------|
| **Compose** | README_REPORT.md | 6.1-6.5 |
| **ViewModel** | README_REPORT.md | 6.6 |
| **Repository** | README_REPORT.md | 6.7 |
| **Retrofit** | README_REPORT.md | 6.9 |
| **Retrofit Chi Tiết** | TECHNICAL_CONCEPTS.md | 3.1 |
| **OkHttp** | README_REPORT.md | 6.10 |
| **Gson** | README_REPORT.md | 6.11 |
| **FCM** | README_REPORT.md | 6.12 |
| **Navigation** | README_REPORT.md | 4, 6.13 |
| **MVVM** | README_REPORT.md | 7.1 |
| **AppModule** | README_REPORT.md | 6.14 |
| **Coroutine** | README_REPORT.md | 6.15 |
| **Flow & StateFlow** | TECHNICAL_CONCEPTS.md | 7.4 |
| **Login Flow** | IMPLEMENTATION_GUIDE.md | Luồng Đăng Nhập |
| **SRS Flow** | IMPLEMENTATION_GUIDE.md | Luồng Học Flashcard |
| **Notifications** | IMPLEMENTATION_GUIDE.md | Firebase Notifications |

---

## ✅ Checklist Đọc Tài Liệu

Nếu bạn mới với project:

- [ ] Đọc README_REPORT.md Sections 1-3 (Overview)
- [ ] Đọc README_REPORT.md Sections 4-5 (Navigation & Features)
- [ ] Chọn 1 tính năng quan tâm, xem luồng ở IMPLEMENTATION_GUIDE.md
- [ ] Đọc các khái niệm liên quan ở README_REPORT.md Section 6
- [ ] Nếu muốn hiểu sâu, xem TECHNICAL_CONCEPTS.md

Nếu bạn muốn debug/modify code:

- [ ] Tìm tính năng ở IMPLEMENTATION_GUIDE.md
- [ ] Xác định layer (UI/ViewModel/Repository/API)
- [ ] Xem code example của layer đó
- [ ] Cross-reference technology ở TECHNICAL_CONCEPTS.md nếu cần

---

## 📞 Khi Nào Dùng Tài Liệu Nào?

**Khi bạn hỏi:**
- "Tính năng này làm gì?" → README_REPORT.md Section 5
- "Architecture của project là gì?" → README_REPORT.md Sections 2, 7
- "Compose/Retrofit/FCM là gì?" → README_REPORT.md Section 6
- "Compose hoạt động như thế nào?" → TECHNICAL_CONCEPTS.md Section 1
- "Flow từ UI đến API là sao?" → IMPLEMENTATION_GUIDE.md
- "Làm sao tích hợp feature mới?" → IMPLEMENTATION_GUIDE.md (reference flow tương tự)

---

## 🚀 Bắt Đầu Ngay

### Lựa chọn 1:

**Muốn overview nhanh**: 5 phút
1. Đọc README_REPORT.md Sections 1-3

**Muốn hiểu sâu hơn**: 30 phút
1. Đọc README_REPORT.md full
2. Skim TECHNICAL_CONCEPTS.md

**Muốn trở thành expert**: 2 giờ+
1. Đọc tất cả 3 tài liệu
2. Code examples
3. Thực hành

---

## 📝 Ghi Chú

- Tất cả 3 tài liệu đều dùng **Tiếng Việt**
- Code examples là **Kotlin**
- References tới source files thực tế trong project
- Regular updates khi project thay đổi

---

**Good luck coding! 🎉**

