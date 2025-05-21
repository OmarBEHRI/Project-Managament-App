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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.User
import com.example.projectmanager.data.model.ChatType
import com.example.projectmanager.ui.components.EmptyStateView
import com.example.projectmanager.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    projectId: String? = null,
    viewModel: NewChatViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToChat: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Conversation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Only show group chat option if not in project context
                    if (projectId == null) {
                        IconButton(onClick = { showCreateGroupDialog = true }) {
                            Icon(Icons.Default.Group, contentDescription = "Create Group")
                        }
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
                    EmptyStateView(
                        icon = Icons.Default.Error,
                        title = "Error",
                        message = uiState.error!!
                    )
                }
                uiState.users.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "No Users Found",
                        message = "There are no other users to chat with"
                    )
                }
                else -> {
                    Column {
                        // Search bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            placeholder = { Text("Search users") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            singleLine = true
                        )
                        
                        // User list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(uiState.filteredUsers) { user ->
                                UserItem(
                                    user = user,
                                    onClick = {
                                        // Au lieu de cru00e9er une conversation, naviguer directement vers l'u00e9cran de conversation
                                        // avec l'ID de l'utilisateur. La conversation sera cru00e9u00e9e seulement si un message est envoyu00e9
                                        onNavigateToChat("new_direct_${user.id}")
                                    },
                                    onSelectForGroup = { viewModel.toggleUserSelection(user.id) },
                                    isSelected = uiState.selectedUserIds.contains(user.id),
                                    showCheckbox = uiState.selectedUserIds.isNotEmpty() || showCreateGroupDialog
                                )
                            }
                        }
                    }
                    
                    // FAB to create group chat if users are selected
                    if (uiState.selectedUserIds.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { showCreateGroupDialog = true },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Create Group Chat")
                        }
                    }
                }
            }
        }
    }
    
    // Dialog for creating a group chat
    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text("Create Group Chat") },
            text = {
                Column {
                    Text("Enter a name for your group chat")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text("Group name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (uiState.selectedUserIds.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Please select at least one user to create a group",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "${uiState.selectedUserIds.size} users selected",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uiState.selectedUserIds.isNotEmpty() && groupName.isNotBlank()) {
                            viewModel.createGroupChat(groupName) { chatId ->
                                showCreateGroupDialog = false
                                onNavigateToChat(chatId)
                            }
                        }
                    },
                    enabled = uiState.selectedUserIds.isNotEmpty() && groupName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit,
    onSelectForGroup: () -> Unit,
    isSelected: Boolean = false,
    showCheckbox: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = if (showCheckbox) onSelectForGroup else onClick),
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
            // User avatar
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // User details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Checkbox for group selection
            if (showCheckbox) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectForGroup() }
                )
            }
        }
    }
}
