package com.example.projectmanager.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.projectmanager.data.model.Comment
import com.example.projectmanager.data.model.FileAttachment
import com.example.projectmanager.util.AttachmentType
import com.example.projectmanager.util.formatDate
import com.example.projectmanager.util.formatDateFromTimestamp
import com.example.projectmanager.util.formatFileSize
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsAndAttachments(
    comments: List<Comment>,
    attachments: List<FileAttachment>,
    onAddComment: (String) -> Unit,
    onAddAttachment: (Uri) -> Unit,
    onDownloadAttachment: (FileAttachment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onDeleteAttachment: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    var showAddCommentDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddAttachment(it) }
    }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Comments (${comments.size})") },
                icon = { Icon(Icons.Default.Comment, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Attachments (${attachments.size})") },
                icon = { Icon(Icons.Default.AttachFile, contentDescription = null) }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> CommentsList(
                    comments = comments,
                    onDeleteComment = onDeleteComment,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> AttachmentsList(
                    attachments = attachments,
                    onDownloadAttachment = onDownloadAttachment,
                    onDeleteAttachment = onDeleteAttachment,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Add button
            FloatingActionButton(
                onClick = {
                    when (activeTab) {
                        0 -> showAddCommentDialog = true
                        1 -> filePickerLauncher.launch("*/*")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = when (activeTab) {
                        0 -> Icons.Default.Add
                        else -> Icons.Default.AttachFile
                    },
                    contentDescription = when (activeTab) {
                        0 -> "Add Comment"
                        else -> "Add Attachment"
                    }
                )
            }
        }
    }

    if (showAddCommentDialog) {
        AddCommentDialog(
            onDismiss = { showAddCommentDialog = false },
            onAddComment = { comment ->
                onAddComment(comment)
                showAddCommentDialog = false
            }
        )
    }
}

@Composable
fun CommentsList(
    comments: List<Comment>,
    onDeleteComment: (Comment) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(comments) { comment ->
            CommentItem(
                comment = comment,
                onDelete = { onDeleteComment(comment) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentItem(
    comment: Comment,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("https://ui-avatars.com/api/?name=${comment.userId}&size=32")
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Column {
                        Text(
                            text = comment.userId,
                            style = MaterialTheme.typography.titleSmall
                        )
                        comment.createdAt?.let {
                            Text(
                                text = formatDateFromTimestamp(it.time),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Comment")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment.content)

            if (comment.attachmentIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    comment.attachmentIds.forEach { attachmentId ->
                        AssistChip(
                            onClick = { /* Open attachment */ },
                            label = { Text("Attachment") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentsList(
    attachments: List<FileAttachment>,
    onDownloadAttachment: (FileAttachment) -> Unit,
    onDeleteAttachment: (FileAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No attachments",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attachments) { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onDownload = { onDownloadAttachment(attachment) },
                    onDelete = { onDeleteAttachment(attachment) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentItem(
    attachment: FileAttachment,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDownload)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Thumbnail or type icon
                if (attachment.thumbnail != null) {
                    AsyncImage(
                        model = attachment.thumbnail,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = when (AttachmentType.fromMimeType(attachment.mimeType)) {
                            AttachmentType.IMAGE -> Icons.Default.Image
                            AttachmentType.DOCUMENT -> Icons.Default.Description
                            AttachmentType.SPREADSHEET -> Icons.Default.TableChart
                            AttachmentType.PRESENTATION -> Icons.Default.Slideshow
                            AttachmentType.VIDEO -> Icons.Default.VideoFile
                            AttachmentType.AUDIO -> Icons.Default.AudioFile
                            AttachmentType.ARCHIVE -> Icons.Default.FolderZip
                            AttachmentType.OTHER -> Icons.Default.InsertDriveFile
                            else -> Icons.Default.InsertDriveFile
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attachment.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatFileSize(attachment.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    attachment.uploadedAt?.let {
                        Text(
                            text = "Uploaded ${formatDate(it)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun AddCommentDialog(
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Comment") },
        text = {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("Comment") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAddComment(commentText) },
                enabled = commentText.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(date)
}

private fun formatDateFromTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    return formatDate(date)
}

private fun formatFileSize(size: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var value = size.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024
        unitIndex++
    }
    return "%.1f %s".format(value, units[unitIndex])
}