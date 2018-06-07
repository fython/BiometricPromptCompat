package moe.feng.support.biometricprompt;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.M)
public abstract class AbstractBiometricPromptDialogFragment extends DialogFragment {

    public static final String ARG_TITLE = "title";
    public static final String ARG_SUBTITLE = "subtitle";
    public static final String ARG_DESCRIPTION = "description";
    public static final String ARG_NEGATIVE_BUTTON_TEXT = "negative_button_text";

    private boolean shouldNotifyClose = true;

    private BiometricPromptApi23Impl biometricPromptApi23;

    public AbstractBiometricPromptDialogFragment() {
        setRetainInstance(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BiometricPromptCompat.isApiPSupported()) {
            throw new UnsupportedOperationException("AbstractBiometricPromptDialogFragment " +
                    "is only designed for old versions which SDK is under 28 (not included).");
        }

        if (getArguments() == null) {
            throw new IllegalArgumentException("Arguments cannot be null or empty.");
        }

        String title = getArguments().getString(ARG_TITLE);
        String subtitle = getArguments().getString(ARG_SUBTITLE);
        String description = getArguments().getString(ARG_DESCRIPTION);
        String negativeButtonText = getArguments().getString(ARG_NEGATIVE_BUTTON_TEXT);

        if (title == null) {
            throw new IllegalArgumentException("You should set a title for BiometricPrompt.");
        }

        biometricPromptApi23 = new BiometricPromptApi23Impl(
                getActivity(), title, subtitle, description, negativeButtonText,
                (dialog, which) -> onNegativeButtonClick()
        );
    }

    @Override
    public final Dialog onCreateDialog(Bundle savedInstanceState) {
        return biometricPromptApi23.getAuthenticateDialogForFragment(
                getCryptoObject(), getCancellationSignal(), new AuthCallback());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (shouldNotifyClose &&
                getCancellationSignal() != null && !getCancellationSignal().isCanceled()) {
            getCancellationSignal().cancel();
            shouldNotifyClose = false;
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (shouldNotifyClose &&
                getCancellationSignal() != null && !getCancellationSignal().isCanceled()) {
            getCancellationSignal().cancel();
            shouldNotifyClose = false;
        }
        dismiss();
    }

    @Nullable
    public BiometricPromptCompat.ICryptoObject getCryptoObject() {
        return null;
    }

    @Nullable
    public CancellationSignal getCancellationSignal() {
        return null;
    }

    public abstract void onAuthenticationSucceeded(
            @NonNull BiometricPromptCompat.IAuthenticationResult result);

    public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {

    }

    public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {

    }

    public void onAuthenticationFailed() {

    }

    public void onNegativeButtonClick() {

    }

    private class AuthCallback implements BiometricPromptCompat.IAuthenticationCallback {

        @Override
        public void onAuthenticationSucceeded(
                @NonNull BiometricPromptCompat.IAuthenticationResult result) {
            shouldNotifyClose = false;
            AbstractBiometricPromptDialogFragment.this.onAuthenticationSucceeded(result);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {
            AbstractBiometricPromptDialogFragment.this.onAuthenticationHelp(helpCode, helpString);
        }

        @Override
        public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {
            AbstractBiometricPromptDialogFragment.this.onAuthenticationError(errorCode, errString);
        }

        @Override
        public void onAuthenticationFailed() {
            AbstractBiometricPromptDialogFragment.this.onAuthenticationFailed();
        }

    }

}
