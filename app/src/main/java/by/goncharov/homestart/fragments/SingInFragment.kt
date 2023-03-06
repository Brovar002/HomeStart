package by.goncharov.homestart.fragments

import androidx.fragment.app.Fragment
import by.goncharov.homestart.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest

class SingInFragment : Fragment() {
    private fun startGoogleOneTapRequest() {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build(),
            )
            .build()
    }
}
