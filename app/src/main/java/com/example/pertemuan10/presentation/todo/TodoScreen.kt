package com.example.pertemuan10.presentation.todo

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pertemuan10.data.UserData
import com.example.pertemuan10.data.model.Priority
import com.example.pertemuan10.data.model.Todo

// --- CUTIE & CALM PALETTE ---
val BgDreamy = Color(0xFFFDF8FF)      // Krem keunguan sangat pucat
val SurfaceMilk = Color(0xFFFFFFFF)
val BorderCandy = Color(0xFFF0E5F5)    // Border ungu sangat muda
val TextDeep = Color(0xFF4A4E69)      // Biru tua keunguan (lebih tenang dari hitam)
val TextSoft = Color(0xFF9A8C98)      // Abu-abu keunguan

// Priority Candy Colors
val HighPastel = Color(0xFFFFB7B2)    // Salmon soft
val MidPastel = Color(0xFFFFDAC1)     // Peach soft
val LowPastel = Color(0xFFB5EAD7)     // Mint soft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    userData: UserData?,
    viewModel: TodoViewModel,
    onSignOut: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    var todoText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    val todos by viewModel.todos.collectAsState()

    Scaffold(
        containerColor = BgDreamy,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgDreamy),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AutoAwesome, "Sparkle", tint = HighPastel, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "To Do List",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = TextDeep,
                                letterSpacing = 0.sp
                            )
                        )
                    }
                },
                actions = {
                    userData?.let {
                        AsyncImage(
                            model = it.profilePictureUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSignOut() }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Dashboard "Cloud Card"
            PremiumDashboard(todos)

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ModernSearchBar(viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                QuickAddCard(
                    text = todoText,
                    onTextChange = { todoText = it },
                    currentPriority = selectedPriority,
                    onPriorityChange = { selectedPriority = it },
                    onAddClick = {
                        if (todoText.isNotBlank()) {
                            userData?.userId?.let { viewModel.add(it, todoText, selectedPriority.name) }
                            todoText = ""
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Ongoing Magic ✨",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextDeep,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    items(todos, key = { it.id }) { todo ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart) {
                                    userData?.userId?.let { uid -> viewModel.delete(uid, todo.id) }
                                    true
                                } else false
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = { SwipeDeleteBackground(dismissState) }
                        ) {
                            TaskItemPremium(todo, onNavigateToEdit) {
                                userData?.userId?.let { uid -> viewModel.toggle(uid, todo) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumDashboard(todos: List<Todo>) {
    val completed = todos.count { it.isCompleted }
    val total = todos.size
    val progress = if (total > 0) completed.toFloat() / total else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = ""
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        color = SurfaceMilk,
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(2.dp, BorderCandy)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(24.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Your Progress", color = TextSoft, style = MaterialTheme.typography.labelMedium)
                Text(
                    if (progress == 1f) "All Done! Yay~ 🌈" else "$completed/$total Tasks",
                    color = TextDeep,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                )
            }

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = Color(0xFFC5A3FF),
                    strokeWidth = 8.dp,
                    trackColor = BorderCandy,
                    modifier = Modifier.size(68.dp)
                )
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    color = TextDeep,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

@Composable
fun ModernSearchBar(viewModel: TodoViewModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceMilk,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderCandy)
    ) {
        TextField(
            value = viewModel.searchQuery.value,
            onValueChange = {
                viewModel.searchQuery.value = it
                viewModel.updateFilteredList()
            },
            placeholder = { Text("Find something fun...", color = TextSoft) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextSoft) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true
        )
    }
}

@Composable
fun QuickAddCard(
    text: String,
    onTextChange: (String) -> Unit,
    currentPriority: Priority,
    onPriorityChange: (Priority) -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = SurfaceMilk,
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.dp, BorderCandy)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("Add new task...", style = MaterialTheme.typography.bodyLarge) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = onAddClick,
                    enabled = text.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFFC5A3FF),
                        contentColor = Color.White,
                        disabledContainerColor = BorderCandy
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Rounded.Add, null)
                }
            }

            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Priority.entries.forEach { p ->
                    val isSelected = currentPriority == p
                    val pColor = when(p) {
                        Priority.HIGH -> HighPastel
                        Priority.MEDIUM -> MidPastel
                        Priority.LOW -> LowPastel
                    }
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { onPriorityChange(p) },
                        color = if (isSelected) pColor else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        border = if (isSelected) null else BorderStroke(1.dp, BorderCandy)
                    ) {
                        Text(
                            p.label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) Color.White else TextSoft,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemPremium(todo: Todo, onEdit: (String) -> Unit, onToggle: () -> Unit) {
    val pColor = when(Priority.fromString(todo.priority)) {
        Priority.HIGH -> HighPastel
        Priority.MEDIUM -> MidPastel
        Priority.LOW -> LowPastel
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(todo.id) },
        color = SurfaceMilk,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderCandy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Bubbly
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (todo.isCompleted) pColor else BorderCandy)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (todo.isCompleted) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp), tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                        color = if (todo.isCompleted) TextSoft else TextDeep,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = todo.priority,
                    style = MaterialTheme.typography.labelSmall.copy(color = pColor, fontWeight = FontWeight.Black)
                )
            }

            Icon(Icons.Default.ChevronRight, null, tint = BorderCandy)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDeleteBackground(state: SwipeToDismissBoxState) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(HighPastel.copy(alpha = 0.3f))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(Icons.Default.DeleteSweep, "Delete", tint = HighPastel)
    }
}