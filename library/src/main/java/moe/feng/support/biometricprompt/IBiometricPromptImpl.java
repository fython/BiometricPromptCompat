package moe.feng.support.biometricprompt;

import android.content.Context;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

@RestrictTo({RestrictTo.Scope.LIBRARY})
interface IBiometricPromptImpl {

    @NonNull
    Context getContext();

    void authenticate(@Nullable BiometricPromptCompat.ICryptoObject crypto,
                      @Nullable CancellationSignal cancel,
                      @NonNull BiometricPromptCompat.AuthenticationCallback callback);

}
