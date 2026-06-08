package com.example.englishflashcard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.englishflashcard.data.repository.*
import com.example.englishflashcard.feature.auth.AuthViewModel
import com.example.englishflashcard.feature.auth.ForgotPasswordScreen
import com.example.englishflashcard.feature.auth.LoginScreen
import com.example.englishflashcard.feature.auth.RegisterScreen
import com.example.englishflashcard.feature.auth.ResetPasswordScreen
import com.example.englishflashcard.feature.auth.VerifyForgotOtpScreen
import com.example.englishflashcard.feature.deck.AddCardScreen
import com.example.englishflashcard.feature.deck.CreateDeckScreen
import com.example.englishflashcard.feature.deck.DeckDetailScreen
import com.example.englishflashcard.feature.deck.DecksScreen
import com.example.englishflashcard.feature.deck.DeckExplorerScreen
import com.example.englishflashcard.feature.exercise.ExerciseScreen
import com.example.englishflashcard.feature.home.HomeScreen
import com.example.englishflashcard.feature.main.MainShellScreen
import com.example.englishflashcard.feature.profile.ProfileScreen
import com.example.englishflashcard.feature.flashcard.FlashcardScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userRepository: UserRepository,
    authRepository: AuthRepository,
    deckRepository: DeckRepository,
    cardRepository: CardRepository,
    srsRepository: SrsRepository,
    analyticsRepository: AnalyticsRepository,
    notificationRepository: NotificationRepository,
    dictionaryRepository: DictionaryRepository
) {

    // LƯU Ý: Tạo một instance của AuthViewModel và giữ nó giữa các lần
    // recomposition bằng `remember { ... }`. Ý nghĩa:
    //  - AuthViewModel được tạo một lần cho composition này và sẽ không
    //    được tạo lại mỗi khi `AppNavGraph` bị recomposed.
    //  - Vì tạo thủ công (không dùng `hiltViewModel()` hay `viewModel()`),
    //    nó KHÔNG được scope theo lifecycle Android chuẩn (ví dụ: không
    //    tự động phục hồi sau process death hoặc SavedState). Dùng cách này
    //    khi bạn muốn state chỉ tồn tại trong composition.
    //  - Nếu cần behavior theo lifecycle (survive cấu hình, dùng SavedState
    //    hoặc share qua NavBackStackEntry), nên dùng `hiltViewModel()` trong
    //    từng composable hoặc `navBackStackEntry.viewModel()` để scope ViewModel
    //    theo navigation graph.
    //  - Nếu repository truyền vào hàm này thay đổi identity giữa các
    //    recomposition, block `remember` sẽ KHÔNG chạy lại (vì không có key).
    //    Nếu repository có thể thay đổi, cân nhắc dùng `remember(key1 = repo)`
    //    hoặc đảm bảo caller cung cấp instance ổn định.
    val authViewModel = remember {
        AuthViewModel(
            deckRepository = deckRepository,
            authRepository = authRepository
        )
    }

    // `NavHost` sẽ compose các destination. Mỗi lambda `composable {}` là
    // một recomposition scope: khi state mà destination đó đọc thay đổi,
    // chỉ composable đó (và các con) sẽ bị recomposed.
    // Tránh thực hiện công việc nặng trực tiếp trong thân composable;
    // để logic ở ViewModel/Repository và giữ UI ở trạng thái thuần (stateless).
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {

            // LoginScreen: màn này đọc state từ `authViewModel`. Khi state
            // exposed bởi ViewModel (StateFlow/LiveData) thay đổi, chỉ những
            // composable thu thập state đó sẽ recompose. Việc truyền cùng
            // instance `authViewModel` giúp tránh tạo lại ViewModel giữa các
            // màn auth trong composition này.
            LoginScreen(
                viewModel = authViewModel,

                onGoRegister = {
                    navController.navigate(Screen.Register.route)
                },

                onForgotPasswordClick = {
                    navController.navigate(Screen.ForgotPassword.route)
                },

                onLoginSuccess = {
                    // Khi đăng nhập thành công: xóa cache SRS để đảm bảo dữ
                    // liệu của user mới sẽ được load lại. Sau đó chuyển tới
                    // Home và loại Login khỏi back stack để người dùng không
                    // thể quay lại bằng Back.
                    srsRepository.clearCache()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Register.route) {

            // RegisterScreen: dùng chung `authViewModel` để các bước
            // register/verify/reset có thể đọc/ghi cùng một state tạm thời
            // (email, otp, resetToken) mà không cần truyền nhiều navigation
            // args. Vì viewModel được remember, các trường này tồn tại qua
            // các lần recomposition trong composition đang active.
            RegisterScreen(
                viewModel = authViewModel,

                onGoLogin = {
                    navController.popBackStack()
                },

                onRegisterSuccess = {
                    // Sau khi đăng ký thành công: xóa cache SRS. Nếu server
                    // phát hành resetToken (ví dụ yêu cầu OTP), chuyển tới
                    // ResetPassword; nếu không, chuyển tới Home và loại Login
                    // khỏi back stack.
                    // Lưu ý: việc đọc `authViewModel.resetToken` ở đây không
                    // kích hoạt recomposition; nó chỉ ảnh hưởng luồng điều
                    // khi. Recomposition xảy ra ở những màn thu thập state.
                    srsRepository.clearCache()

                    if (authViewModel.resetToken.isNotBlank()) {
                        navController.navigate(Screen.ResetPassword.route)
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {

            // ForgotPasswordScreen: màn này ghi OTP vào `authViewModel`
            // dùng chung. Việc đặt `authViewModel.otp = ""` ở đây để xóa
            // input cũ trước khi điều hướng; thay đổi này chỉ gây
            // recomposition ở các composable nào thu thập `otp`.
            ForgotPasswordScreen(
                viewModel = authViewModel,

                onOtpSent = {

                    authViewModel.otp = ""

                    navController.navigate(
                        Screen.VerifyForgotOtp.route
                    )
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.VerifyForgotOtp.route) {

            VerifyForgotOtpScreen(
                viewModel = authViewModel,

                onSuccess = {
                    navController.navigate(
                        Screen.ResetPassword.route
                    )
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.ResetPassword.route) {

            // ResetPasswordScreen: khi thành công, xóa các trường liên quan
            // tới auth trong viewModel dùng chung. Việc xóa này chỉ gây
            // recomposition cho UI quan sát các trường đó. Sau đó điều hướng
            // về Login. Lưu ý: đang dùng `popUpTo(0)` để xóa back stack; rõ
            // ràng hơn là dùng `popUpTo(Screen.Login.route) { inclusive = true }`.
            ResetPasswordScreen(
                viewModel = authViewModel,

                onSuccess = {

                    authViewModel.email = ""
                    authViewModel.otp = ""
                    authViewModel.resetToken = ""
                    authViewModel.newPassword = ""

                    navController.navigate(
                        Screen.Login.route
                    ) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            // MainShellScreen được tái sử dụng cho nhiều route ở mức cao
            // và đóng vai tabbed shell của app. Vì được khởi tạo ở đây với
            // các giá trị `initialTab` khác nhau, đảm bảo MainShell tự
            // scope và remember state nội bộ đúng cách để khi chuyển giữa
            // các composable này không vô tình tạo lại tài nguyên tốn kém.
            // Compose sẽ recompose MainShellScreen khi các tham số (vd: repo)
            // hoặc state mà nó đọc thay đổi.
            MainShellScreen(
                userRepository = userRepository,
                deckRepository = deckRepository,
                analyticsRepository = analyticsRepository,
                srsRepository = srsRepository,
                onStudyFlashcard = { navController.navigate(Screen.FlashcardStudy.createRoute("flashcard")) },
                onStudyExercise = { deckId -> navController.navigate(Screen.Exercise.createRoute(deckId)) },
                onStudyDeck = { deckId -> navController.navigate(Screen.FlashcardStudy.createRoute("flashcard", deckId)) },
                onDeckDetail = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onCreateDeck = { navController.navigate(Screen.CreateDeck.route) },
                onLogout = {
                    // Luồng logout: xóa cache, gọi repository.logout(), sau đó
                    // điều hướng về Login và xóa back stack để người dùng không
                    // thể quay lại các màn đã xác thực.
                    srsRepository.clearCache()
                    userRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                initialTab = 0
            )
        }

        composable(Screen.Decks.route) {
            MainShellScreen(
                userRepository = userRepository,
                deckRepository = deckRepository,
                analyticsRepository = analyticsRepository,
                srsRepository = srsRepository,
                onStudyFlashcard = { navController.navigate(Screen.FlashcardStudy.createRoute("flashcard")) },
                onStudyExercise = { deckId -> navController.navigate(Screen.Exercise.createRoute(deckId)) },
                onStudyDeck = { deckId -> navController.navigate(Screen.FlashcardStudy.createRoute("flashcard", deckId)) },
                onDeckDetail = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onCreateDeck = { navController.navigate(Screen.CreateDeck.route) },
                onLogout = {
                    srsRepository.clearCache()
                    userRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                initialTab = 1
            )
        }

        composable(
            route = Screen.DeckDetail.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            DeckDetailScreen(
                deckRepository = deckRepository,
                cardRepository = cardRepository,
                srsRepository = srsRepository,
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onAddCard = { navController.navigate(Screen.AddCard.createRoute(deckId)) },
                onStudyFlashcards = { id -> navController.navigate(Screen.FlashcardStudy.createRoute("flashcard", id)) },
                onStudyExercise = { id -> navController.navigate(Screen.Exercise.createRoute(id)) }
            )
        }

        composable(
            route = Screen.AddCard.route,
            arguments = listOf(navArgument("deckId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId") ?: ""
            AddCardScreen(
                dictionaryRepository = dictionaryRepository,
                cardRepository = cardRepository,
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.FlashcardStudy.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("deckId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "flashcard"
            val deckId = backStackEntry.arguments?.getString("deckId")
            FlashcardScreen(
                deckRepository = deckRepository,
                cardRepository = cardRepository,
                srsRepository = srsRepository,
                analyticsRepository = analyticsRepository,
                mode = mode,
                deckId = deckId,
                onBack = { navController.popBackStack() },
                onNavigateToExercise = { targetDeckId ->
                    navController.navigate(Screen.Exercise.createRoute(targetDeckId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.CreateDeck.route) {
            CreateDeckScreen(
                dictionaryRepository = dictionaryRepository,
                deckRepository = deckRepository,
                cardRepository = cardRepository,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            MainShellScreen(
                userRepository = userRepository,
                deckRepository = deckRepository,
                analyticsRepository = analyticsRepository,
                srsRepository = srsRepository,
                onStudyFlashcard = { navController.navigate(Screen.FlashcardStudy.createRoute("flashcard")) },
                onStudyExercise = { deckId -> navController.navigate(Screen.Exercise.createRoute(deckId)) },
                onStudyDeck = { deckId -> navController.navigate(Screen.FlashcardStudy.createRoute("flashcard", deckId)) },
                onDeckDetail = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onCreateDeck = { navController.navigate(Screen.CreateDeck.route) },
                onLogout = {
                    srsRepository.clearCache()
                    userRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                initialTab = 3
            )
        }

        composable(Screen.DeckExplorer.route) {
            MainShellScreen(
                userRepository = userRepository,
                deckRepository = deckRepository,
                analyticsRepository = analyticsRepository,
                srsRepository = srsRepository,
                onStudyFlashcard = { navController.navigate(Screen.FlashcardStudy.createRoute("flashcard")) },
                onStudyExercise = { deckId -> navController.navigate(Screen.Exercise.createRoute(deckId)) },
                onStudyDeck = { deckId -> navController.navigate(Screen.FlashcardStudy.createRoute("flashcard", deckId)) },
                onDeckDetail = { deckId -> navController.navigate(Screen.DeckDetail.createRoute(deckId)) },
                onCreateDeck = { navController.navigate(Screen.CreateDeck.route) },
                onLogout = {
                    srsRepository.clearCache()
                    userRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                initialTab = 1
            )
        }

        composable(
            route = Screen.Exercise.route,
            arguments = listOf(
                navArgument("deckId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val deckId = backStackEntry.arguments?.getString("deckId")
            ExerciseScreen(
                deckRepository = deckRepository,
                cardRepository = cardRepository,
                srsRepository = srsRepository,
                analyticsRepository = analyticsRepository,
                deckId = deckId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
