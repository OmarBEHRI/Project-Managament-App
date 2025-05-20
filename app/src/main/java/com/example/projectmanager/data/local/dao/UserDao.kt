package com.example.projectmanager.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.projectmanager.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE email = :email")
    fun getUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id IN (:userIds)")
    fun getUsersByIds(userIds: List<String>): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE displayName LIKE '%' || :query || '%'")
    fun searchUsersByName(query: String): Flow<List<UserEntity>>

    @Query("UPDATE users SET lastLoginAt = CASE WHEN :isOnline = 1 THEN :timestamp ELSE lastLoginAt END WHERE id = :userId")
    suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET lastLoginAt = :lastActive WHERE id = :userId")
    suspend fun updateUserLastActive(userId: String, lastActive: Long)
}