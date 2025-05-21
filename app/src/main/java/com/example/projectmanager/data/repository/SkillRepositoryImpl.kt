package com.example.projectmanager.data.repository

import com.example.projectmanager.data.model.Skill
import com.example.projectmanager.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SkillRepository {

    private val skillsCollection = firestore.collection("skills")

    override fun getAllSkills(): Flow<Resource<List<Skill>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = skillsCollection.get().await()
            val skills = snapshot.toObjects(Skill::class.java)
            emit(Resource.Success(skills))
        } catch (e: Exception) {
            Timber.e(e, "Error getting all skills")
            emit(Resource.Error(e.message ?: "Failed to get skills"))
        }
    }

    override fun getSkillsByIds(skillIds: List<String>): Flow<Resource<List<Skill>>> = flow {
        emit(Resource.Loading)
        try {
            if (skillIds.isEmpty()) {
                emit(Resource.Success(emptyList()))
                return@flow
            }

            Timber.d("Getting skills by IDs: $skillIds")
            
            // Firestore can only query up to 10 items at a time in a whereIn clause
            val skills = mutableListOf<Skill>()
            
            // Get each skill individually to ensure we don't miss any
            for (skillId in skillIds) {
                try {
                    val doc = skillsCollection.document(skillId).get().await()
                    if (doc.exists()) {
                        val skill = doc.toObject(Skill::class.java)
                        if (skill != null) {
                            skills.add(skill)
                            Timber.d("Found skill: ${skill.name}")
                        }
                    } else {
                        Timber.w("Skill with ID $skillId not found")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error getting skill with ID $skillId")
                }
            }
            
            Timber.d("Retrieved ${skills.size} skills out of ${skillIds.size} IDs")
            emit(Resource.Success(skills))
        } catch (e: Exception) {
            Timber.e(e, "Error getting skills by IDs")
            emit(Resource.Error(e.message ?: "Failed to get skills"))
        }
    }

    override fun searchSkills(query: String): Flow<Resource<List<Skill>>> = flow {
        emit(Resource.Loading)
        try {
            // Firestore doesn't support native text search, so we'll do a simple prefix search
            val snapshot = skillsCollection
                .orderBy("name")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .get()
                .await()
            
            val skills = snapshot.toObjects(Skill::class.java)
            emit(Resource.Success(skills))
        } catch (e: Exception) {
            Timber.e(e, "Error searching skills")
            emit(Resource.Error(e.message ?: "Failed to search skills"))
        }
    }

    override suspend fun createSkill(skill: Skill): Resource<Skill> {
        return try {
            // Check if a skill with the same name already exists
            val existingSkill = skillsCollection
                .whereEqualTo("name", skill.name)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?.toObject(Skill::class.java)

            if (existingSkill != null) {
                // If the skill already exists, increment its usage count
                val updatedSkill = existingSkill.copy(usageCount = existingSkill.usageCount + 1)
                skillsCollection.document(existingSkill.id).set(updatedSkill).await()
                Resource.Success(updatedSkill)
            } else {
                // Create a new skill
                val newSkillRef = skillsCollection.document()
                val newSkill = skill.copy(id = newSkillRef.id)
                newSkillRef.set(newSkill).await()
                Resource.Success(newSkill)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating skill")
            Resource.Error(e.message ?: "Failed to create skill")
        }
    }

    override suspend fun updateSkill(skill: Skill): Resource<Skill> {
        return try {
            skillsCollection.document(skill.id).set(skill).await()
            Resource.Success(skill)
        } catch (e: Exception) {
            Timber.e(e, "Error updating skill")
            Resource.Error(e.message ?: "Failed to update skill")
        }
    }

    override suspend fun deleteSkill(skillId: String): Resource<Unit> {
        return try {
            skillsCollection.document(skillId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting skill")
            Resource.Error(e.message ?: "Failed to delete skill")
        }
    }

    override fun getSkillById(skillId: String): Flow<Resource<Skill>> = flow {
        emit(Resource.Loading)
        try {
            val document = skillsCollection.document(skillId).get().await()
            val skill = document.toObject(Skill::class.java)
            if (skill != null) {
                emit(Resource.Success(skill))
            } else {
                emit(Resource.Error("Skill not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting skill by ID")
            emit(Resource.Error(e.message ?: "Failed to get skill"))
        }
    }
}
