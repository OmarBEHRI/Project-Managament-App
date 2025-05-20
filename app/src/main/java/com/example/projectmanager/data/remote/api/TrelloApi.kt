package com.example.projectmanager.data.remote.api

import retrofit2.Response
import retrofit2.http.*

interface TrelloApi {
    @GET("boards")
    suspend fun getBoards(
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<List<TrelloBoard>>

    @GET("boards/{boardId}")
    suspend fun getBoard(
        @Path("boardId") boardId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String,
        @Query("lists") includeLists: Boolean = true
    ): Response<TrelloBoard>

    @POST("boards")
    suspend fun createBoard(
        @Query("name") name: String,
        @Query("desc") description: String? = null,
        @Query("defaultLists") defaultLists: Boolean = true,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<TrelloBoard>

    @GET("boards/{boardId}/lists")
    suspend fun getBoardLists(
        @Path("boardId") boardId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<List<TrelloList>>

    @POST("lists")
    suspend fun createList(
        @Query("name") name: String,
        @Query("idBoard") boardId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<TrelloList>

    @GET("lists/{listId}/cards")
    suspend fun getCardsInList(
        @Path("listId") listId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<List<TrelloCard>>

    @POST("cards")
    suspend fun createCard(
        @Query("name") name: String,
        @Query("desc") description: String? = null,
        @Query("idList") listId: String,
        @Query("due") dueDate: String? = null,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<TrelloCard>

    @PUT("cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: String,
        @Query("name") name: String? = null,
        @Query("desc") description: String? = null,
        @Query("idList") listId: String? = null,
        @Query("due") dueDate: String? = null,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<TrelloCard>

    @DELETE("cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<Unit>

    @POST("cards/{cardId}/members")
    suspend fun addMemberToCard(
        @Path("cardId") cardId: String,
        @Query("value") memberId: String,
        @Query("key") apiKey: String,
        @Query("token") token: String
    ): Response<TrelloCard>
}

data class TrelloBoard(
    val id: String,
    val name: String,
    val desc: String,
    val url: String,
    val lists: List<TrelloList>? = null
)

data class TrelloList(
    val id: String,
    val name: String,
    val idBoard: String,
    val closed: Boolean,
    val pos: Int
)

data class TrelloCard(
    val id: String,
    val name: String,
    val desc: String,
    val idList: String,
    val idBoard: String,
    val due: String? = null,
    val dueComplete: Boolean = false,
    val idMembers: List<String> = emptyList(),
    val url: String
)