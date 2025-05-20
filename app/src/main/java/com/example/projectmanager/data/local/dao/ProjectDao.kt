package com.example.projectmanager.data.local.dao

import androidx.room.*
import com.example.projectmanager.data.local.entity.ProjectEntity
import com.example.projectmanager.data.model.ProjectStatus
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("SELECT * FROM projects")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE ownerId = :ownerId")
    fun getProjectsByOwnerId(ownerId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE deadline < :date AND status != 'COMPLETED'")
    fun getProjectsWithUpcomingDeadlines(date: Date): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%'")
    fun searchProjectsByName(query: String): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET completedTasks = :completedCount, totalTasks = :totalCount WHERE id = :projectId")
    suspend fun updateProjectProgress(projectId: String, completedCount: Int, totalCount: Int)

    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Query("DELETE FROM projects")
    suspend fun deleteAllProjects()

    @Query("SELECT * FROM projects WHERE ownerId = :userId OR :userId IN (SELECT value FROM json_each(members))")
    fun getProjectsByUser(userId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE status = :status")
    fun getProjectsByStatus(status: ProjectStatus): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC LIMIT :limit")
    fun getRecentProjects(limit: Int): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%'")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Query("UPDATE projects SET totalTasks = totalTasks + 1 WHERE id = :projectId")
    suspend fun incrementTotalTasks(projectId: String)

    @Query("UPDATE projects SET completedTasks = completedTasks + 1 WHERE id = :projectId")
    suspend fun incrementCompletedTasks(projectId: String)

    @Query("UPDATE projects SET completedTasks = completedTasks - 1 WHERE id = :projectId")
    suspend fun decrementCompletedTasks(projectId: String)
}