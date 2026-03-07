package org.delcom.pam_proyek1_ifs23010.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.delcom.pam_proyek1_ifs23010.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23010.ui.components.CustomSnackbar
import org.delcom.pam_proyek1_ifs23010.ui.screens.HomeScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.ProfileScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.auth.AuthLoginScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.auth.AuthRegisterScreen
// Pastikan import di bawah ini disesuaikan ke folder 'events' yang baru
import org.delcom.pam_proyek1_ifs23010.ui.screens.events.EventsAddScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.events.EventsDetailScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.events.EventsEditScreen
import org.delcom.pam_proyek1_ifs23010.ui.screens.events.EventsScreen
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel // Ubah import ViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UIApp(
    navController: NavHostController = rememberNavController(),
    eventViewModel: EventViewModel, // Ubah dari TodoViewModel
    authViewModel: AuthViewModel
) {
    // Inisialisasi SnackbarHostState
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState){ snackbarData ->
            CustomSnackbar(snackbarData, onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
        } },
    ) { _ ->
        NavHost(
            navController = navController,
            startDestination = ConstHelper.RouteNames.Home.path,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F8FA))

        ) {
            // Auth Login
            composable(
                route = ConstHelper.RouteNames.AuthLogin.path,
            ) { _ ->
                AuthLoginScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            // Auth Register
            composable(
                route = ConstHelper.RouteNames.AuthRegister.path,
            ) { _ ->
                AuthRegisterScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                )
            }

            // Home
            composable(
                route = ConstHelper.RouteNames.Home.path,
            ) { _ ->
                HomeScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel // Ubah dari todoViewModel
                )
            }

            // Profile
            composable(
                route = ConstHelper.RouteNames.Profile.path,
            ) { _ ->
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel // Ubah dari todoViewModel
                )
            }

            // Events (Dulu Todos)
            composable(
                route = ConstHelper.RouteNames.Events.path,
            ) { _ ->
                EventsScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel // Ubah dari todoViewModel
                )
            }

            // Events Add (Dulu Todos Add)
            composable(
                route = ConstHelper.RouteNames.EventsAdd.path,
            ) { _ ->
                EventsAddScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel // Ubah dari todoViewModel
                )
            }

            // Events Detail (Dulu Todos Detail)
            composable(
                route = ConstHelper.RouteNames.EventsDetail.path,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }, // Ubah argumen ke eventId
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

                EventsDetailScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel, // Ubah dari todoViewModel
                    eventId = eventId // Ubah dari todoId
                )
            }

            // Events Edit (Dulu Todos Edit)
            composable(
                route = ConstHelper.RouteNames.EventsEdit.path,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }, // Ubah argumen ke eventId
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""

                EventsEditScreen(
                    navController = navController,
                    snackbarHost = snackbarHostState,
                    authViewModel = authViewModel,
                    eventViewModel = eventViewModel, // Ubah dari todoViewModel
                    eventId = eventId // Ubah dari todoId
                )
            }
        }
    }
}