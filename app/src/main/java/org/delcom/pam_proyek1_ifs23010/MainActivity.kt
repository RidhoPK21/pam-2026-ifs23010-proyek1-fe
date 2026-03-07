package org.delcom.pam_proyek1_ifs23010

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_proyek1_ifs23010.ui.UIApp
import org.delcom.pam_proyek1_ifs23010.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel // Ganti import ke EventViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Ubah inisiasi ViewModel
    private val eventViewModel: EventViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DelcomTheme {
                UIApp(
                    eventViewModel = eventViewModel, // Ubah parameter yang dikirim
                    authViewModel = authViewModel
                )
            }
        }
    }
}