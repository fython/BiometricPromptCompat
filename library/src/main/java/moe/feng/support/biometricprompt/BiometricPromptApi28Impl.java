package moe.feng.support.biometricprompt;

import android.content.Context;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;

import java.security.Signature;

import javax.crypto.Cipher;
import javax.crypto.Mac;

@RequiresApi(api = Build.VERSION_CODES.P)
@RestrictTo({RestrictTo.Scope.LIBRARY})
class BiometricPromptApi28Impl implements IBiometricPromptImpl {

    private final Context context;

    private final BiometricPrompt biometricPrompt;

    BiometricPromptApi28Impl(@NonNull Context context, @NonNull BiometricPrompt prompt) {
        this.context = context;
        this.biometricPrompt = prompt;
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void authenticate(
            @Nullable BiometricPromptCompat.ICryptoObject crypto,
            @Nullable CancellationSignal cancel,
            @NonNull BiometricPromptCompat.IAuthenticationCallback callback
    ) {
        final BiometricPrompt.AuthenticationCallback authCallback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                callback.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                callback.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                callback.onAuthenticationSucceeded(
                        () -> new CryptoObjectApi28Impl(result.getCryptoObject()));
            }

            @Override
            public void onAuthenticationFailed() {
                callback.onAuthenticationFailed();
            }
        };
        if (crypto != null) {
            biometricPrompt.authenticate(
                    toCryptoObjectApi28(crypto), cancel, context.getMainExecutor(), authCallback);
        } else {
            biometricPrompt.authenticate(cancel, context.getMainExecutor(), authCallback);
        }
    }

    private static BiometricPrompt.CryptoObject toCryptoObjectApi28(
            @Nullable BiometricPromptCompat.ICryptoObject ico
    ) {
        if (ico == null) {
           return null;
        } else if (ico.getCipher() != null) {
            return new BiometricPrompt.CryptoObject(ico.getCipher());
        } else if (ico.getMac() != null) {
            return new BiometricPrompt.CryptoObject(ico.getMac());
        } else if (ico.getSignature() != null) {
            return new BiometricPrompt.CryptoObject(ico.getSignature());
        } else {
            throw new IllegalArgumentException("ICryptoObject doesn\'t include any data.");
        }
    }

    private static class CryptoObjectApi28Impl implements BiometricPromptCompat.ICryptoObject {

        private final BiometricPrompt.CryptoObject cryptoObject;

        CryptoObjectApi28Impl(BiometricPrompt.CryptoObject cryptoObject) {
            this.cryptoObject = cryptoObject;
        }

        @Override
        public Signature getSignature() {
            return cryptoObject.getSignature();
        }

        @Override
        public Cipher getCipher() {
            return cryptoObject.getCipher();
        }

        @Override
        public Mac getMac() {
            return cryptoObject.getMac();
        }

    }

}
