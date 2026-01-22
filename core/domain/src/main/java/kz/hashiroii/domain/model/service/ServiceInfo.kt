package kz.hashiroii.domain.model.service

data class ServiceInfo(
    val name: String,
    val logoUrl: String? = null,
    val logoResId: Int = 0,
    val primaryColor: Long,
    val secondaryColor: Long,
    val serviceType: ServiceType
)
