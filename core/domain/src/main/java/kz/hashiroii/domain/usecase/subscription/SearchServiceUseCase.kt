package kz.hashiroii.domain.usecase.subscription

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kz.hashiroii.domain.model.service.ServiceInfo
import kz.hashiroii.domain.model.service.ServiceType
import kz.hashiroii.domain.usecase.logo.GetLogoUrlUseCase
import javax.inject.Inject

class SearchServiceUseCase @Inject constructor(
    private val getLogoUrlUseCase: GetLogoUrlUseCase
) {
    operator fun invoke(query: String): Flow<ServiceSearchResult?> {
        return flow {
            if (query.isBlank()) {
                emit(null)
                return@flow
            }
            
            // Simple search - extract domain from query or use query as domain
            val domain = extractDomain(query)
            val serviceName = extractServiceName(query)
            
            val serviceInfo = ServiceInfo(
                name = serviceName,
                domain = domain,
                logoResId = 0,
                primaryColor = 0xFF6200EE,
                secondaryColor = 0xFF000000,
                serviceType = ServiceType.OTHER
            )
            
            // Get logo URL for the domain - collect first value
            var logoUrl: String? = null
            getLogoUrlUseCase(domain).collect { url ->
                logoUrl = url
                emit(
                    ServiceSearchResult(
                        serviceInfo = serviceInfo,
                        logoUrl = logoUrl
                    )
                )
                return@collect
            }
        }
    }
    
    private fun extractDomain(query: String): String {
        // Try to extract domain from query
        val trimmed = query.trim().lowercase()
        
        // If it looks like a domain (contains .)
        if (trimmed.contains(".")) {
            return trimmed
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")
                .split("/")[0]
        }
        
        // Otherwise, create a domain from the service name
        return "$trimmed.com"
    }
    
    private fun extractServiceName(query: String): String {
        val trimmed = query.trim()
        
        // If it looks like a domain, extract the name part
        if (trimmed.contains(".")) {
            val domain = trimmed
                .removePrefix("http://")
                .removePrefix("https://")
                .removePrefix("www.")
                .split("/")[0]
                .split(".")[0]
            
            return domain.replaceFirstChar { it.uppercaseChar() }
        }
        
        // Otherwise use the query as the name
        return trimmed.replaceFirstChar { it.uppercaseChar() }
    }
}

data class ServiceSearchResult(
    val serviceInfo: ServiceInfo,
    val logoUrl: String?
)
