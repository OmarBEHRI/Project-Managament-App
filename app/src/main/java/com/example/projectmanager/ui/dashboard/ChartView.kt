package com.example.projectmanager.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Chart components for the analytics dashboard
 */
class ChartView

@Composable
fun TaskCompletionRateChart(completedTasks: Int, totalTasks: Int, modifier: Modifier = Modifier) {
    val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) * 100f else 0f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Task Completion Rate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${String.format("%.1f", completionRate)}%",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = completionRate / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "$completedTasks completed",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "$totalTasks total",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ProjectStatusChart(statusCounts: Map<String, Int>, modifier: Modifier = Modifier) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )
    
    // Calculate total for percentages
    val total = statusCounts.values.sum().toFloat().coerceAtLeast(1f)
    
    // Calculate sweep angles for each slice
    val slices = statusCounts.entries.mapIndexed { index, entry ->
        val percentage = entry.value / total
        val sweepAngle = percentage * 360f
        
        Triple(
            entry.key,
            sweepAngle,
            colors[index % colors.size]
        )
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Projects by Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), contentAlignment = Alignment.Center) {
                
                // Extract colors outside of the Canvas to avoid @Composable invocation errors
                val sliceColors = slices.map { it.third }
                
                // Custom pie chart using Canvas
                Canvas(modifier = Modifier.size(180.dp)) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val radius = minOf(canvasWidth, canvasHeight) / 2
                    val center = Offset(canvasWidth / 2, canvasHeight / 2)
                    
                    var startAngle = -90f // Start from top (12 o'clock position)
                    
                    slices.forEachIndexed { index, (_, sweepAngle, _) ->
                        val color = sliceColors[index]
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2)
                        )
                        
                        // Draw outline
                        drawArc(
                            color = Color.White,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 2f)
                        )
                        
                        startAngle += sweepAngle
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                statusCounts.entries.forEachIndexed { index, entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(colors[index % colors.size], shape = MaterialTheme.shapes.small)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val percentage = (entry.value / total * 100).toInt()
                        Text(
                            text = "${entry.key}: ${entry.value} ($percentage%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun TimeToCompletionChart(timeData: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    // Custom bar chart implementation using Canvas
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Average Time to Completion (days)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (timeData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No completion data available", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // Find the maximum value for scaling
                val maxValue = timeData.maxOf { it.second }.coerceAtLeast(0.1f)
                
                // Extract colors outside of Canvas to avoid @Composable invocation errors
                val barColor = MaterialTheme.colorScheme.primary
                val axisColor = Color.Gray
                
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 20.dp, bottom = 30.dp, end = 20.dp)) {
                    
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth / (timeData.size * 2f)
                    
                    // Draw horizontal axis line
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 2f
                    )
                    
                    // Draw vertical axis line
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, canvasHeight),
                        strokeWidth = 2f
                    )
                    
                    // Draw bars
                    timeData.forEachIndexed { index, (_, value) ->
                        val barHeight = (value / maxValue) * canvasHeight
                        val left = index * (barWidth * 2) + barWidth / 2
                        
                        drawRect(
                            color = barColor,
                            topLeft = Offset(left, canvasHeight - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
                
                // Draw labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    timeData.forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", value),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamProductivityChart(productivityData: List<Pair<String, Float>>, modifier: Modifier = Modifier) {
    // Custom line chart implementation using Canvas
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Team Productivity (tasks/week)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (productivityData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No productivity data available", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                // Find the maximum value for scaling
                val maxValue = productivityData.maxOf { it.second }.coerceAtLeast(0.1f)
                
                // Extract colors outside of Canvas to avoid @Composable invocation errors
                val lineColor = MaterialTheme.colorScheme.secondary
                val pointColor = MaterialTheme.colorScheme.primary
                val axisColor = Color.Gray
                val whiteColor = Color.White
                
                Canvas(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 20.dp, bottom = 30.dp, end = 20.dp)) {
                    
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Draw horizontal axis line
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, canvasHeight),
                        end = Offset(canvasWidth, canvasHeight),
                        strokeWidth = 2f
                    )
                    
                    // Draw vertical axis line
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, canvasHeight),
                        strokeWidth = 2f
                    )
                    
                    // Calculate x positions
                    val xStep = canvasWidth / (productivityData.size - 1).coerceAtLeast(1)
                    val points = productivityData.mapIndexed { index, (_, value) ->
                        val x = index * xStep
                        val y = canvasHeight - (value / maxValue) * canvasHeight
                        Offset(x, y)
                    }
                    
                    // Draw lines connecting points
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3f
                        )
                    }
                    
                    // Draw points
                    points.forEach { point ->
                        drawCircle(
                            color = pointColor,
                            radius = 6f,
                            center = point
                        )
                        
                        // Draw white inner circle for better visibility
                        drawCircle(
                            color = whiteColor,
                            radius = 3f,
                            center = point
                        )
                    }
                }
                
                // Draw labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    productivityData.forEach { (label, value) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.1f", value),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeamMembersSection(teamMembers: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Team Members",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (teamMembers.isEmpty()) {
                Text(
                    text = "No team members found",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column {
                    teamMembers.forEach { memberId ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Simple circle avatar placeholder
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = memberId.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = memberId,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}