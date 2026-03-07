package org.delcom.pam_proyek1_ifs23010.ui.screens.events // Ubah package

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
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData // Ganti import Todo ke Event
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventActionUIState // Ganti import
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventUIState // Ganti import
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel // Ganti import

@Composable
fun EventsEditScreen(
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel,
    eventId: String
) {
    // Ambil data dari viewmodel
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEvent by eventViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    // Muat data
    var event by remember { mutableStateOf<ResponseEventData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        if(uiStateAuth.auth !is AuthUIState.Success){
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.Home.path,
                true
            )
            return@LaunchedEffect
        }

        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        uiStateEvent.event = EventUIState.Loading
        uiStateEvent.eventChange = EventActionUIState.Loading

        // Request data kegiatan by ID
        eventViewModel.getEventById(authToken.value!!, eventId)
    }

    // Picu ulang ketika data kegiatan berubah
    LaunchedEffect(uiStateEvent.event) {
        if (uiStateEvent.event !is EventUIState.Loading) {
            if (uiStateEvent.event is EventUIState.Success) {
                event = (uiStateEvent.event as EventUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
                isLoading = false
            }
        }
    }

    // Simpan perubahan data
    fun onSave(
        title: String,
        description: String,
        status: String,
        tanggalPelaksanaan: String,
        tempatPelaksanaan: String,
        estimasiBiaya: String,
        divisi: String
    ) {
        isLoading = true

        eventViewModel.putEvent(
            authToken = authToken.value!!,
            eventId = eventId,
            title = title,
            description = description,
            status = status,
            tanggalPelaksanaan = tanggalPelaksanaan,
            tempatPelaksanaan = tempatPelaksanaan,
            estimasiBiaya = estimasiBiaya,
            divisi = divisi
        )
    }

    LaunchedEffect(uiStateEvent.eventChange) {
        when (val state = uiStateEvent.eventChange) {
            is EventActionUIState.Success -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.SUCCESS,
                    message = state.message
                )
                RouteHelper.to(
                    navController = navController,
                    destination = ConstHelper.RouteNames.EventsDetail.path
                        .replace("{eventId}", eventId),
                    popUpTo = ConstHelper.RouteNames.EventsDetail.path
                        .replace("{eventId}", eventId),
                    removeBackStack = true
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
    if (isLoading || event == null) {
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
            title = "Ubah Data Kegiatan",
            showBackButton = true,
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            EventsEditUI(
                event = event!!,
                onSave = ::onSave
            )
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun EventsEditUI(
    event: ResponseEventData,
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

    var dataTitle by remember { mutableStateOf(event.title) }
    var dataDescription by remember { mutableStateOf(event.description) }
    var dataTanggal by remember { mutableStateOf(event.tanggalPelaksanaan) }
    var dataTempat by remember { mutableStateOf(event.tempatPelaksanaan) }
    var dataBiaya by remember { mutableStateOf(event.estimasiBiaya) }
    var dataDivisi by remember { mutableStateOf(event.divisi) }
    var dataStatus by remember { mutableStateOf(event.status) }

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
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
        )

        // Divisi
        OutlinedTextField(
            value = dataDivisi,
            onValueChange = { dataDivisi = it },
            label = { Text(text = "Divisi Penanggung Jawab") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        )

        // Pilihan Status
        Text(
            text = "Status Kegiatan",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val statusOptions = listOf("belum terlaksana", "sudah terlaksana", "dibatalkan")
            statusOptions.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { dataStatus = option }
                ) {
                    RadioButton(
                        selected = (dataStatus == option),
                        onClick = { dataStatus = option }
                    )
                    Text(
                        text = option.capitalize(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Floating Action Button
        FloatingActionButton(
            onClick = {
                if (dataTitle.isBlank() || dataDescription.isBlank() || dataTanggal.isBlank() ||
                    dataTempat.isBlank() || dataBiaya.isBlank() || dataDivisi.isBlank()
                ) {
                    AlertHelper.show(
                        alertState,
                        AlertType.ERROR,
                        "Semua data wajib diisi!"
                    )
                    return@FloatingActionButton
                }

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
                .align(Alignment.BottomEnd) // pojok kanan bawah
                .padding(16.dp) // jarak dari tepi
            ,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Simpan Data"
            )
        }
    }

    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = {
                AlertHelper.dismiss(alertState)
            },
            title = {
                Text(alertState.value.type.title)
            },
            text = {
                Text(alertState.value.message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        AlertHelper.dismiss(alertState)
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewEventsEditUI() {
}