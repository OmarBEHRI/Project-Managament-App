package com.example.projectmanager.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.ChatType
import com.example.projectmanager.ui.components.EmptyStateView
import com.example.projectmanager.ui.components.LoadingView
import com.example.projectmanager.ui.components.BottomNavBar
import com.example.projectmanager.navigation.MainScreenNavigator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    projectId: String? = null,
    viewModel: ChatListViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToNewChat: () -> Unit = {},
    appNavigator: MainScreenNavigator = com.example.projectmanager.navigation.AppNavigatorImpl(com.google.firebase.auth.FirebaseAuth.getInstance())
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(projectId) {
        viewModel.loadChats(projectId)
    }

    Scaffold(
        bottomBar = {
            // Only show bottom navigation if we have a valid AppNavigator
            if (appNavigator != null && appNavigator is com.example.projectmanager.navigation.MainScreenNavigator) {
                com.example.projectmanager.ui.components.BottomNavBar(
                    currentRoute = "chats",
                    appNavigator = appNavigator
                )
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(if (projectId != null) "Project Chat" else "Chats") },
                actions = {
                    IconButton(onClick = onNavigateToNewChat) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToNewChat) {
                Icon(Icons.Default.Chat, contentDescription = "New Chat")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }
                uiState.error != null -> {
                    EmptyStateView(
                        icon = Icons.Default.Error,
                        title = "Error",
                        message = uiState.error!!
                    )
                }
                uiState.chats.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.Chat,
                        title = "No Chats Yet",
                        message = "Start a new conversation"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.chats) { chat ->
                            ChatItem(
                                chat = chat,
                                currentUserId = viewModel.getCurrentUserId(),
                                onClick = { onNavigateToChat(chat.id) },
                                getUserName = viewModel::getUserName
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatItem(
    chat: Chat,
    currentUserId: String,
    onClick: () -> Unit,
    getUserName: (String, (String) -> Unit) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chat avatar or icon
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = when (chat.type) {
                        ChatType.DIRECT -> Icons.Default.Person
                        ChatType.GROUP -> Icons.Default.Group
                        ChatType.PROJECT -> Icons.Default.Folder
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Pour les conversations directes, afficher le nom de l'autre participant
                var displayName by remember { mutableStateOf("Loading...") }
                
                if (chat.type == ChatType.DIRECT) {
                    // Trouver l'ID de l'autre participant (pas l'utilisateur actuel)
                    val otherUserId = chat.participants.find { it != currentUserId }
                    
                    if (otherUserId != null) {
                        // Ru00e9cupu00e9rer le nom d'utilisateur u00e0 partir de son ID
                        LaunchedEffect(otherUserId) {
                            getUserName(otherUserId) { userName ->
                                displayName = userName
                            }
                        }
                    } else {
                        displayName = chat.name ?: "Unknown User"
                    }
                } else {
                    displayName = chat.name ?: "Unnamed Chat"
                }
                
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium
                )

                // Afficher le dernier message avec le style appropriu00e9 selon l'expu00e9diteur
                chat.lastMessage?.let { message ->
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val isFromCurrentUser = message.senderId == currentUserId
                    val messagePrefix = if (isFromCurrentUser) {
                        "You: "
                    } else if (!message.senderName.isNullOrBlank()) {
                        "${message.senderName}: "
                    } else {
                        ""
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFromCurrentUser) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Text(
                                text = messagePrefix + message.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFromCurrentUser) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } ?: run {
                    // Si aucun message n'existe encore
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No messages yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Timestamp and unread count
            Column(
                horizontalAlignment = Alignment.End
            ) {
                chat.updatedAt?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val unreadCount = chat.unreadCount[currentUserId] ?: 0
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val now = Calendar.getInstance()
    val messageDate = Calendar.getInstance().apply { time = date }

    return when {
        now.get(Calendar.DATE) == messageDate.get(Calendar.DATE) -> {
            // Today, show time
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        now.get(Calendar.DATE) - messageDate.get(Calendar.DATE) == 1 -> {
            // Yesterday
            "Yesterday"
        }
        now.get(Calendar.WEEK_OF_YEAR) == messageDate.get(Calendar.WEEK_OF_YEAR) -> {
            // This week, show day name
            SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        }
        else -> {
            // Other, show date
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
        }
    }
} 