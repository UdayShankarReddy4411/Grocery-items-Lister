package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GroceryItem
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListItemsScreen(
    viewModel: GroceryViewModel,
    onBack: () -> Unit
) {
    val selectedList by viewModel.selectedList.collectAsState()
    val items by viewModel.currentItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val simulationActive by viewModel.simulationActive.collectAsState()
    val collabNotification by viewModel.collabNotification.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showShareCodeDialog by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current

    val categories = listOf("All", "Vegetables", "Dairy", "Snacks", "Beverages", "Household", "Other")

    Scaffold(
        modifier = Modifier.fillMaxSize().background(SunYellow),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SunYellow)
                    .padding(horizontal = 16.dp)
                    .padding(top = 36.dp, bottom = 12.dp)
            ) {
                // Editorial Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .background(DeepBlack, CircleShape)
                            .testTag("back_to_lists_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SunYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT LIST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = DeepBlack.copy(alpha = 0.6f)
                        )
                        Text(
                            text = selectedList?.name ?: "Grocery List",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = DeepBlack,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 32.sp
                        )
                    }

                    // Cloud Sync Indicator / Action
                    IconButton(
                        onClick = { showShareCodeDialog = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        when (syncStatus) {
                            SyncStatus.SYNCING -> {
                                CircularProgressIndicator(
                                    color = DeepBlack,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            SyncStatus.SUCCESS -> {
                                Icon(
                                    Icons.Default.CloudQueue,
                                    contentDescription = "Synced successfully",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            SyncStatus.ERROR -> {
                                Icon(
                                    Icons.Default.SyncProblem,
                                    contentDescription = "Sync Error",
                                    tint = SignalRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            SyncStatus.IDLE -> {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Sync Settings",
                                    tint = DeepBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = DeepBlack,
                contentColor = SunYellow,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_item_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Grocery Item")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Item", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SunYellow)
        ) {
            // Toast Collaboration Banner
            AnimatedVisibility(
                visible = collabNotification != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                collabNotification?.let { notif ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DeepBlack)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📡 " + notif.message,
                            color = SunYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SunYellow, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Progress Bento Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val totalCount = items.size
                val boughtCount = items.count { it.isAddedToCart }
                val progressFraction = if (totalCount > 0) boughtCount.toFloat() / totalCount else 0f

                // Left card: Sync & items overview (Weight 3f)
                Card(
                    modifier = Modifier
                        .weight(3f)
                        .height(110.dp)
                        .testTag("progress_bento_left"),
                    colors = CardDefaults.cardColors(containerColor = DeepBlack),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedList?.syncCode != null) Icons.Default.CloudSync else Icons.Default.CloudOff,
                                contentDescription = "Sync state indicator",
                                tint = SunYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (selectedList?.syncCode != null) "SYNCED" else "LOCAL",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = SunYellow
                            )
                        }

                        Column {
                            Text(
                                text = "$totalCount Items",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = SunYellow,
                                fontStyle = FontStyle.Italic
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Small progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(SunYellow.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressFraction)
                                        .fillMaxHeight()
                                        .background(SunYellow, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                // Right card: In Cart status count (Weight 2f)
                Card(
                    modifier = Modifier
                        .weight(2f)
                        .height(110.dp)
                        .testTag("progress_bento_right"),
                    colors = CardDefaults.cardColors(containerColor = WarmPaper.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, DeepBlack)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "IN CART",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepBlack.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d", boughtCount),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepBlack,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 38.sp
                        )
                    }
                }
            }

            // Live Collaborative Playroom Setup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = WarmPaper),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.5.dp, DeepBlack)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👥", fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
                            Column {
                                Text(
                                    text = "Collab Playroom Demo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepBlack
                                )
                                Text(
                                    text = "Simulates instant shared updates",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DeepBlack.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleSimulation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (simulationActive) SignalRed else DeepBlack,
                                contentColor = SunYellow
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(34.dp).testTag("simulation_toggle_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (simulationActive) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (simulationActive) "Stop Demo" else "Start Demo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (simulationActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = DeepBlack.copy(0.15f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Connected Shoppers: Me (Buyer) • Emily (At Home) • Dad (Aisle 4)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkBlue,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            // Search Bar & Filter options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search list items...", color = DeepBlack.copy(0.5f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepBlack) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = DeepBlack)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WarmPaper,
                        unfocusedContainerColor = WarmPaper,
                        focusedBorderColor = DeepBlack,
                        unfocusedBorderColor = DeepBlack,
                        focusedTextColor = DeepBlack,
                        unfocusedTextColor = DeepBlack,
                        cursorColor = DeepBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().testTag("search_grocery_items_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sorting Selector & Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val boughtCount = items.count { it.isAddedToCart }
                    val totalCount = items.size
                    Text(
                        text = "CART STATUS: $boughtCount / $totalCount ITEMS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = DeepBlack.copy(0.7f)
                    )

                    // Simple clean sorting toggle button
                    TextButton(
                        onClick = {
                            if (sortBy == SortBy.TIME_ADDED) {
                                viewModel.updateSortOrder(SortBy.ALPHABETICAL)
                            } else {
                                viewModel.updateSortOrder(SortBy.TIME_ADDED)
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (sortBy == SortBy.TIME_ADDED) Icons.Default.Schedule else Icons.Default.SortByAlpha,
                                contentDescription = "Sort Type",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (sortBy == SortBy.TIME_ADDED) "Added Time ↓" else "A to Z",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Category badges row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) DeepBlack else WarmPaper,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { viewModel.updateCategoryFilter(category) }
                                .border(
                                    width = 2.dp,
                                    color = DeepBlack,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) SunYellow else DeepBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Scrollable list items
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your notebook is clear! 🌕",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No items match your search." else "Add products like Milk, Rice, Strawberries with notes!",
                        fontSize = 13.sp,
                        color = DeepBlack.copy(0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        GroceryItemCard(
                            item = item,
                            onToggleCart = { isAdded ->
                                viewModel.toggleItemCart(item.id, isAdded)
                            },
                            onDelete = {
                                viewModel.deleteItem(item.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                    }
                }
            }
        }
    }

    // --- Add Grocery Item Dialog ---
    if (showAddItemDialog) {
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("pcs") }
        var category by remember { mutableStateOf("Other") }
        var notes by remember { mutableStateOf("") }

        val units = listOf("pcs", "packets", "kg", "grams", "litres", "eggs", "bottles")

        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val quantityText = if (quantity.isNotBlank()) "$quantity $unit" else "1 $unit"
                            viewModel.addItem(name.trim(), quantityText, category, notes.trim())
                            showAddItemDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepBlack, contentColor = SunYellow),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add to List", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddItemDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            title = { Text("Write Grocery Note 🍏", fontWeight = FontWeight.Black, color = DeepBlack) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Product Name") },
                            singleLine = true,
                            placeholder = { Text("e.g. Fresh Bananas") },
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_item_name_input")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("*Required field", fontSize = 10.sp, color = SignalRed)
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = quantity,
                                onValueChange = { quantity = it },
                                label = { Text("Quantity") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                placeholder = { Text("e.g. 5") },
                                colors = outlinedFieldColors(),
                                modifier = Modifier.weight(1f).testTag("add_item_quantity_input")
                            )

                            // Unit Dropdown replacement using simple raw select
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unit", fontSize = 12.sp, color = DeepBlack.copy(0.7f), fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    units.forEach { u ->
                                        val uSelected = unit == u
                                        Box(
                                            modifier = Modifier
                                                .background(if (uSelected) DeepBlack else GrayAccent, RoundedCornerShape(4.dp))
                                                .clickable { unit = u }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(u, color = if (uSelected) SunYellow else DeepBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Pick Product Tag/Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepBlack)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.filter { it != "All" }.forEach { cat ->
                                val catSelected = category == cat
                                Box(
                                    modifier = Modifier
                                        .background(if (catSelected) DeepBlack else WarmPaper, RoundedCornerShape(12.dp))
                                        .clickable { category = cat }
                                        .border(1.5.dp, DeepBlack, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(cat, color = if (catSelected) SunYellow else DeepBlack, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Special Notes") },
                            placeholder = { Text("e.g. Low-fat, ripe only or brand details") },
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_item_notes_input")
                        )
                    }
                }
            },
            containerColor = WarmPaper,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 8.dp,
            modifier = Modifier.testTag("add_item_dialog")
        )
    }

    // --- Share / Live Sync Code Configuration Dialog ---
    if (showShareCodeDialog) {
        var syncCodeValue by remember { mutableStateOf("") }
        val currentCode = selectedList?.syncCode

        AlertDialog(
            onDismissRequest = { showShareCodeDialog = false },
            confirmButton = {
                if (currentCode == null) {
                    Button(
                        onClick = {
                            if (syncCodeValue.isNotBlank()) {
                                selectedList?.let { viewModel.joinListSyncRoom(it.id, syncCodeValue.trim()) }
                                showShareCodeDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepBlack, contentColor = SunYellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Connect Cloud", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            selectedList?.let { viewModel.leaveSyncRoom(it.id) }
                            showShareCodeDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SignalRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Deactivate Live Sync")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showShareCodeDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepBlack)
                ) {
                    Text("Close")
                }
            },
            title = { Text(if (currentCode != null) "Active Cloud Connection 📡" else "Go Live & Collaborative! 📡🛒", fontWeight = FontWeight.Black, color = DeepBlack) },
            text = {
                Column {
                    if (currentCode != null) {
                        Text("This shopping list is linked online in real-time under code: $currentCode", fontSize = 14.sp, color = DeepBlack, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Family members can join using this code to collaborate. Updates will sync instantly!", fontSize = 12.sp, color = DeepBlack.copy(0.7f))
                    } else {
                        Text("Publish this list to the cloud instantly. Share this 6-letter room code with family members to shop together in real-time on any device!", fontSize = 13.sp, color = DeepBlack.copy(0.8f))
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = syncCodeValue,
                            onValueChange = { syncCodeValue = it },
                            label = { Text("Choose Sync Code (e.g. PAPAYA)") },
                            singleLine = true,
                            colors = outlinedFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("add_sync_room_code_input")
                        )
                    }
                }
            },
            containerColor = WarmPaper,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun GroceryItemCard(
    item: GroceryItem,
    onToggleCart: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    // Elegant check states
    val isChecked = item.isAddedToCart
    val textColor = if (isChecked) DeepBlack.copy(alpha = 0.4f) else DeepBlack
    val lineDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("item_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = if (isChecked) WarmPaper.copy(0.6f) else WarmPaper),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isChecked) 1.5.dp else 2.5.dp, if (isChecked) DeepBlack.copy(0.3f) else DeepBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Area with 48dp target boundary
            IconButton(
                onClick = { onToggleCart(!isChecked) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("item_checkbox_${item.id}")
            ) {
                Icon(
                    imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isChecked) "Added to Cart" else "Mark pending",
                    tint = if (isChecked) SuccessGreen else DeepBlack,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textDecoration = lineDecoration,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Quantity badge
                    Box(
                        modifier = Modifier
                            .background(if (isChecked) GrayAccent.copy(0.4f) else SunYellow, RoundedCornerShape(6.dp))
                            .border(1.5.dp, if (isChecked) DeepBlack.copy(0.3f) else DeepBlack, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.quantity,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isChecked) DeepBlack.copy(0.4f) else DeepBlack
                        )
                    }
                }

                if (item.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "✍️ " + item.notes,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = if (isChecked) InkBlue.copy(0.4f) else InkBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent)
                            .border(1.dp, if (isChecked) DeepBlack.copy(0.2f) else DeepBlack.copy(0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) DeepBlack.copy(0.4f) else DeepBlack.copy(0.6f)
                        )
                    }

                    // Collab contributor trace
                    if (item.updatedBy != "Me" && item.updatedBy != "System") {
                        Text(
                            text = "by ${item.updatedBy}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) SuccessGreen.copy(0.4f) else SuccessGreen,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trail delete target
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("delete_item_button_${item.id}")
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete item",
                    tint = if (isChecked) SignalRed.copy(0.4f) else SignalRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
