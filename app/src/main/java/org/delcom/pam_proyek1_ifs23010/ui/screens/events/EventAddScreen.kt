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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEvent by eventViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var tmpEvent by remember { mutableStateOf<ResponseEventData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        uiStateEvent.eventAdd = EventActionUIState.Loading
    }

    fun onSave(
        title: String,
        description: String,
        status: String,
        tanggalPelaksanaan: String,
        tempatPelaksanaan: String,
        estimasiBiaya: String,
        divisi: String
    ) {
        if (authToken.value == null) return
        isLoading = true

        tmpEvent = ResponseEventData(
            title = title, description = description, status = status,
            tanggalPelaksanaan = tanggalPelaksanaan, tempatPelaksanaan = tempatPelaksanaan,
            estimasiBiaya = estimasiBiaya, divisi = divisi
        )

        eventViewModel.postEvent(
            authToken = authToken.value!!, title = title, description = description,
            status = status, tanggalPelaksanaan = tanggalPelaksanaan,
            tempatPelaksanaan = tempatPelaksanaan, estimasiBiaya = estimasiBiaya, divisi = divisi
        )
    }

    LaunchedEffect(uiStateEvent.eventAdd) {
        when (val state = uiStateEvent.eventAdd) {
            is EventActionUIState.Success -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.SUCCESS, state.message)
                RouteHelper.to(navController, ConstHelper.RouteNames.Events.path, true)
                isLoading = false
            }
            is EventActionUIState.Error -> {
                SuspendHelper.showSnackBar(snackbarHost, SnackBarType.ERROR, state.message)
                isLoading = false
            }
            else -> {}
        }
    }

    if (isLoading) {
        LoadingUI()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBarComponent(
            navController = navController,
            title = "Tambah Kegiatan",
            showBackButton = true,
        )
        Box(modifier = Modifier.weight(1f)) {
            EventsAddUI(
                tmpEvent = tmpEvent,
                onSave = ::onSave
            )
        }
        BottomNavComponent(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsAddUI(
    tmpEvent: ResponseEventData?,
    onSave: (String, String, String, String, String, String, String) -> Unit
) {
    val alertState = remember { mutableStateOf(AlertState()) }

    var dataTitle by remember { mutableStateOf(tmpEvent?.title ?: "") }
    var dataDescription by remember { mutableStateOf(tmpEvent?.description ?: "") }
    var dataTanggal by remember { mutableStateOf(tmpEvent?.tanggalPelaksanaan ?: "") }
    var dataTempat by remember { mutableStateOf(tmpEvent?.tempatPelaksanaan ?: "") }
    var dataBiaya by remember { mutableStateOf(tmpEvent?.estimasiBiaya ?: "") }
    var dataDivisi by remember { mutableStateOf(tmpEvent?.divisi ?: "") }
    var dataStatus by remember { mutableStateOf(tmpEvent?.status ?: "belum terlaksana") }

    var expandedDivisi by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Form dibungkus dalam Card agar rapi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Informasi Kegiatan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Title
                OutlinedTextField(
                    value = dataTitle,
                    onValueChange = { dataTitle = it },
                    label = { Text("Nama Kegiatan") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Tanggal Pelaksanaan
                OutlinedTextField(
                    value = dataTanggal,
                    onValueChange = { dataTanggal = it },
                    label = { Text("Tanggal Pelaksanaan") },
                    placeholder = { Text("Contoh: 25 Desember 2026") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Tempat Pelaksanaan
                OutlinedTextField(
                    value = dataTempat,
                    onValueChange = { dataTempat = it },
                    label = { Text("Tempat Pelaksanaan") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // Estimasi Biaya
                OutlinedTextField(
                    value = dataBiaya,
                    onValueChange = { dataBiaya = it },
                    label = { Text("Estimasi Biaya (Rp)") },
                    placeholder = { Text("Contoh: 1500000") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                )

                // Divisi (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedDivisi,
                    onExpandedChange = { expandedDivisi = !expandedDivisi },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = dataDivisi,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Divisi Penanggung Jawab") },
                        leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDivisi) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
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

                // Description
                OutlinedTextField(
                    value = dataDescription,
                    onValueChange = { dataDescription = it },
                    label = { Text("Deskripsi Kegiatan") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5,
                    minLines = 3
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pilihan Status Kegiatan (Di dalam Card terpisah agar rapi)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Status Kegiatan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statusOptions = listOf("belum terlaksana", "sudah terlaksana", "dibatalkan")
                    statusOptions.forEach { option ->
                        val isSelected = dataStatus == option
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { dataStatus = option }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null // Di-handle oleh Row
                            )
                            Text(
                                text = option.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Simpan Besar di Bawah
        Button(
            onClick = {
                if (dataTitle.isBlank() || dataDescription.isBlank() || dataTanggal.isBlank() ||
                    dataTempat.isBlank() || dataBiaya.isBlank() || dataDivisi.isBlank()
                ) {
                    AlertHelper.show(alertState, AlertType.ERROR, "Semua data wajib diisi!")
                    return@Button
                }
                onSave(dataTitle, dataDescription, dataStatus, dataTanggal, dataTempat, dataBiaya, dataDivisi)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Simpan")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Kegiatan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(100.dp)) // Ruang untuk Bottom Nav
    }

    if (alertState.value.isVisible) {
        AlertDialog(
            onDismissRequest = { AlertHelper.dismiss(alertState) },
            title = { Text(alertState.value.type.title) },
            text = { Text(alertState.value.message) },
            confirmButton = {
                TextButton(onClick = { AlertHelper.dismiss(alertState) }) { Text("OK") }
            }
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewEventsAddUI() {
    // Preview
}