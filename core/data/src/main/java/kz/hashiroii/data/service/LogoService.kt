package kz.hashiroii.data.service

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoService @Inject constructor() {
    
    fun getLogoUrlsWithFallbacks(domain: String): List<String> {
        return listOf(
            "https://www.google.com/s2/favicons?domain=$domain&sz=256",
            "https://icons.duckduckgo.com/ip3/$domain.ico",
            "https://logo.clearbit.com/$domain",
            "https://$domain/favicon.ico"
        )
    }
    
    fun getPrimaryLogoUrl(domain: String): String {
        return "https://www.google.com/s2/favicons?domain=$domain&sz=256"
    }
}
