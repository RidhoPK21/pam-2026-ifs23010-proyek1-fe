package org.delcom.pam_proyek1_ifs23010.ui.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.delcom.pam_proyek1_ifs23010.R
import org.delcom.pam_proyek1_ifs23010.helper.ConstHelper
import org.delcom.pam_proyek1_ifs23010.helper.RouteHelper
import org.delcom.pam_proyek1_ifs23010.helper.ToolsHelper
import org.delcom.pam_proyek1_ifs23010.network.events.data.DivisiEnum
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarMenuItem
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventsUIState
import java.util.UUID

@Composable
fun EventsScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    eventViewModel: EventViewModel
) {
    val uiStateAuth by authViewModel.uiState.collectAsState()
    val uiStateEvent by eventViewModel.uiState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    // State untuk Filter Status, Divisi, dan Scroll
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedDivisi by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    var events by remember { mutableStateOf<List<ResponseEventData>>(emptyList()) }
    var authToken by remember { mutableStateOf<String?>(null) }

    fun fetchEventsData() {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Success) {
            isLoading = true
            authToken = authState.data.authToken
            eventViewModel.resetAndGetAllEvents(authToken ?: "", searchQuery.text, selectedStatus, selectedDivisi)
        }
    }

    LaunchedEffect(uiStateAuth.auth) {
        val authState = uiStateAuth.auth
        if (authState is AuthUIState.Success) {
            fetchEventsData()
        } else if (authState is AuthUIState.Error) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= (totalItems - 2) && totalItems > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiStateEvent.events !is EventsUIState.Loading) {
            eventViewModel.getAllEvents(authToken ?: "", searchQuery.text, selectedStatus, selectedDivisi)
        }
    }

    LaunchedEffect(uiStateEvent.events) {
        if (uiStateEvent.events !is EventsUIState.Loading) {
            isLoading = false
            events = if (uiStateEvent.events is EventsUIState.Success) {
                (uiStateEvent.events as EventsUIState.Success).data
            } else {
                emptyList()
            }
        }
    }

    fun onLogout(token: String){
        isLoading = true
        authViewModel.logout(token)
    }

    LaunchedEffect(uiStateAuth.authLogout) {
        if (uiStateAuth.authLogout !is AuthLogoutUIState.Loading) {
            RouteHelper.to(navController, ConstHelper.RouteNames.AuthLogin.path, true)
        }
    }

    if (isLoading && events.isEmpty()) {
        LoadingUI()
        return
    }

    val menuItems = listOf(
        TopAppBarMenuItem(text = "Profile", icon = Icons.Filled.Person, route = ConstHelper.RouteNames.Profile.path),
        TopAppBarMenuItem(text = "Logout", icon = Icons.AutoMirrored.Filled.Logout, route = null, onClick = { onLogout(authToken ?: "") })
    )

    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        TopAppBarComponent(
            navController = navController,
            title = "Kegiatan Himpunan",
            showBackButton = false,
            customMenuItems = menuItems,
            withSearch = true,
            searchQuery = searchQuery,
            onSearchQueryChange = { query -> searchQuery = query },
            onSearchAction = { fetchEventsData() }
        )

        // Filter Area
        Column(modifier = Modifier.fillMaxWidth()) {
            // Filter Status
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    null to "Semua Status",
                    "belum terlaksana" to "Belum",
                    "sudah terlaksana" to "Selesai",
                    "dibatalkan" to "Batal"
                )
                items(statuses) { (key, label) ->
                    FilterChip(
                        selected = selectedStatus == key,
                        onClick = { selectedStatus = key; fetchEventsData() },
                        label = { Text(label) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            // Filter Divisi
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedDivisi == null,
                        onClick = { selectedDivisi = null; fetchEventsData() },
                        label = { Text("Semua Divisi") },
                        shape = RoundedCornerShape(50)
                    )
                }
                items(DivisiEnum.entries) { divisi ->
                    FilterChip(
                        selected = selectedDivisi == divisi.fullName,
                        onClick = { selectedDivisi = divisi.fullName; fetchEventsData() },
                        label = { Text(divisi.shortName) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            EventsUI(events = events, onOpen = { id -> RouteHelper.to(navController, "events/$id") }, listState = listState)

            // FAB (Floating Action Button)
            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.EventsAdd.path) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp), // Bentuk modern (squircle)
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Kegiatan")
                }
            }
        }
        BottomNavComponent(navController = navController)
    }
}

@Composable
fun EventsUI(
    events: List<ResponseEventData>,
    onOpen: (String) -> Unit,
    listState: LazyListState
) {
    if (events.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Inbox,
                contentDescription = "Kosong",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Belum ada kegiatan ditemukan.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp), // Ruang bawah untuk FAB
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                items = events,
                key = { it.id ?: UUID.randomUUID().toString() }
            ) { event ->
                EventItemUI(event, onOpen)
            }
        }
    }
}

@Composable
fun EventItemUI(
    event: ResponseEventData,
    onOpen: (String) -> Unit
) {
    val safeId = event.id ?: ""
    val safeTitle = event.title ?: "Tanpa Judul"
    val safeDivisi = event.divisi ?: "-"
    val safeTanggal = event.tanggalPelaksanaan ?: "-"
    val safeUpdatedAt = event.updatedAt ?: "0"
    val safeStatus = event.status ?: "belum terlaksana"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onOpen(safeId) },
        shape = RoundedCornerShape(16.dp), // Lebih melengkung
        elevation = CardDefaults.cardElevation(2.dp), // Bayangan lebih halus
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding dalam diperbesar
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Gambar Cover
            AsyncImage(
                model = ToolsHelper.getEventImage(safeId, safeUpdatedAt),
                contentDescription = safeTitle,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(80.dp) // Sedikit lebih besar
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant), // Background jika gambar gagal muat
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Info Teks
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = safeTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = safeDivisi,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = safeTanggal,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                val statusColor = when (safeStatus.lowercase()) {
                    "sudah terlaksana" -> MaterialTheme.colorScheme.secondaryContainer
                    "dibatalkan" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }

                val statusTextColor = when (safeStatus.lowercase()) {
                    "sudah terlaksana" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "dibatalkan" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(statusColor)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = safeStatus.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun PreviewEventsUI() {
    // Preview
}