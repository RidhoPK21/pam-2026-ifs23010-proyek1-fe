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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
import org.delcom.pam_proyek1_ifs23010.network.events.data.ResponseEventData // Ganti Import
import org.delcom.pam_proyek1_ifs23010.ui.components.BottomNavComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.LoadingUI
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarComponent
import org.delcom.pam_proyek1_ifs23010.ui.components.TopAppBarMenuItem
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthLogoutUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthUIState
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.AuthViewModel
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventViewModel // Ganti Import
import org.delcom.pam_proyek1_ifs23010.ui.viewmodels.EventsUIState // Ganti Import

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
        isLoading = true
        authToken = (uiStateAuth.auth as AuthUIState.Success).data.authToken
        eventViewModel.resetAndGetAllEvents(authToken ?: "", searchQuery.text, selectedStatus, selectedDivisi)
    }

    LaunchedEffect(Unit) {
        if (uiStateAuth.auth !is AuthUIState.Success) {
            RouteHelper.to(navController, ConstHelper.RouteNames.Home.path, true)
            return@LaunchedEffect
        }
        fetchEventsData()
    }

    // Logika Pagination (Infinite Scroll)
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

        // Barisan Filter Chips
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            // Row 1: Filter Status
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf(
                    null to "Semua",
                    "belum terlaksana" to "Belum",
                    "sudah terlaksana" to "Selesai",
                    "dibatalkan" to "Batal"
                )
                statuses.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedStatus == key,
                        onClick = { selectedStatus = key; fetchEventsData() },
                        label = { Text(label) }
                    )
                }
            }

            // Row 2: Filter Divisi (Contoh beberapa divisi)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val divisiOptions = listOf(null to "Semua", "Humas" to "Humas", "BPH" to "BPH", "Pendidikan" to "Pendidikan")
                divisiOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedDivisi == key,
                        onClick = { selectedDivisi = key; fetchEventsData() },
                        label = { Text(label) }
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            EventsUI(events = events, onOpen = { id -> RouteHelper.to(navController, "events/$id") }, listState = listState)

            Box(modifier = Modifier.fillMaxSize()) {
                FloatingActionButton(
                    onClick = { RouteHelper.to(navController, ConstHelper.RouteNames.EventsAdd.path) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
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
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(events) { event ->
            EventItemUI(event, onOpen)
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Tidak ada kegiatan!", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun EventItemUI(
    event: ResponseEventData,
    onOpen: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onOpen(event.id) },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = ToolsHelper.getTodoImage(event.id, event.updatedAt), // Asumsi helper tidak berubah nama
                contentDescription = event.title,
                placeholder = painterResource(R.drawable.img_placeholder),
                error = painterResource(R.drawable.img_placeholder),
                modifier = Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // ROW UNTUK JUDUL DAN DIVISI
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Judul Kegiatan
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Label Divisi
                    Text(
                        text = event.divisi,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.tanggalPelaksanaan,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
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