package com.example.englishflashcard.navigation

sealed class Screen(val route: String) {
    // Danh sách route điều hướng của app. Mỗi `data object` đại diện cho
    // một màn hình/destination trong NavHost.
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Decks : Screen("decks")
    // Route có path param `{deckId}`: dùng khi cần truyền ID deck vào URL.
    data object DeckDetail : Screen("deck_detail/{deckId}") {
        // Tạo route thực tế bằng cách thay `{deckId}` bằng giá trị thật.
        fun createRoute(deckId: String) = "deck_detail/$deckId"
    }
    // Thêm thẻ vào deck cụ thể, cũng dùng path param `deckId`.
    data object AddCard : Screen("add_card/{deckId}") {
        // Tạo route cho màn thêm thẻ theo deck.
        fun createRoute(deckId: String) = "add_card/$deckId"
    }
    data object CreateDeck : Screen("create_deck")
    data object Profile : Screen("profile")
    // Route kết hợp path param và query param:
    // - `{mode}` là bắt buộc trong path
    // - `deckId` là tùy chọn, nằm sau dấu `?`
    data object FlashcardStudy : Screen("flashcard_study/{mode}?deckId={deckId}") {
        // Nếu có deckId thì thêm query param, nếu không thì chỉ truyền mode.
        fun createRoute(mode: String, deckId: String? = null) =
            if (deckId != null) "flashcard_study/$mode?deckId=$deckId" else "flashcard_study/$mode"
    }
    data object DeckExplorer : Screen("deck_explorer")
    // Route exercise dùng query param tùy chọn deckId.
    data object Exercise : Screen("exercise?deckId={deckId}") {
        // Nếu có deckId thì nối vào query string, nếu không thì mở chế độ chung.
        fun createRoute(deckId: String? = null) =
            if (deckId != null) "exercise?deckId=$deckId" else "exercise"
    }

    data object ForgotPassword : Screen("forgot_password")
    data object ResetPassword : Screen("reset_password")
    data object VerifyForgotOtp :
        Screen("verify_forgot_otp")
}
