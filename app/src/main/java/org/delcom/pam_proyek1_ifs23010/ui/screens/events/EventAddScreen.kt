package org.delcom.pam_proyek1_ifs23010.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.delcom.pam_proyek1_ifs23010.helper.AlertHelper
import org.delcom.pam_proyek1_ifs23010.helper.AlertState
import org.delcom.pam_proyek1_ifs23010.helper.AlertType
import org.delcom.pam_proyek1_ifs23010.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23010.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23010.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23010.helper.SuspendHelper.SnackBarType
import org.delcom.pam_proyek1_ifs23010.network.events.data.DivisiEnum
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventActionUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel

@Composable
fun EventsAddScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEvent by eventViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var tmpEvent by remember { mutableStateOf<ResponseEventData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Reset status event action
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.Home.path,
                true
            )
            return@LaunchedEffect
        }

        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        uiStateEvent.eventAdd = EventActionUIState.Loading
    }

    // Simpan data
    fun onSave(
        title: String,
        description: String,
        status: String,
        tanggalPelaksanaan: String,
        tempatPelaksanaan: String,
        estimasiBiaya: String,
        divisi: String
    ) {
        if (authToken.value == null) {
            return
        }

        isLoading = true

        tmpEvent = ResponseEventData(
            title = title,
            description = description,
            status = status,
            tanggalPelaksanaan = tanggalPelaksanaan,
            tempatPelaksanaan = tempatPelaksanaan,
            estimasiBiaya = estimasiBiaya,
            divisi = divisi
        )

        eventViewModel.postEvent(
            authToken = authToken.value!!,
            title = title,
            description = description,
            status = status,
            tanggalPelaksanaan = tanggalPelaksanaan,
            tempatPelaksanaan = tempatPelaksanaan,
            estimasiBiaya = estimasiBiaya,
            divisi = divisi
        )
    }

    LaunchedEffect(uiStateEvent.eventAdd) {
        when (val state = uiStateEvent.eventAdd) {
            is EventActionUIState.Success -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.SUCCESS,
                    message = state.message
                )
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.Events.path, // Ubah rute ke Events
                    true
                )
                isLoading = false
            }
            is EventActionUIState.Error -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.ERROR,
                    message = state.message
                )
                isLoading = false
            }
            else -> {}
        }
    }

    // Tampilkan halaman loading
    if (isLoading) {
        LoadingUI()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBarComponent(
            navController = navController,
            title = "Tambah Kegiatan",
            showBackButton = true,
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            EventsAddUI(
                tmpEvent = tmpEvent,
                onSave = ::onSave
            )
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsAddUI(
    tmpEvent: ResponseEventData?,
    onSave: (
        String, // Title
        String, // Description
        String, // Status
        String, // Tanggal
        String, // Tempat
        String, // Estimasi Biaya
        String  // Divisi
    ) -> Unit
) {
    val alertState = remember { mutableStateOf(AlertState()) }

    var dataTitle by remember { mutableStateOf(tmpEvent?.title ?: "") }
    var dataDescription by remember { mutableStateOf(tmpEvent?.description ?: "") }
    var dataTanggal by remember { mutableStateOf(tmpEvent?.tanggalPelaksanaan ?: "") }
    var dataTempat by remember { mutableStateOf(tmpEvent?.tempatPelaksanaan ?: "") }
    var dataBiaya by remember { mutableStateOf(tmpEvent?.estimasiBiaya ?: "") }
    var dataDivisi by remember { mutableStateOf(tmpEvent?.divisi ?: "") }
    var dataStatus by remember { mutableStateOf(tmpEvent?.status ?: "belum terlaksana") }

    // State untuk mengontrol dropdown terbuka atau tertutup
    var expandedDivisi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        OutlinedTextField(
            value = dataTitle,
            onValueChange = { dataTitle = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            label = { Text(text = "Nama Kegiatan") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
        )

        // Description
        OutlinedTextField(
            value = dataDescription,
            onValueChange = { dataDescription = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            label = { Text(text = "Deskripsi Kegiatan") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            maxLines = 5,
            minLines = 3
        )

        // Tanggal Pelaksanaan
        OutlinedTextField(
            value = dataTanggal,
            onValueChange = { dataTanggal = it },
            label = { Text(text = "Tanggal Pelaksanaan") },
            placeholder = { Text(text = "Contoh: 25 Desember 2026") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        // Tempat Pelaksanaan
        OutlinedTextField(
            value = dataTempat,
            onValueChange = { dataTempat = it },
            label = { Text(text = "Tempat Pelaksanaan") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        // Estimasi Biaya
        OutlinedTextField(
            value = dataBiaya,
            onValueChange = { dataBiaya = it },
            label = { Text(text = "Estimasi Biaya") },
            placeholder = { Text(text = "Contoh: 1500000") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
        )

        // Divisi (Diubah menggunakan Dropdown)
        ExposedDropdownMenuBox(
            expanded = expandedDivisi,
            onExpandedChange = { expandedDivisi = !expandedDivisi },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = dataDivisi,
                onValueChange = {}, // Kosong karena diisi melalui opsi
                readOnly = true,    // Hanya bisa dipilih
                label = { Text("Divisi Penanggung Jawab") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedDivisi,
                onDismissRequest = { expandedDivisi = false }
            ) {
                DivisiEnum.getAllFullNames().forEach { divisiName ->
                    DropdownMenuItem(
                        text = { Text(text = divisiName) },
                        onClick = {
                            dataDivisi = divisiName
                            expandedDivisi = false
                        }
                    )
                }
            }
        }

        // Pilihan Status
        Text(
            text = "Status Kegiatan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        // UBAH: Menggunakan Column agar pilihan berbaris ke bawah
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp) // Memberi jarak antar pilihan
        ) {
            val statusOptions = listOf("belum terlaksana", "sudah terlaksana", "dibatalkan")
            statusOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dataStatus = option }
                        .padding(vertical = 4.dp) // Memberi area klik yang lebih luas
                ) {
                    RadioButton(
                        selected = (dataStatus == option),
                        onClick = { dataStatus = option }
                    )
                    Text(
                        // Menggunakan replaceFirstChar untuk kapitalisasi (lebih modern dari capitalize())
                        text = option.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium, // Sedikit diperbesar agar mudah dibaca
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = {
                // Validasi singkat
                if (dataTitle.isBlank() || dataDescription.isBlank() || dataTanggal.isBlank() ||
                    dataTempat.isBlank() || dataBiaya.isBlank() || dataDivisi.isBlank()
                ) {
                    AlertHelper.show(alertState, AlertType.ERROR, "Semua data wajib diisi!")
                    return@FloatingActionButton
                }

                // Kirim semua data ke fungsi onSave
                onSave(
                    dataTitle,
                    dataDescription,
                    dataStatus,
                    dataTanggal,
                    dataTempat,
                    dataBiaya,
                    dataDivisi
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan Data Kegiatan")
        }
    }

    // Alert Dialog
    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = { AlertHelper.dismiss(alertState) },
            title = { Text(alertState.value.type.title) },
            text = { Text(alertState.value.message) },
            confirmButton = {
                TextButton(onClick = { AlertHelper.dismiss(alertState) }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewEventsAddUI() {
    // Preview
}