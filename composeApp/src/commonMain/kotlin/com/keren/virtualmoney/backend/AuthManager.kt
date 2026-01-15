package com.keren.virtualmoney.backend

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Authentication state.
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object NotAuthenticated : AuthState()
    data class Authenticated(val user: GameUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Simplified user data for the game.
 */
data class GameUser(
    val odId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)

/**
 * Manages Firebase Authentication for both Android and iOS.
 */
class AuthManager(
    private val localStorage: LocalStorage
) {
    private val auth = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Current user or null if not authenticated.
     */
    val currentUser: GameUser?
        get() = (authState.value as? AuthState.Authenticated)?.user

    /**
     * Check if user is signed in.
     */
    val isSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Initialize auth and check current state.
     */
    suspend fun initialize() {
        _authState.value = AuthState.Loading

        try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                _authState.value = AuthState.Authenticated(firebaseUser.toGameUser())
            } else {
                _authState.value = AuthState.NotAuthenticated
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Failed to initialize auth")
        }
    }

    /**
     * Sign in anonymously (guest mode).
     * This allows players to start playing immediately without creating an account.
     */
    suspend fun signInAnonymously(): Result<GameUser> {
        return try {
            _authState.value = AuthState.Loading
            val result = auth.signInAnonymously()
            val user = result.user?.toGameUser()
                ?: throw Exception("Sign in succeeded but no user returned")

            localStorage.setUserId(user.odId)
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Anonymous sign-in failed")
            Result.failure(e)
        }
    }

    /**
     * Sign in with Google.
     * @param idToken The Google ID token from platform-specific sign-in flow
     */
    suspend fun signInWithGoogle(idToken: String): Result<GameUser> {
        return try {
            _authState.value = AuthState.Loading

            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = auth.signInWithCredential(credential)
            val user = result.user?.toGameUser()
                ?: throw Exception("Sign in succeeded but no user returned")

            localStorage.setUserId(user.odId)
            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            Result.failure(e)
        }
    }

    /**
     * Link anonymous account with Google.
     * Allows guest players to save their progress to a permanent account.
     */
    suspend fun linkWithGoogle(idToken: String): Result<GameUser> {
        return try {
            val currentUser = auth.currentUser
                ?: throw Exception("No user to link")

            if (!currentUser.isAnonymous) {
                throw Exception("User is not anonymous")
            }

            val credential = GoogleAuthProvider.credential(idToken, null)
            val result = currentUser.linkWithCredential(credential)
            val user = result.user?.toGameUser()
                ?: throw Exception("Link succeeded but no user returned")

            _authState.value = AuthState.Authenticated(user)
            Result.success(user)
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Account linking failed")
            Result.failure(e)
        }
    }

    /**
     * Sign out the current user.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            localStorage.clearAuthTokens()
            _authState.value = AuthState.NotAuthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete the current user's account.
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()
            localStorage.clearAll()
            _authState.value = AuthState.NotAuthenticated
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user display name.
     */
    suspend fun updateDisplayName(displayName: String): Result<Unit> {
        return try {
            val firebaseUser = auth.currentUser
                ?: throw Exception("No user signed in")

            firebaseUser.updateProfile(displayName = displayName)

            // Update local state
            val currentAuth = _authState.value
            if (currentAuth is AuthState.Authenticated) {
                _authState.value = AuthState.Authenticated(
                    currentAuth.user.copy(displayName = displayName)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observe auth state changes.
     */
    fun observeAuthState(): Flow<AuthState> = _authState.asStateFlow()

    /**
     * Get user ID or null if not signed in.
     */
    fun getUserId(): String? = auth.currentUser?.uid

    private fun FirebaseUser.toGameUser() = GameUser(
        odId = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoURL,
        isAnonymous = isAnonymous
    )
}
