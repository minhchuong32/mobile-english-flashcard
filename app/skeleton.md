# Android Project Architecture: English Learning Platform

Dự án này sử dụng kiến trúc **MVVM (Model-View-ViewModel)** hướng theo **Feature-first**.
Mục tiêu là giúp dự án:

* Dễ mở rộng
* Dễ test
* Không bị rườm rà bởi các layer trung gian không cần thiết

---

# 1. Cấu trúc thư mục (File Tree)

```plaintext
com.example.englishflashcard
├── data/               # Layer xử lý dữ liệu thô
│   ├── api/            # Retrofit Interface (kết nối Express.js)
│   └── repository/     # Logic gọi API hoặc lấy dữ liệu từ DB
│
├── di/                 # Cấu hình Hilt (Dependency Injection)
│
├── feature/            # Các tính năng chính (Feature-first)
│   ├── lesson/         # Tính năng bài học
│   │   ├── LessonScreen.kt      # UI - Compose
│   │   └── LessonViewModel.kt   # Logic - StateFlow
│   │
│   └── quiz/           # Tính năng kiểm tra
│
├── model/              # Data Class dùng chung (DTOs, Domain Models)
│
└── ui/                 # Các thành phần UI dùng chung (Theme, Components)
```

---

# 2. Luồng dữ liệu (Data Flow)

Để đảm bảo tính nhất quán, mọi tính năng mới phải đi theo luồng sau:

## Bước 1: Định nghĩa Model

Tạo `Data Class` trong `model/` để khớp với JSON từ Express.js.

```kotlin
data class LessonDto(
    val id: Int,
    val title: String,
    val description: String
)
```

---

## Bước 2: Khai báo API

Thêm endpoint trong `data/api/ApiService.kt`.

```kotlin
interface ApiService {

    @GET("lessons")
    suspend fun getLessons(): List<LessonDto>
}
```

---

## Bước 3: Repository xử lý dữ liệu

Repository gọi API hoặc lấy dữ liệu từ DB.

```kotlin
class LessonRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getLessons(): List<LessonDto> {
        return apiService.getLessons()
    }
}
```

---

## Bước 4: ViewModel quản lý State

* Inject Repository
* Expose dữ liệu bằng `StateFlow`

```kotlin
@HiltViewModel
class LessonViewModel @Inject constructor(
    private val repository: LessonRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<LessonDto>>(emptyList())
    val lessons: StateFlow<List<LessonDto>> = _lessons

    init {
        loadLessons()
    }

    private fun loadLessons() {
        viewModelScope.launch {
            _lessons.value = repository.getLessons()
        }
    }
}
```

---

## Bước 5: UI quan sát StateFlow

Compose Screen chỉ render UI theo state.

```kotlin
@Composable
fun LessonScreen(
    viewModel: LessonViewModel = hiltViewModel()
) {
    val lessons by viewModel.lessons.collectAsState()

    LazyColumn {
        items(lessons) { lesson ->
            Text(text = lesson.title)
        }
    }
}
```

---

# 3. Quy tắc "Code Chuẩn" (Guidelines)

## Repository là Class

Không cần tạo interface cho Repository nếu:

* Chưa cần Unit Test riêng
* Chưa có nhiều implementation

Giữ Repository là class cụ thể để giảm boilerplate.

✅ Nên:

```kotlin
class LessonRepository
```

❌ Không cần thiết:

```kotlin
interface LessonRepository
class LessonRepositoryImpl
```

---

## ViewModel là trung tâm

Logic nghiệp vụ nhỏ nên nằm trong ViewModel:

* Tính điểm
* Kiểm tra đúng/sai
* Validate dữ liệu đơn giản

Chỉ tách `UseCase` khi logic:

* Quá lớn
* Dùng lại nhiều nơi
* Khó maintain

---

## Hilt là bắt buộc

Mọi dependency phải được inject qua `di/AppModule.kt`.

✅ Đúng:

```kotlin
@Provides
fun provideApiService(): ApiService
```

❌ Sai:

```kotlin
val api = Retrofit.Builder().build()
```

Không tự tạo instance thủ công bằng:

* `new`
* `object`
* Singleton tự viết

---

## UI Stateless

Screen chỉ nhận state và callback từ ViewModel.

✅ Đúng:

```kotlin
LessonScreen(
    lessons = state.lessons,
    onRetry = viewModel::reload
)
```

❌ Sai:

```kotlin
LessonScreen tự gọi API
LessonScreen tự xử lý business logic
```

---

# 4. Checklist triển khai tính năng

## Checklist

* [ ] Đã định nghĩa DTO trong `model/`?
* [ ] Endpoint đã được thêm vào `ApiService.kt`?
* [ ] Repository đã xử lý dữ liệu?
* [ ] Dữ liệu đã được expose qua `StateFlow` trong ViewModel?
* [ ] UI đã xử lý đầy đủ:

    * Loading
    * Error
    * Success
* [ ] Dependency đã được inject bằng Hilt?
* [ ] File có vượt quá ~400 dòng không?

    * Nếu có → tách Component hoặc Helper

---

# 5. Quy ước thêm (Recommended Conventions)

## Naming Convention

| Thành phần | Quy ước               |
| ---------- | --------------------- |
| Screen     | `LessonScreen.kt`     |
| ViewModel  | `LessonViewModel.kt`  |
| Repository | `LessonRepository.kt` |
| DTO        | `LessonDto.kt`        |
| UI State   | `LessonUiState.kt`    |

---

## UI State Pattern

Nên dùng sealed class cho trạng thái UI.

```kotlin
sealed class LessonUiState {

    object Loading : LessonUiState()

    data class Success(
        val data: List<LessonDto>
    ) : LessonUiState()

    data class Error(
        val message: String
    ) : LessonUiState()
}
```

---

# 6. Tư duy kiến trúc

Mục tiêu của kiến trúc này là:

* Đơn giản nhưng đủ mạnh
* Không over-engineering
* Tối ưu cho team nhỏ hoặc đồ án
* Dễ scale về sau

Nguyên tắc chính:

> "Code đủ sạch để maintain, không cần enterprise-level complexity."
