package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GroceryList
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.SoftBlack
import com.example.ui.theme.GrayAccent
import com.example.ui.theme.LightYellow
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SunYellow
import com.example.ui.theme.WarmPaper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.zIndex

@Composable
fun ListSelectionScreen(
    viewModel: GroceryViewModel,
    onListSelected: (String) -> Unit
) {
    val lists by viewModel.allLists.collectAsState()
    var showCreateDialog by varShowCreateDialog()
    var showJoinDialog by varShowJoinDialog()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SunYellow)
                    .padding(horizontal = 24.dp)
                    .padding(top = 36.dp, bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "COLLECTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = DeepBlack.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Grocery\nNotebooks",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 44.sp,
                            color = DeepBlack,
                            letterSpacing = (-1.5).sp
                        )
                    }

                    // Layered overlapping active member avatars from the editorial design
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DeepBlack, CircleShape)
                                .border(2.dp, SunYellow, CircleShape)
                                .zIndex(3f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("JD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunYellow)
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = (-8).dp)
                                .size(36.dp)
                                .background(SoftBlack, CircleShape)
                                .border(2.dp, SunYellow, CircleShape)
                                .zIndex(2f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunYellow)
                        }
                        Box(
                            modifier = Modifier
                                .offset(x = (-16).dp)
                                .size(36.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, SunYellow, CircleShape)
                                .zIndex(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("＋", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlack)
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Join shared live list
                FloatingActionButton(
                    onClick = { showJoinDialog = true },
                    containerColor = DeepBlack,
                    contentColor = SunYellow,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("join_live_room_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Join Shared List", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Link Live Code", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }

                // Add list
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = DeepBlack,
                    contentColor = SunYellow,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("create_list_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add List", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Notebook", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SunYellow)
        ) {
            if (lists.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🌕",
                        fontSize = 72.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Your grocery galaxy is empty 🌕",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Add your first item or a clean new notebook to begin shopping.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = DeepBlack.copy(alpha = 0.7f),
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "ACTIVE NOTEBOOKS",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
                            color = DeepBlack.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    items(lists) { groceryList ->
                        ListCardItem(
                            groceryList = groceryList,
                            onClick = { onListSelected(groceryList.id) },
                            onDelete = { viewModel.deleteList(groceryList.id) },
                            onRename = { newName -> viewModel.renameList(groceryList.id, newName) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Padding for FABs
                    }
                }
            }
        }
    }

    // --- Create List Dialog ---
    if (showCreateDialog) {
        var newListName by remember { mutableStateOf("") }
        var optionalSyncCode by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            viewModel.createList(
                                name = newListName.trim(),
                                syncCode = if (optionalSyncCode.isNotBlank()) optionalSyncCode.trim() else null
                            )
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlack, contentColor = SunYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            title = { Text("New Shopping Notebook 📝", fontWeight = FontWeight.Bold, color = DeepBlack) },
            text = {
                Column {
                    Text("Give your laundry, weekly basket, or party list a sunlit home name.", fontSize = 14.sp, color = DeepBlack.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        label = { Text("Notebook Name (e.g. Weekly Food)") },
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("new_list_name_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = optionalSyncCode,
                        onValueChange = { optionalSyncCode = it },
                        label = { Text("Room Sync Code (Optional, max 8 chars)") },
                        singleLine = true,
                        placeholder = { Text("e.g. SUNDAY") },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("optional_sync_code_input")
                    )
                }
            },
            containerColor = WarmPaper,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("create_list_dialog")
        )
    }

    // --- Link/Join Shared Code Dialog ---
    if (showJoinDialog) {
        var roomCodeToJoin by remember { mutableStateOf("") }
        var newListNameForJoin by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (roomCodeToJoin.isNotBlank() && newListNameForJoin.isNotBlank()) {
                            viewModel.createList(name = newListNameForJoin.trim(), syncCode = roomCodeToJoin.trim().uppercase())
                            showJoinDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlack, contentColor = SunYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Connect & Sync", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showJoinDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            title = { Text("Link via Shared Code ⚡", fontWeight = FontWeight.Bold, color = DeepBlack) },
            text = {
                Column {
                    Text("Connect instantly to a family member's current shopping notebook by putting the identical room code.", fontSize = 14.sp, color = DeepBlack.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = roomCodeToJoin,
                        onValueChange = { roomCodeToJoin = it },
                        label = { Text("6-Digit Shared Room Code") },
                        singleLine = true,
                        placeholder = { Text("e.g. HOMECART") },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("join_room_code_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newListNameForJoin,
                        onValueChange = { newListNameForJoin = it },
                        label = { Text("Save Locally as...") },
                        singleLine = true,
                        placeholder = { Text("e.g. Shared Shopping") },
                        colors = outlinedFieldColors(),
                        modifier = Modifier.fillMaxWidth().testTag("join_room_local_name_input")
                    )
                }
            },
            containerColor = WarmPaper,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("join_list_dialog")
        )
    }
}

@Composable
fun ListCardItem(
    groceryList: GroceryList,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameValue by remember { mutableStateOf(groceryList.name) }
    val formattedDate = remember {
        val sdf = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
        sdf.format(Date(groceryList.createdAt))
    }

    // Playful rotation to resemble slightly dynamic tilted paper notes!
    // Alternate tilt slightly based on ID hashCode to look organized but organic.
    val tiltAngle = remember {
        (groceryList.id.hashCode() % 3).toFloat() - 1f // Tilted between -1 to +1 degrees
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .rotate(tiltAngle)
            .clickable { onClick() }
            .testTag("list_card_${groceryList.id}"),
        colors = CardDefaults.cardColors(containerColor = WarmPaper),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(3.dp, DeepBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat bold aesthetic
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = groceryList.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (groceryList.syncCode != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                        .background(DeepBlack, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CloudSync,
                                        contentDescription = "Synced",
                                        tint = SunYellow,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = groceryList.syncCode,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = SunYellow
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notebook created $formattedDate",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepBlack.copy(alpha = 0.5f)
                    )
                }

                Row {
                    IconButton(
                        onClick = { showRenameDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = DeepBlack,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("delete_list_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SignalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalMall,
                        contentDescription = "Items count",
                        tint = DeepBlack,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tap to open and manage products",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack
                    )
                }

                // Clean right indicator
                Text("→", fontSize = 20.sp, fontWeight = FontWeight.Black, color = DeepBlack)
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameValue.isNotBlank()) {
                            onRename(renameValue.trim())
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlack, contentColor = SunYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRenameDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack)
                ) {
                    Text("Close")
                }
            },
            title = { Text("Rename List ✏️", fontWeight = FontWeight.Bold, color = DeepBlack) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    colors = outlinedFieldColors(),
                    modifier = Modifier.fillMaxWidth().testTag("rename_list_input")
                )
            },
            containerColor = WarmPaper,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Helpers for cleaner inputs
@Composable
fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DeepBlack,
    unfocusedBorderColor = DeepBlack.copy(alpha = 0.5f),
    focusedLabelColor = DeepBlack,
    unfocusedLabelColor = DeepBlack.copy(alpha = 0.6f),
    focusedTextColor = DeepBlack,
    unfocusedTextColor = DeepBlack
)

@Composable
fun varShowCreateDialog() = remember { mutableStateOf(false) }

@Composable
fun varShowJoinDialog() = remember { mutableStateOf(false) }
