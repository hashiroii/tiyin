package kz.hashiroii.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kz.hashiroii.designsystem.theme.TiyinTheme
import kz.hashiroii.profile.R

@Composable
fun ProfileScreenRoute(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    ProfileScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onSignInClick = onSignInClick,
        modifier = modifier
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onIntent: (ProfileIntent) -> Unit,
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is ProfileUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Profile Picture
                if (state.user?.photoUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(state.user.photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.user?.displayName?.take(1)?.uppercase() 
                                    ?: state.user?.email?.take(1)?.uppercase() 
                                    ?: "?",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // User Name
                Text(
                    text = state.user?.displayName 
                        ?: state.user?.email 
                        ?: "Guest",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                if (state.user?.email != null && state.user?.displayName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Sign In / Sign Out Button
                if (state.user == null) {
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_sign_in_with_google))
                    }
                } else {
                    Button(
                        onClick = { onIntent(ProfileIntent.SignOut) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.profile_sign_out))
                    }
                }
            }
        }
        
        is ProfileUiState.Error -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(name = "Authenticated - Light", showBackground = true)
@Composable
private fun ProfileScreenAuthenticatedLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen(
                uiState = ProfileUiState.Success(
                    user = kz.hashiroii.domain.model.auth.User(
                        id = "1",
                        email = "user@example.com",
                        displayName = "John Doe",
                        photoUrl = null
                    )
                ),
                onIntent = {},
                onBackClick = {},
                onSignInClick = {}
            )
        }
    }
}

@Preview(name = "Not Authenticated - Light", showBackground = true)
@Composable
private fun ProfileScreenNotAuthenticatedLightPreview() {
    TiyinTheme(themePreference = "Light") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen(
                uiState = ProfileUiState.Success(user = null),
                onIntent = {},
                onBackClick = {},
                onSignInClick = {}
            )
        }
    }
}

@Preview(name = "Loading - Dark", showBackground = true)
@Composable
private fun ProfileScreenLoadingDarkPreview() {
    TiyinTheme(themePreference = "Dark") {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            ProfileScreen(
                uiState = ProfileUiState.Loading,
                onIntent = {},
                onBackClick = {},
                onSignInClick = {}
            )
        }
    }
}
