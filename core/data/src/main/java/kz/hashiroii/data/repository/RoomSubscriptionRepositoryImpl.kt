package kz.hashiroii.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kz.hashiroii.data.local.SubscriptionDao
import kz.hashiroii.data.mapper.SubscriptionRoomMapper.toEntity
import kz.hashiroii.data.mapper.SubscriptionRoomMapper.toRoomEntity
import kz.hashiroii.data.mapper.SubscriptionMapper
import kz.hashiroii.domain.model.service.Subscription
import kz.hashiroii.domain.repository.AuthRepository
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.repository.RoomSubscriptionRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionDao: SubscriptionDao,
    private val authRepository: AuthRepository,
    private val firestoreSubscriptionRepository: FirestoreSubscriptionRepository
) : RoomSubscriptionRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


    override fun getSubscriptions(): Flow<List<Subscription>> {
        return subscriptionDao.getAllFlow().map { roomEntities ->
            val today = LocalDate.now()
            val domainList = roomEntities.map { it.toEntity() }.map { SubscriptionMapper.toDomain(it) }
            // Just return the rolled list for display — don't write back here
            domainList.map { it.rolledIfExpired(today) }
        }
            .distinctUntilChanged() // ← extra safety: suppress duplicate emissions
    }

    override suspend fun refreshSubscriptions(userId: String?) {
        withContext(Dispatchers.IO) {
            if (userId == null) {
                return@withContext
            }
            try {
                val today = LocalDate.now()
                val subscriptions = firestoreSubscriptionRepository.getUserSubscriptions(userId).first()
                val rolledSubscriptions = subscriptions.map { it.rolledIfExpired(today) }
                val roomEntities = rolledSubscriptions.map { SubscriptionMapper.toEntity(it).toRoomEntity() }
                subscriptionDao.replaceAll(roomEntities)
                firestoreSubscriptionRepository.syncSubscriptions(userId, rolledSubscriptions)
            } catch (e: Exception) {
                // Firestore failed (e.g. API key expired); keep existing local data.
            }
        }
    }

    override suspend fun addSubscription(subscription: Subscription) {
        withContext(Dispatchers.IO) {
            val entity = SubscriptionMapper.toEntity(subscription)
            subscriptionDao.insert(entity.toRoomEntity())
        }
    }

    override suspend fun updateSubscription(oldSubscription: Subscription, newSubscription: Subscription) {
        withContext(Dispatchers.IO) {
            val newEntity = SubscriptionMapper.toEntity(newSubscription)
            subscriptionDao.update(newEntity.toRoomEntity())
        }
    }

    override suspend fun deleteSubscription(subscription: Subscription) {
        withContext(Dispatchers.IO) {
            subscription.id?.let { subscriptionDao.deleteById(it) }
                ?: subscriptionDao.deleteByServiceNameAndDomain(
                    subscription.serviceInfo.name,
                    subscription.serviceInfo.domain
                )
        }
    }

    override suspend fun getSubscriptionById(serviceName: String, serviceDomain: String): Subscription? {
        return withContext(Dispatchers.IO) {
            val subscription = subscriptionDao.getByServiceNameAndDomain(serviceName, serviceDomain)
                ?.toEntity()
                ?.let { SubscriptionMapper.toDomain(it) } ?: return@withContext null
            val today = LocalDate.now()
            val rolled = subscription.rolledIfExpired(today)
            if (rolled != subscription) {
                updateSubscription(subscription, rolled)
                authRepository.getCurrentUser()?.let { user ->
                    rolled.id?.let { id ->
                        firestoreSubscriptionRepository.updateSubscription(user.id, id, rolled)
                    }
                }
            }
            rolled
        }
    }
}