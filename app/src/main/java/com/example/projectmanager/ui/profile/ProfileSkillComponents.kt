package com.example.projectmanager.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projectmanager.data.model.Skill

/**
 * Displays a section for user skills with the ability to add and remove skills
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsSection(
    skills: List<Skill>,
    onAddSkill: () -> Unit,
    onRemoveSkill: (Skill) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Add skill button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Skills",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            IconButton(
                onClick = onAddSkill,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Skill"
                )
            }
        }
        
        // Skills list
        if (skills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No skills added yet. Click the + button to add skills.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                skills.forEach { skill ->
                    SkillChip(
                        skill = skill,
                        onRemove = { onRemoveSkill(skill) }
                    )
                }
            }
        }
    }
}

/**
 * A chip that displays a skill with the ability to remove it
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillChip(
    skill: Skill,
    onRemove: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = { /* No action on click */ },
        label = { Text(skill.name) },
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove Skill",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        },
        colors = InputChipDefaults.inputChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

/**
 * Dialog for adding new skills or selecting from existing ones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSkillDialog(
    availableSkills: List<Skill>,
    isSearching: Boolean,
    onSearch: (String) -> Unit,
    onAddSkill: (String) -> Unit,
    onAddExistingSkill: (Skill) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var newSkillName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Skill") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search existing skills
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        onSearch(it)
                    },
                    label = { Text("Search Skills") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Available skills section
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (searchQuery.isNotEmpty()) {
                    if (availableSkills.isNotEmpty()) {
                        Text(
                            text = "Available Skills",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            availableSkills.forEach { skill ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onAddExistingSkill(skill) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = skill.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            if (skill.description.isNotEmpty()) {
                                                Text(
                                                    text = skill.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add Skill",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No skills found for \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Create new skill section
                Text(
                    text = "Create New Skill",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                OutlinedTextField(
                    value = newSkillName,
                    onValueChange = { newSkillName = it },
                    label = { Text("Skill Name") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAddSkill(newSkillName) },
                enabled = newSkillName.isNotBlank()
            ) {
                Text("Add New Skill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
