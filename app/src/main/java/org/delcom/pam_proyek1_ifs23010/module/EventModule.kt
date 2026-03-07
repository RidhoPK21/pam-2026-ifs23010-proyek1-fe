package org.delcom.pam_proyek1_ifs23010.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
// Pastikan import mengarah ke package events.service yang baru
import org.delcom.pam_proyek1_ifs23010.network.events.service.EventAppContainer
import org.delcom.pam_proyek1_ifs23010.network.events.service.IEventAppContainer
import org.delcom.pam_proyek1_ifs23010.network.events.service.IEventRepository

@Module
@InstallIn(SingletonComponent::class)
object EventModule { // Ubah nama dari TodoModule menjadi EventModule

    @Provides
    // Ubah nama fungsi dari providePlantContainer menjadi provideEventContainer
    fun provideEventContainer(): IEventAppContainer {
        return EventAppContainer()
    }

    @Provides
    // Ubah nama fungsi dari providePlantRepository menjadi provideEventRepository
    fun provideEventRepository(container: IEventAppContainer): IEventRepository {
        return container.repository
    }
}