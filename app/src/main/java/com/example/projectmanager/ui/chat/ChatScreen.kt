package com.example.projectmanager.ui.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.projectmanager.data.model.Message
import com.example.projectmanager.data.model.MessageType
import com.example.projectmanager.ui.components.LoadingView
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import com.example.projectmanager.data.model.ChatType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    viewModel: ChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var showAttachmentOptions by remember { mutableStateOf(false) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendAttachment(it) }
    }

    LaunchedEffect(chatId) {
        viewModel.loadChat(chatId)
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    val appNavigator = remember {
        object : com.example.projectmanager.navigation.AppNavigator {
            override fun navigateBack() { onNavigateBack() }
            override fun navigateToSignIn() {}
            override fun navigateToSignUp() {}
            override fun navigateToForgotPassword() {}
            override fun navigateToHome() { onNavigateBack() }
            override fun navigateToProjects() { onNavigateBack() }
            override fun navigateToProject(projectId: String) {}
            override fun navigateToTasks() { onNavigateBack() }
            override fun navigateToTask(taskId: String) {}
            override fun navigateToProfile() {}
            override fun navigateToSettings() {}
            override fun navigateToCreateProject() {}
            override fun navigateToCreateTask(projectId: String?) {}
            override fun navigateToEditProject(projectId: String) {}
            override fun navigateToEditTask(taskId: String) {}
            override fun navigateToAnalyticsDashboard() {}
            override fun isUserSignedIn(): Boolean = true
            override fun navigateToChats() {}
            override fun navigateToChat(chatId: String) {}
            override fun navigateToNewChat(projectId: String?) {}
            override fun navigateToChatSettings(chatId: String) {}
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        // Display the chat name with a more descriptive fallback
                        Text(
                            text = when {
                                !uiState.chat?.name.isNullOrBlank() -> uiState.chat?.name ?: "Chat"
                                uiState.chat?.type == ChatType.DIRECT -> "Conversation"
                                uiState.chat?.type == ChatType.GROUP -> "Group Chat"
                                uiState.chat?.type == ChatType.PROJECT -> "Project Chat"
                                else -> "Chat"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        // Show additional information based on chat type
                        when (uiState.chat?.type) {
                            ChatType.DIRECT -> {
                                // For direct chats, show the other participant's name
                                val otherParticipantId = uiState.chat?.participants?.find { it != viewModel.getCurrentUserId() }
                                if (otherParticipantId != null) {
                                    LaunchedEffect(otherParticipantId) {
                                        viewModel.loadUserInfo(otherParticipantId)
                                    }
                                }
                                
                                Text(
                                    text = viewModel.getRecipientName() ?: "Loading...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            ChatType.GROUP -> {
                                Text(
                                    text = "${uiState.chat?.participants?.size ?: 0} participants",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            ChatType.PROJECT -> {
                                Text(
                                    text = "Project: ${uiState.projectName ?: ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            else -> { /* No subtitle for other chat types */ }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Add a settings button to view chat details
                    val context = LocalContext.current
                    IconButton(onClick = { 
                        // Navigate to chat settings
                        uiState.chat?.id?.let { chatId ->
                            // This would be handled by the navigation callback
                            // For now we'll just show a toast
                            android.widget.Toast.makeText(context, "Chat settings", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Chat Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingView()
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uiState.error!!)
                    }
                }
                else -> {
                    // Messages list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            // Récupérer l'ID de l'utilisateur actuel
                            val currentUserId = viewModel.getCurrentUserId()
                            
                            // S'assurer que senderId n'est pas null ou vide
                            val senderId = message.senderId?.takeIf { it.isNotBlank() } ?: "unknown"
                            
                            // Déterminer si le message vient de l'utilisateur actuel
                            // Forcer la comparaison avec trim() pour éviter les problèmes d'espaces
                            val isFromCurrentUser = senderId.trim() == currentUserId.trim()
                            
                            // Afficher les informations de débogage dans la console
                            println("DEBUG DISPLAY: Message ID: ${message.id}, Content: ${message.content.take(20)}...")
                            println("DEBUG DISPLAY: Message senderId: '$senderId', CurrentUserId: '$currentUserId'")
                            println("DEBUG DISPLAY: isFromCurrentUser: $isFromCurrentUser")
                            
                            // Ajouter un Spacer pour séparer les messages
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            MessageItem(
                                message = message,
                                isFromCurrentUser = isFromCurrentUser,
                                onAttachmentClick = { /* Open attachment */ }
                            )
                        }
                    }

                    // Message input
                    MessageInput(
                        message = uiState.messageText,
                        onMessageChange = { viewModel.updateMessageText(it) },
                        onSendClick = { viewModel.sendMessage() },
                        onAttachmentClick = { showAttachmentOptions = true }
                    )
                }
            }
        }

        // Attachment options dialog
        if (showAttachmentOptions) {
            AlertDialog(
                onDismissRequest = { showAttachmentOptions = false },
                title = { Text("Add Attachment") },
                text = {
                    Column {
                        ListItem(
                            headlineContent = { Text("Image") },
                            leadingContent = {
                                Icon(Icons.Default.Image, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                filePickerLauncher.launch("image/*")
                                showAttachmentOptions = false
                            }
                        )
                        ListItem(
                            headlineContent = { Text("File") },
                            leadingContent = {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                            },
                            modifier = Modifier.clickable {
                                filePickerLauncher.launch("*/*")
                                showAttachmentOptions = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAttachmentOptions = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MessageItem(
    message: Message,
    isFromCurrentUser: Boolean,
    onAttachmentClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Afficher le nom de l'expu00e9diteur pour les messages qui ne sont pas de l'utilisateur actuel
        if (!isFromCurrentUser) {
            val senderName = message.senderName
            if (senderName != null && senderName.isNotBlank()) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }
        }
        
        // Message bubble
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            color = if (isFromCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.content,
                            color = if (isFromCurrentUser) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    MessageType.IMAGE -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(message.content)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.5f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onAttachmentClick(message.content) },
                            contentScale = ContentScale.Crop
                        )
                    }
                    MessageType.FILE -> {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAttachmentClick(message.content) },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.InsertDriveFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = message.content.substringAfterLast("/"),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    MessageType.SYSTEM -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Timestamp
                message.sentAt?.let { date ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatMessageTime(date),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isFromCurrentUser) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachmentClick) {
                Icon(Icons.Default.AttachFile, contentDescription = "Add Attachment")
            }

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Type a message") },
                maxLines = 4,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            IconButton(
                onClick = onSendClick,
                enabled = message.isNotBlank()
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

private fun formatMessageTime(date: Date): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
} 