package kz.hashiroii.domain.model.service

data class ServiceInfo(
    val name: String,
    val logoResId: Int,
    val primaryColor: Long,
    val secondaryColor: Long,
    val serviceType: ServiceType
)
