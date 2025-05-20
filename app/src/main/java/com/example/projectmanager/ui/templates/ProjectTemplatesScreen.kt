package com.example.projectmanager.ui.templates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.projectmanager.data.model.ProjectTemplate
import com.example.projectmanager.ui.components.EmptyStateView
import com.example.projectmanager.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTemplatesScreen(
    viewModel: ProjectTemplatesViewModel = hiltViewModel(),
    onNavigateToCreateTemplate: () -> Unit = {},
    onNavigateToEditTemplate: (String) -> Unit = {},
    onNavigateToCreateProject: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Templates") },
                actions = {
                    IconButton(onClick = onNavigateToCreateTemplate) {
                        Icon(Icons.Default.Add, contentDescription = "Create Template")
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
                uiState.templates.isEmpty() -> {
                    EmptyStateView(
                        icon = Icons.Default.Description,
                        title = "No Templates Yet",
                        message = "Create your first project template to get started"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.templates) { template ->
                            TemplateCard(
                                template = template,
                                onEditClick = { onNavigateToEditTemplate(template.id) },
                                onUseTemplate = { onNavigateToCreateProject(template.id) }
                            )
                        }
                    }
                }
            }

            // Error handling
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(
    template: ProjectTemplate,
    onEditClick: () -> Unit,
    onUseTemplate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onUseTemplate
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Template")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Template metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                template.category.takeIf { it.isNotEmpty() }?.let {
                    AssistChip(
                        onClick = {},
                        label = { Text(it) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text("${template.tasks.size} tasks") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                AssistChip(
                    onClick = {},
                    label = { Text("${template.estimatedDuration} days") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
} 