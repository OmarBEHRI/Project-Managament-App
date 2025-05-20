package com.example.projectmanager.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SlackApi {
    @POST("chat.postMessage")
    suspend fun postMessage(
        @Header("Authorization") authToken: String,
        @Body message: SlackMessage
    ): Response<SlackResponse>

    @GET("conversations.list")
    suspend fun getConversations(
        @Header("Authorization") authToken: String,
        @Query("types") types: String = "public_channel,private_channel"
    ): Response<ConversationsResponse>

    @GET("conversations.history")
    suspend fun getConversationHistory(
        @Header("Authorization") authToken: String,
        @Query("channel") channelId: String,
        @Query("limit") limit: Int = 100
    ): Response<ConversationHistoryResponse>

    @GET("conversations.members")
    suspend fun getConversationMembers(
        @Header("Authorization") authToken: String,
        @Query("channel") channelId: String
    ): Response<ConversationMembersResponse>

    @POST("conversations.create")
    suspend fun createConversation(
        @Header("Authorization") authToken: String,
        @Query("name") name: String,
        @Query("is_private") isPrivate: Boolean = false
    ): Response<CreateConversationResponse>

    @POST("conversations.invite")
    suspend fun inviteToConversation(
        @Header("Authorization") authToken: String,
        @Query("channel") channelId: String,
        @Query("users") userIds: String // comma-separated list
    ): Response<SlackResponse>

    @Multipart
    @POST("files.upload")
    suspend fun uploadFile(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("channels") channels: String,
        @Part("title") title: String? = null,
        @Part("initial_comment") initialComment: String? = null
    ): Response<FileUploadResponse>

    @GET("users.list")
    suspend fun getUsers(
        @Header("Authorization") authToken: String
    ): Response<UsersListResponse>
}

data class SlackMessage(
    val channel: String,
    val text: String,
    val blocks: List<SlackBlock>? = null,
    val attachments: List<SlackAttachment>? = null
)

data class SlackBlock(
    val type: String,
    val text: SlackText? = null,
    val elements: List<SlackElement>? = null
)

data class SlackText(
    val type: String, // plain_text or mrkdwn
    val text: String,
    val emoji: Boolean? = true
)

data class SlackElement(
    val type: String,
    val text: SlackText? = null,
    val value: String? = null,
    val url: String? = null
)

data class SlackAttachment(
    val color: String? = null,
    val pretext: String? = null,
    val title: String? = null,
    val text: String? = null,
    val fields: List<SlackField>? = null
)

data class SlackField(
    val title: String,
    val value: String,
    val short: Boolean = false
)

data class SlackResponse(
    val ok: Boolean,
    val error: String? = null,
    val ts: String? = null,
    val channel: String? = null
)

data class ConversationsResponse(
    val ok: Boolean,
    val channels: List<SlackChannel>? = null,
    val error: String? = null
)

data class SlackChannel(
    val id: String,
    val name: String,
    val is_private: Boolean,
    val is_channel: Boolean,
    val is_group: Boolean,
    val is_im: Boolean,
    val is_archived: Boolean,
    val num_members: Int
)

data class ConversationHistoryResponse(
    val ok: Boolean,
    val messages: List<SlackMessageResponse>? = null,
    val has_more: Boolean,
    val error: String? = null
)

data class SlackMessageResponse(
    val type: String,
    val user: String,
    val text: String,
    val ts: String, // timestamp
    val attachments: List<SlackAttachment>? = null,
    val blocks: List<SlackBlock>? = null
)

data class ConversationMembersResponse(
    val ok: Boolean,
    val members: List<String>? = null,
    val error: String? = null
)

data class CreateConversationResponse(
    val ok: Boolean,
    val channel: SlackChannel? = null,
    val error: String? = null
)

data class FileUploadResponse(
    val ok: Boolean,
    val file: SlackFile? = null,
    val error: String? = null
)

data class SlackFile(
    val id: String,
    val name: String,
    val title: String,
    val mimetype: String,
    val filetype: String,
    val permalink: String,
    val permalink_public: String?,
    val channels: List<String>
)

data class UsersListResponse(
    val ok: Boolean,
    val members: List<SlackUser>? = null,
    val error: String? = null
)

data class SlackUser(
    val id: String,
    val team_id: String,
    val name: String,
    val real_name: String?,
    val profile: SlackUserProfile
)

data class SlackUserProfile(
    val display_name: String,
    val email: String?,
    val image_24: String?,
    val image_72: String?,
    val image_192: String?
)