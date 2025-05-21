package com.example.projectmanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A modern floating action button component inspired by Trello's design.
 * This component provides a consistent FAB style throughout the app.
 */
@Composable
fun TrelloFAB(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String? = null,
    expanded: Boolean = false,
    text: String? = null
) {
    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            icon = { Icon(icon, contentDescription = contentDescription) },
            text = { Text(text) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

/**
 * A multi-action floating action button component that expands to show multiple options.
 */
@Composable
fun TrelloMultiFAB(
    mainFabIcon: ImageVector = Icons.Default.Add,
    mainFabText: String? = null,
    items: List<MultiFabItem>
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                items.forEach { item ->
                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            item.onClick()
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        text = { Text(item.label) },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        TrelloFAB(
            onClick = { expanded = !expanded },
            icon = mainFabIcon,
            contentDescription = "Toggle FAB menu",
            expanded = expanded && mainFabText != null,
            text = mainFabText
        )
    }
}

/**
 * Data class representing an item in the multi-action FAB menu.
 */
data class MultiFabItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)
