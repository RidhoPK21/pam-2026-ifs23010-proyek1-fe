package org.delcom.pam_proyek1_ifs23010.ui.screens.events // Ubah nama package

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_proyek1_ifs23010.R
import org.delcom.pam_proyek1_ifs23010.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23010.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23010.helper.SuspendHelper
import org.delcom.pam_proyek1_ifs23010.helper.SuspendHelper.SnackBarType
import org.delcom.pam_proyek1_ifs23010.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23010.helper.ToolsHelper.uriToMultipart
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData // Ganti import Todo ke Event
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomDialog
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomDialogType
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarMenuItem
import org.delcom.pam_proyek1_ifs23010.ui.theme.DelcomTheme
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventActionUIState // Ganti import
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventUIState // Ganti import
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel // Ganti import
//import kotlinx.datetime.Clock // Perbaiki import kotlinx.datetime

@Composable
fun EventsDetailScreen( // Ganti nama fungsi
    navController: NavHostController,
    snackbarHost: SnackbarHostState,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel, // Ganti viewmodel
    eventId: String // Ganti argumen
) {
    // Ambil data dari viewmodel
    val uiStateEvent by eventViewModel.uiState.collectAsState()
    val uiStateAuth by authViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var isConfirmDelete by remember { mutableStateOf(false) }

    // Muat data
    var event by remember { mutableStateOf<ResponseEventData?>(null) }
    val authToken = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true

        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(
                navController,
                ConstHelper.RouteNames.Home.path,
                true
            )
            return@LaunchedEffect
        }

        authToken.value = (uiStateAuth.auth as AuthUIState.Success).data.authToken

        // Reset status action
        uiStateEvent.eventDelete = EventActionUIState.Loading
        uiStateEvent.eventChangeCover = EventActionUIState.Loading
        uiStateEvent.event = EventUIState.Loading

        eventViewModel.getEventById(authToken.value!!, eventId)
    }

    // Picu ulang ketika data berubah
    LaunchedEffect(uiStateEvent.event) {
        if (uiStateEvent.event !is EventUIState.Loading) {
            if (uiStateEvent.event is EventUIState.Success) {
                event = (uiStateEvent.event as EventUIState.Success).data
                isLoading = false
            } else {
                RouteHelper.back(navController)
            }
        }
    }

    fun onDelete() {
        if (authToken.value == null) {
            return
        }

        uiStateEvent.eventDelete = EventActionUIState.Loading

        isLoading = true
        eventViewModel.deleteEvent(authToken.value!!, eventId)
    }

    LaunchedEffect(uiStateEvent.eventDelete) {
        when (val state = uiStateEvent.eventDelete) {
            is EventActionUIState.Success -> {
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.SUCCESS,
                    message = state.message
                )
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.Events.path, // Kembali ke list events
                    true
                )
                uiStateEvent.event = EventUIState.Loading
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

    fun onChangeCover(
        context: Context,
        file: Uri
    ) {
        if (authToken.value == null) {
            return
        }

        uiStateEvent.eventChangeCover = EventActionUIState.Loading
        isLoading = true

        val filePart = uriToMultipart(context, file, "file")

        eventViewModel.putEventCover(
            authToken = authToken.value!!,
            eventId = eventId,
            file = filePart
        )
    }

    LaunchedEffect(uiStateEvent.eventChangeCover) {
        when (val state = uiStateEvent.eventChangeCover) {
            is EventActionUIState.Success -> {
                if(event != null){
                    event!!.updatedAt = System.currentTimeMillis().toString()
                }
                SuspendHelper.showSnackBar(
                    snackbarHost = snackbarHost,
                    type = SnackBarType.SUCCESS,
                    message = state.message
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

    // Menu item details
    val detailMenuItems = listOf(
        TopAppBarMenuItem(
            text = "Ubah Data",
            icon = Icons.Filled.Edit,
            route = null,
            onClick = {
                RouteHelper.to(
                    navController,
                    ConstHelper.RouteNames.EventsEdit.path
                        .replace("{eventId}", event!!.id),
                )
            }
        ),
        TopAppBarMenuItem(
            text = "Hapus Data",
            icon = Icons.Filled.Delete,
            route = null,
            onClick = {
                isConfirmDelete = true
            }
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    )
    {
        // Top App Bar
        TopAppBarComponent(
            navController = navController,
            title = "Detail Kegiatan",
            showBackButton = true,
            customMenuItems = detailMenuItems
        )
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            // Content UI
            EventsDetailUI(
                event = event!!,
                onChangeCover = ::onChangeCover,
            )
            // Bottom Dialog to Confirmation Delete
            BottomDialog(
                type = BottomDialogType.ERROR,
                show = isConfirmDelete,
                onDismiss = { isConfirmDelete = false },
                title = "Konfirmasi Hapus Data",
                message = "Apakah Anda yakin ingin menghapus kegiatan ini?",
                confirmText = "Ya, Hapus",
                onConfirm = {
                    onDelete()
                },
                cancelText = "Batal",
                destructiveAction = true
            )
        }
        // Bottom Nav
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun EventsDetailUI(
    event: ResponseEventData,
    onChangeCover: (context: Context, file: Uri) -> Unit,
) {
    var dataFile by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        dataFile = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    )
    {
        // Gambar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cover Image
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            imagePicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = dataFile ?: ToolsHelper.getTodoImage(event.id, event.updatedAt), // Asumsi helper getTodoImage belum diganti namanya
                        contentDescription = "Cover Kegiatan",
                        placeholder = painterResource(R.drawable.img_placeholder),
                        error = painterResource(R.drawable.img_placeholder),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Text di bawah gambar
                Text(
                    text = if (dataFile == null)
                        "Sentuh cover untuk mengubah"
                    else
                        "Gambar baru dipilih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tombol Simpan muncul jika ada gambar baru
                if (dataFile != null) {
                    Button(
                        onClick = {
                            onChangeCover(context, dataFile!!)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(0.7f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Simpan",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Label Status
                val statusColor = when (event.status.lowercase()) {
                    "sudah terlaksana" -> MaterialTheme.colorScheme.secondaryContainer
                    "dibatalkan" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }

                val statusTextColor = when (event.status.lowercase()) {
                    "sudah terlaksana" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "dibatalkan" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = event.status.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = statusTextColor
                    )
                }
            }
        }

        // Card Informasi Detail Kegiatan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Tanggal
                Text(
                    text = "Tanggal Pelaksanaan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.tanggalPelaksanaan.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Tempat
                Text(
                    text = "Tempat Pelaksanaan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.tempatPelaksanaan.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Biaya
                Text(
                    text = "Estimasi Biaya",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Rp ${event.estimasiBiaya.ifEmpty { "0" }}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Divisi
                Text(
                    text = "Divisi Penanggung Jawab",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.divisi.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Deskripsi
                Text(
                    text = "Deskripsi Kegiatan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewEventsDetailUI() {
    DelcomTheme {
    }
}