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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.Chat
import com.example.projectmanager.data.model.ChatType
import com.example.projectmanager.data.model.User
import com.example.projectmanager.ui.components.LoadingView
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSettingsScreen(
    chatId: String,
    viewModel: ChatSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onDeleteChat: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddParticipantDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newChatName by remember { mutableStateOf("") }
    
    LaunchedEffect(chatId) {
        viewModel.loadChatDetails(chatId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                uiState.chat != null -> {
                    val chat = uiState.chat!!
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        // Chat info section
                        item {
                            ChatInfoSection(chat, onRenameClick = {
                                newChatName = chat.name ?: ""
                                showRenameDialog = true
                            })
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        
                        // Participants section (for group chats)
                        if (chat.type != ChatType.DIRECT) {
                            item {
                                Text(
                                    "Participants (${uiState.participants.size})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                if (uiState.isAdmin) {
                                    OutlinedButton(
                                        onClick = { showAddParticipantDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Add Participants")
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            
                            items(uiState.participants) { participant ->
                                ParticipantItem(
                                    user = participant,
                                    isCurrentUser = participant.id == viewModel.getCurrentUserId(),
                                    onRemove = { 
                                        if (uiState.isAdmin) {
                                            viewModel.removeParticipant(participant.id)
                                        }
                                    },
                                    canRemove = uiState.isAdmin
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        
                        // Actions section
                        item {
                            Text(
                                "Actions",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Mute notifications
                            SwitchItem(
                                title = "Mute Notifications",
                                icon = Icons.Default.NotificationsOff,
                                checked = uiState.isMuted,
                                onCheckedChange = { viewModel.toggleMuteNotifications() }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Clear chat history
                            ActionItem(
                                title = "Clear Chat History",
                                icon = Icons.Default.DeleteSweep,
                                onClick = { viewModel.clearChatHistory() }
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Leave group (for group chats) or Delete chat (for direct chats)
                            if (chat.type != ChatType.DIRECT) {
                                ActionItem(
                                    title = "Leave Group",
                                    icon = Icons.Default.ExitToApp,
                                    onClick = { showLeaveDialog = true },
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            } else {
                                ActionItem(
                                    title = "Delete Chat",
                                    icon = Icons.Default.Delete,
                                    onClick = { showDeleteDialog = true },
                                    textColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Leave group dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Group") },
            text = { Text("Are you sure you want to leave this group chat?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.leaveGroup {
                            showLeaveDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete chat dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this chat? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChat {
                            showDeleteDialog = false
                            onDeleteChat()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Rename chat dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = newChatName,
                    onValueChange = { newChatName = it },
                    label = { Text("Chat Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChatName.isNotBlank()) {
                            viewModel.renameChat(newChatName)
                            showRenameDialog = false
                        }
                    },
                    enabled = newChatName.isNotBlank()
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChatInfoSection(chat: Chat, onRenameClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chat icon
        Surface(
            modifier = Modifier
                .size(80.dp)
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
                    .padding(16.dp)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chat name with edit button for group chats
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = chat.name ?: "Chat",
                style = MaterialTheme.typography.headlineSmall
            )
            
            if (chat.type != ChatType.DIRECT) {
                IconButton(onClick = onRenameClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename")
                }
            }
        }
        
        // Chat type
        Text(
            text = when (chat.type) {
                ChatType.DIRECT -> "Direct Message"
                ChatType.GROUP -> "Group Chat"
                ChatType.PROJECT -> "Project Chat"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Creation date
        chat.createdAt?.let { date ->
            Text(
                text = "Created on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ParticipantItem(
    user: User,
    isCurrentUser: Boolean,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User avatar
        Surface(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // User name and status
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.displayName + if (isCurrentUser) " (You)" else "",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Remove button (only for admins and not for current user)
        if (canRemove && !isCurrentUser) {
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.PersonRemove,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SwitchItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun ActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}
