package com.example.projectmanager.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


interface GoogleDriveApi {
    @GET("files")
    suspend fun listFiles(
        @Query("q") query: String? = null,
        @Query("spaces") spaces: String = "drive",
        @Query("fields") fields: String = "files(id,name,mimeType,size,thumbnailLink,webViewLink,createdTime)"
    ): Response<DriveFilesResponse>

    @GET("files/{fileId}")
    suspend fun getFile(
        @Path("fileId") fileId: String,
        @Query("fields") fields: String = "id,name,mimeType,size,thumbnailLink,webViewLink,createdTime"
    ): Response<DriveFile>

    @Multipart
    @POST("files")
    suspend fun uploadFile(
        @Part("metadata") metadata: DriveFileMetadata,
        @Part file: MultipartBody.Part
    ): Response<DriveFile>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(
        @Path("fileId") fileId: String
    ): Response<Unit>

    @PATCH("files/{fileId}")
    suspend fun updateFileMetadata(
        @Path("fileId") fileId: String,
        @Body metadata: DriveFileMetadata
    ): Response<DriveFile>

    @GET("files/{fileId}/permissions")
    suspend fun getFilePermissions(
        @Path("fileId") fileId: String
    ): Response<PermissionsResponse>

    @POST("files/{fileId}/permissions")
    suspend fun shareFile(
        @Path("fileId") fileId: String,
        @Body permission: Permission
    ): Response<Permission>
}

data class DriveFilesResponse(
    val files: List<DriveFile>,
    val nextPageToken: String?
)

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: Long?,
    val thumbnailLink: String?,
    val webViewLink: String?,
    val createdTime: String
)

data class DriveFileMetadata(
    val name: String,
    val mimeType: String,
    val parents: List<String>? = null,
    val description: String? = null
)

data class PermissionsResponse(
    val permissions: List<Permission>
)

data class Permission(
    val id: String? = null,
    val type: String, // user, group, domain, anyone
    val role: String, // owner, organizer, fileOrganizer, writer, commenter, reader
    val emailAddress: String? = null
)