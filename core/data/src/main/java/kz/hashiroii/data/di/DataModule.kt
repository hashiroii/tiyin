package kz.hashiroii.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kz.hashiroii.data.repository.NotificationRepositoryImpl
import kz.hashiroii.data.service.ServiceRecognizer
import kz.hashiroii.domain.repository.NotificationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideServiceRecognizer(
        @ApplicationContext context: Context
    ): ServiceRecognizer {
        return ServiceRecognizer(context)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        serviceRecognizer: ServiceRecognizer
    ): NotificationRepository {
        return NotificationRepositoryImpl()
    }
}
