package com.example.projectmanager.data.model

data class TeamMember(
    val userId: String = "",
    val role: MemberRole = MemberRole.MEMBER,
    val joinedAt: Long = 0,
    val skills: List<String> = emptyList()
)
