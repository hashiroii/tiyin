package kz.hashiroii.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kz.hashiroii.data.mapper.SubscriptionMapper
import kz.hashiroii.data.model.SubscriptionFirestoreEntity
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSubscriptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : FirestoreSubscriptionRepository {
    
    override fun getUserSubscriptions(userId: String): Flow<List<Subscription>> = flow {
        val subscriptions = withContext(Dispatchers.IO) {
            val snapshot = firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val entity = doc.toObject(SubscriptionFirestoreEntity::class.java)
                    entity?.let { SubscriptionMapper.toDomain(it.toEntity(doc.id)) }
                } catch (e: Exception) {
                    null
                }
            }
        }
        emit(subscriptions)
    }
    
    override suspend fun saveSubscription(userId: String, subscription: Subscription, subscriptionId: String?): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
            val entity = SubscriptionMapper.toEntity(subscription)
            val firestoreEntity = SubscriptionFirestoreEntity.fromEntity(entity)
            val docId = subscriptionId ?: UUID.nameUUIDFromBytes(
                "${subscription.serviceInfo.domain}-${subscription.serviceInfo.name}".toByteArray()
            ).toString()

            firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(docId)
                .set(firestoreEntity)
                .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateSubscription(userId: String, subscriptionId: String, subscription: Subscription): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
            val entity = SubscriptionMapper.toEntity(subscription)
            val firestoreEntity = SubscriptionFirestoreEntity.fromEntity(entity)

            firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .set(firestoreEntity, SetOptions.merge())
                .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .delete()
                .await()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncSubscriptions(userId: String, subscriptions: List<Subscription>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
            val batch = firestore.batch()
            val subscriptionsRef = firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
            
            subscriptions.forEach { subscription ->
                val entity = SubscriptionMapper.toEntity(subscription)
                val firestoreEntity = SubscriptionFirestoreEntity.fromEntity(entity)
                val subscriptionId = UUID.nameUUIDFromBytes(
                    "${subscription.serviceInfo.domain}-${subscription.serviceInfo.name}".toByteArray()
                ).toString()
                
                batch.set(
                    subscriptionsRef.document(subscriptionId),
                    firestoreEntity,
                    SetOptions.merge()
                )
            }
            
                batch.commit().await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}