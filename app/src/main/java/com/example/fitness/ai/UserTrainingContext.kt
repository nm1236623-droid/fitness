package com.example.fitness.ai

import com.example.fitness.inbody.FirebaseInBodyRepository
import com.example.fitness.inbody.InBodyRecord
import com.example.fitness.user.FirebaseUserProfileFirestoreRepository
import com.example.fitness.user.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * AI 訓練計畫需要的「使用者整合資料」。
 *
 * 資料來源：
 * - UserProfile: Firestore user_profiles/{uid}
 * - InBodyRecord: Firestore inbody_records (latest)
 */
data class UserTrainingContext(
    val profile: UserProfile,
    val latestInBody: InBodyRecord?,
) {
    val age: Int get() = profile.age
    val heightCm: Float get() = profile.heightCm
    val weightKg: Float get() = profile.weightKg
    val muscleMassKg: Float? get() = latestInBody?.muscleMassKg
}

class UserTrainingContextRepository(
    private val profileRepo: FirebaseUserProfileFirestoreRepository = FirebaseUserProfileFirestoreRepository(),
) {
    fun observeMyContext(): Flow<UserTrainingContext> {
        val profileFlow = profileRepo.observeMyProfile()
        val inBodyFlow = FirebaseInBodyRepository.records

        return combine(profileFlow, inBodyFlow) { profile, inBodyRecords ->
            UserTrainingContext(
                profile = profile,
                latestInBody = inBodyRecords.firstOrNull(),
            )
        }
    }
}
