package kz.hashiroii.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kz.hashiroii.data.mapper.SubscriptionMapper
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreSubscriptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirestoreSubscriptionRepository {
    
    override fun getUserSubscriptions(userId: String): Flow<List<Subscription>> = flow {
        val snapshot = firestore
            .collection("users")
            .document(userId)
            .collection("subscriptions")
            .get()
            .await()
        
        val subscriptions = snapshot.documents.mapNotNull { doc ->
            try {
                val entity = doc.toObject(SubscriptionFirestoreEntity::class.java)
                entity?.let { SubscriptionMapper.toDomain(it.toEntity(doc.id)) }
            } catch (e: Exception) {
                null
            }
        }
        
        emit(subscriptions)
    }
    
    override suspend fun saveSubscription(userId: String, subscription: Subscription): Result<Unit> {
        return try {
            val entity = SubscriptionMapper.toEntity(subscription)
            val firestoreEntity = SubscriptionFirestoreEntity.fromEntity(entity)
            val subscriptionId = UUID.randomUUID().toString()
            
            firestore
                .collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .set(firestoreEntity)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSubscription(userId: String, subscriptionId: String): Result<Unit> {
        return try {
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
    
    override suspend fun syncSubscriptions(userId: String, subscriptions: List<Subscription>): Result<Unit> {
        return try {
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

data class SubscriptionFirestoreEntity(
    val serviceName: String = "",
    val serviceDomain: String = "",
    val cost: String = "",
    val period: String = "",
    val nextPaymentDate: Long = 0,
    val currentPaymentDate: Long = 0,
    val serviceType: String = "",
    val primaryColor: Long = 0,
    val secondaryColor: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(id: String): kz.hashiroii.data.model.SubscriptionEntity {
        return kz.hashiroii.data.model.SubscriptionEntity(
            serviceName = serviceName,
            serviceDomain = serviceDomain,
            cost = cost,
            period = period,
            nextPaymentDate = Instant.ofEpochMilli(nextPaymentDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            currentPaymentDate = Instant.ofEpochMilli(currentPaymentDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate(),
            serviceType = serviceType,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor
        )
    }
    
    companion object {
        fun fromEntity(entity: kz.hashiroii.data.model.SubscriptionEntity): SubscriptionFirestoreEntity {
            return SubscriptionFirestoreEntity(
                serviceName = entity.serviceName,
                serviceDomain = entity.serviceDomain,
                cost = entity.cost,
                period = entity.period,
                nextPaymentDate = entity.nextPaymentDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                currentPaymentDate = entity.currentPaymentDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                serviceType = entity.serviceType,
                primaryColor = entity.primaryColor,
                secondaryColor = entity.secondaryColor,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
