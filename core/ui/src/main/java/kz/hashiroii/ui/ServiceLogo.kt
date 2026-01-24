package kz.hashiroii.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.hashiroii.domain.model.service.ServiceInfo

@Composable
fun ServiceLogo(
    serviceInfo: ServiceInfo,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val context = LocalContext.current
    var currentUrlIndex by remember { mutableIntStateOf(0) }
    var showFallback by remember { mutableStateOf(false) }
    
    val logoUrls = if (serviceInfo.logoUrls.isNotEmpty()) {
        serviceInfo.logoUrls
    } else {
        serviceInfo.effectiveLogoUrl?.let { listOf(it) } ?: emptyList()
    }
    
    if (logoUrls.isNotEmpty() && !showFallback && currentUrlIndex < logoUrls.size) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(logoUrls[currentUrlIndex])
                .crossfade(true)
                .build(),
            contentDescription = "${serviceInfo.name} logo",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
            onError = {
                if (currentUrlIndex < logoUrls.size - 1) {
                    currentUrlIndex++
                } else {
                    showFallback = true
                }
            },
            onSuccess = {
                showFallback = false
            }
        )
    } else {
        FallbackLogo(serviceInfo = serviceInfo, size = size)
    }
}

@Composable
private fun FallbackLogo(
    serviceInfo: ServiceInfo,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                Color(serviceInfo.primaryColor).copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (serviceInfo.name.isNotEmpty()) {
            Text(
                text = serviceInfo.name.take(1).uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = Color(serviceInfo.primaryColor),
                fontWeight = FontWeight.Bold
            )
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(size * 0.4f),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
