package kz.hashiroii.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kz.hashiroii.data.datasource.MockSubscriptionDataSource
import kz.hashiroii.data.network.NetworkMonitor
import kz.hashiroii.data.repository.CurrencyRepositoryImpl
import kz.hashiroii.data.repository.NotificationRepositoryImpl
import kz.hashiroii.data.repository.PreferencesRepositoryImpl
import kz.hashiroii.data.service.AppNameResolver
import kz.hashiroii.data.service.ServiceRecognizer
import kz.hashiroii.data.service.SubscriptionDetectionService
import kz.hashiroii.domain.repository.AuthRepository
import kz.hashiroii.domain.repository.CurrencyRepository
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.repository.NotificationRepository
import kz.hashiroii.domain.repository.PreferencesRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideServiceRecognizer(
        appNameResolver: AppNameResolver
    ): ServiceRecognizer {
        return ServiceRecognizer(appNameResolver)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        subscriptionDetectionService: SubscriptionDetectionService,
        mockDataSource: MockSubscriptionDataSource,
        authRepository: AuthRepository,
        firestoreSubscriptionRepository: FirestoreSubscriptionRepository
    ): NotificationRepository {
        return NotificationRepositoryImpl(
            subscriptionDetectionService,
            mockDataSource,
            authRepository,
            firestoreSubscriptionRepository
        )
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(): NetworkMonitor {
        return NetworkMonitor()
    }

    @Provides
    @Singleton
    fun provideCurrencyRepository(): CurrencyRepository {
        return CurrencyRepositoryImpl()
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        preferencesRepositoryImpl: PreferencesRepositoryImpl
    ): PreferencesRepository {
        return preferencesRepositoryImpl
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
