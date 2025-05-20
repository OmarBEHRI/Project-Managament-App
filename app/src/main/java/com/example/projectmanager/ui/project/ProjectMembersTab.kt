package com.example.projectmanager.ui.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Project
import com.example.projectmanager.data.model.ProjectMember
import com.example.projectmanager.data.model.ProjectRole
import com.example.projectmanager.data.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectMembersTab(
    project: Project,
    members: List<User>,
    userSuggestions: List<User>,
    searchQuery: String,
    isCurrentUserManager: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onAddMember: (User, ProjectRole) -> Unit,
    onRemoveMember: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(ProjectRole.MEMBER) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Only show add member UI if the current user is a manager
        if (isCurrentUserManager) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search users by name or email") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true
            )
            
            // User suggestions
            AnimatedVisibility(
                visible = userSuggestions.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(userSuggestions) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedUser = user
                                        showAddMemberDialog = true
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.displayName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // User details
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Add button
                                IconButton(
                                    onClick = {
                                        selectedUser = user
                                        showAddMemberDialog = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add member",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Divider()
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Team members list
        Text(
            text = "Team Members (${members.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (members.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No team members yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(members) { user ->
                    val member = project.members.find { it.userId == user.id }
                    val role = member?.role ?: ProjectRole.MEMBER
                    val isOwner = role == ProjectRole.OWNER
                    val canRemove = isCurrentUserManager && !isOwner
                    
                    MemberListItemWithActions(
                        user = user,
                        role = role,
                        onRemove = if (canRemove) { { onRemoveMember(user.id) } } else null
                    )
                }
            }
        }
    }
    
    // Add member role selection dialog
    if (showAddMemberDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Member") },
            text = {
                Column {
                    Text("Select role for ${selectedUser?.displayName}")
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Role selection
                    Column {
                        ProjectRole.values().filter { it != ProjectRole.OWNER }.forEach { role ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRole = role }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRole == role,
                                    onClick = { selectedRole = role }
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = role.name.replace('_', ' '),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUser?.let { onAddMember(it, selectedRole) }
                        showAddMemberDialog = false
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddMemberDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MemberListItemWithActions(
    user: User,
    role: ProjectRole,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Role badge
            val roleColor = when(role) {
                ProjectRole.OWNER -> MaterialTheme.colorScheme.tertiary
                ProjectRole.ADMIN -> MaterialTheme.colorScheme.primary
                ProjectRole.MANAGER -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = roleColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = role.name.replace('_', ' '),
                    style = MaterialTheme.typography.labelSmall,
                    color = roleColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Remove button (only shown if onRemove is provided)
            if (onRemove != null) {
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onRemove
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove member",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
