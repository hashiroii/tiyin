package kz.hashiroii.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kz.hashiroii.data.repository.AuthRepositoryImpl
import kz.hashiroii.data.repository.FirestoreSubscriptionRepositoryImpl
import kz.hashiroii.data.repository.LogoRepositoryImpl
import kz.hashiroii.data.repository.RoomSubscriptionRepositoryImpl
import kz.hashiroii.domain.repository.AuthRepository
import kz.hashiroii.domain.repository.FirestoreSubscriptionRepository
import kz.hashiroii.domain.repository.LogoRepository
import kz.hashiroii.domain.repository.RoomSubscriptionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindLogoRepository(
        logoRepositoryImpl: LogoRepositoryImpl
    ): LogoRepository
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindFirestoreSubscriptionRepository(
        firestoreSubscriptionRepositoryImpl: FirestoreSubscriptionRepositoryImpl
    ): FirestoreSubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindRoomSubscriptionRepository(
        roomSubscriptionRepositoryImpl: RoomSubscriptionRepositoryImpl
    ) : RoomSubscriptionRepository
}