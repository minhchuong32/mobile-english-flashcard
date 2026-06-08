README - Analytics (Phân tích & Thống kê)

Mục đích

Mô tả luồng thu thập và hiển thị số liệu học tập: thời gian học, số thẻ đã học, tiến trình theo ngày, streak.

Vị trí tham khảo

- UI: feature/analytic/AnalyticsScreen.kt, AnalyticsViewModel.kt
- Repository: data/repository/AnalyticsRepository.kt
- Model: model/Analytics.kt, UserStudyProgress.kt

Luồng chính

1) Thu thập sự kiện
- Khi người dùng bắt đầu/hoàn thành study session hoặc hoàn thành một thẻ, gửi event tới AnalyticsRepository.logEvent(event).
- AnalyticsRepository có thể gửi event tới server hoặc tới Firebase Analytics.

2) Tạo báo cáo
- AnalyticsRepository cung cấp các endpoint/getters: getDailySummary(), getStudyTrends(), getStreakInfo().
- ViewModel gọi repository và transform dữ liệu thành UI models (chart points, labels).

3) Hiển thị trên UI
- AnalyticsScreen hiển thị biểu đồ, bảng tóm tắt, và khuyến nghị dựa trên dữ liệu.

Edge cases

- Offline events: cache events và gửi khi có mạng.
- Privacy: tôn trọng user opt-out của analytics.

Gợi ý kỹ thuật

- Làm batch upload events để tiết kiệm request.
- Dùng caching & pre-aggregation cho báo cáo nặng.


