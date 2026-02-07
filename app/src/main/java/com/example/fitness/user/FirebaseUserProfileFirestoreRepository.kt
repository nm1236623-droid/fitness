package com.example.fitness.user

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 個人資料基本設定：Firestore 單一資料源 (Single Source of Truth)
 * Collection: user_profiles/{uid}
 * 規則：firestore.rules 已允許 /user_profiles/{uid} 僅本人可讀寫
 */
class FirebaseUserProfileFirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    private fun docRef() = db.collection(COLLECTION).document(requireUid())

    private fun requireUid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("尚未登入")

    fun observeMyProfile(): Flow<UserProfile> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            close(IllegalStateException("尚未登入"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val data = snapshot?.data
                val profile = UserProfile(
                    nickname = (data?.get("nickname") as? String).orEmpty(),
                    age = (data?.get("age") as? Number)?.toInt() ?: 0,
                    avatarUri = data?.get("avatarUri") as? String,
                    weightKg = (data?.get("weightKg") as? Number)?.toFloat() ?: 70f,
                    heightCm = (data?.get("heightCm") as? Number)?.toFloat() ?: 170f,
                    tdee = (data?.get("tdee") as? Number)?.toInt() ?: 2000,
                    proteinGoalGrams = (data?.get("proteinGoalGrams") as? Number)?.toFloat() ?: 120f,
                )
                trySend(profile)
            }

        awaitClose { registration.remove() }
    }

    /**
     * 全量覆寫（實際使用 merge），避免把其他欄位清空。
     */
    suspend fun saveMyProfile(profile: UserProfile): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("尚未登入"))

        return try {
            val payload = mapOf(
                "uid" to uid,
                "nickname" to profile.nickname.trim(),
                "age" to profile.age,
                "avatarUri" to profile.avatarUri,
                "weightKg" to profile.weightKg,
                "heightCm" to profile.heightCm,
                "tdee" to profile.tdee,
                "proteinGoalGrams" to profile.proteinGoalGrams,
                "updatedAt" to FieldValue.serverTimestamp(),
            )

            db.collection(COLLECTION).document(uid)
                .set(payload, com.google.firebase.firestore.SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    companion object {
        const val COLLECTION = "user_profiles"
    }
}
