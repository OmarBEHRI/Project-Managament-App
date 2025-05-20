package com.example.projectmanager.ui.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import com.example.projectmanager.data.model.*
import java.text.SimpleDateFormat
import java.util.*

// Overview tab showing project summary
@Composable
fun ProjectOverviewTab(project: Project) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Project metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Project Metrics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Budget metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            title = "Budget",
                            value = "${project.budgetCurrency} ${project.budgetAmount}",
                            icon = Icons.Outlined.AttachMoney
                        )
                        
                        MetricItem(
                            title = "Actual Cost",
                            value = "${project.budgetCurrency} ${project.actualCost}",
                            icon = Icons.Outlined.Receipt
                        )
                        
                        MetricItem(
                            title = "Variance",
                            value = "${project.budgetCurrency} ${project.budgetAmount - project.actualCost}",
                            icon = Icons.Outlined.TrendingUp,
                            valueColor = if (project.budgetAmount >= project.actualCost) 
                                Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                    
                    // Time metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            title = "Est. Hours",
                            value = "${project.estimatedHours}h",
                            icon = Icons.Outlined.Timer
                        )
                        
                        MetricItem(
                            title = "Actual Hours",
                            value = "${project.actualHours}h",
                            icon = Icons.Outlined.HourglassBottom
                        )
                        
                        MetricItem(
                            title = "Efficiency",
                            value = if (project.estimatedHours > 0) 
                                "${(project.estimatedHours / project.actualHours * 100).toInt()}%" 
                                else "N/A",
                            icon = Icons.Outlined.Speed,
                            valueColor = if (project.estimatedHours >= project.actualHours) 
                                Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
        }
        
        // Team summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Team",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            title = "Team Size",
                            value = "${project.members.size}",
                            icon = Icons.Outlined.People
                        )
                        
                        val managers = project.members.count { 
                            it.role == ProjectRole.MANAGER || it.role == ProjectRole.OWNER 
                        }
                        
                        MetricItem(
                            title = "Managers",
                            value = "$managers",
                            icon = Icons.Outlined.SupervisorAccount
                        )
                        
                        MetricItem(
                            title = "Members",
                            value = "${project.members.size - managers}",
                            icon = Icons.Outlined.Person
                        )
                    }
                }
            }
        }
        
        // Milestones
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (project.milestones.isEmpty()) {
                        Text(
                            text = "No milestones defined for this project",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            project.milestones.forEach { milestone ->
                                MilestoneItem(milestone = milestone)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tasks tab showing project tasks
@Composable
fun ProjectTasksTabComponent(
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    var filterStatus by remember { mutableStateOf<TaskStatus?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { filterStatus = null },
                label = { Text("All") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null
                    )
                }
            )
            
            TaskStatus.values().forEach { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = { filterStatus = status },
                    label = { Text(status.name.replace('_', ' ')) },
                    leadingIcon = {
                        val icon = when(status) {
                            TaskStatus.TODO -> Icons.Filled.Assignment
                            TaskStatus.IN_PROGRESS -> Icons.Filled.PlayArrow
                            TaskStatus.REVIEW -> Icons.Filled.RateReview
                            TaskStatus.COMPLETED -> Icons.Filled.Done
                            TaskStatus.BLOCKED -> Icons.Filled.Block
                            TaskStatus.CANCELLED -> Icons.Filled.Close
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        
        // Tasks list
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tasks found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a task to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val filteredTasks = if (filterStatus != null) {
                tasks.filter { it.status == filterStatus }
            } else {
                tasks
            }
            
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${filterStatus?.name?.replace('_', ' ') ?: ""} tasks found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { filterStatus = null }) {
                            Text("Clear Filter")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskListItem(
                            task = task,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }
    }
}

// Members tab showing project team
@Composable
fun ProjectMembersTab(members: List<User>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (members.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No team members found",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } else {
            items(members) { user ->
                MemberListItem(user = user)
            }
        }
    }
}

// Comments tab showing project discussions
@Composable
fun ProjectCommentsTab(project: Project) {
    // Sample comments for demonstration
    val sampleComments = listOf(
        Comment(
            id = "comment1",
            projectId = project.id,
            userId = "user1",
            authorName = "Alice Wonderland",
            content = "This is a great start! Looking forward to seeing the progress.",
            createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2), // 2 days ago
        ),
        Comment(
            id = "comment2",
            projectId = project.id,
            userId = "user2",
            authorName = "Bob The Builder",
            content = "I have a few questions about the scope. Can we discuss?",
            createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 1), // 1 day ago
        ),
        Comment(
            id = "comment3",
            projectId = project.id,
            userId = "user3",
            authorName = "Charlie Brown",
            content = "All looks good from my end.",
            createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 30), // 30 minutes ago
        )
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Comments list
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleComments) { comment ->
                CommentItem(comment = comment)
            }
        }
        
        // Comment input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var commentText by remember { mutableStateOf("") }
                
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    maxLines = 3
                )
                
                IconButton(
                    onClick = { /* Add comment */ },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

// Attachments tab showing project files
@Composable
fun ProjectAttachmentsTab(project: Project) {
    // Sample attachments for demonstration
    val sampleAttachments = listOf(
        FileAttachment(
            id = "file1",
            name = "ProjectProposal.pdf",
            mimeType = "application/pdf",
            size = 1024 * 1024 * 2, // 2MB
            downloadUrl = "https://example.com/proposal.pdf",
            uploadedBy = "user1",
            uploadedAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3)
        ),
        FileAttachment(
            id = "file2",
            name = "InitialMockups.zip",
            mimeType = "application/zip",
            size = 1024 * 1024 * 5, // 5MB
            downloadUrl = "https://example.com/mockups.zip",
            uploadedBy = "user2",
            uploadedAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2)
        ),
        FileAttachment(
            id = "file3",
            name = "LogoDesign.png",
            mimeType = "image/png",
            size = 1024 * 300, // 300KB
            downloadUrl = "https://example.com/logo.png",
            uploadedBy = "user1",
            uploadedAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 5)
        )
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Upload button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { /* Upload file */ },
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload File")
            }
        }
        
        // Attachments list
        if (sampleAttachments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Attachment,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No attachments found",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload files to share with the team",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleAttachments) { attachment ->
                    AttachmentItem(attachment = attachment)
                }
            }
        }
    }
}
