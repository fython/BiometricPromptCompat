package moe.feng.support.biometricprompt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.util.Log;

import java.security.Signature;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;

public class BiometricPromptCompat {

    public static final int BIOMETRIC_ACQUIRED_GOOD = 0;
    public static final int BIOMETRIC_ACQUIRED_IMAGER_DIRTY = 3;
    public static final int BIOMETRIC_ACQUIRED_INSUFFICIENT = 2;
    public static final int BIOMETRIC_ACQUIRED_PARTIAL = 1;
    public static final int BIOMETRIC_ACQUIRED_TOO_FAST = 5;
    public static final int BIOMETRIC_ACQUIRED_TOO_SLOW = 4;
    public static final int BIOMETRIC_ERROR_CANCELED = 5;
    public static final int BIOMETRIC_ERROR_HW_NOT_PRESENT = 12;
    public static final int BIOMETRIC_ERROR_HW_UNAVAILABLE = 1;
    public static final int BIOMETRIC_ERROR_LOCKOUT = 7;
    public static final int BIOMETRIC_ERROR_LOCKOUT_PERMANENT = 9;
    public static final int BIOMETRIC_ERROR_NO_BIOMETRICS = 11;
    public static final int BIOMETRIC_ERROR_NO_SPACE = 4;
    public static final int BIOMETRIC_ERROR_TIMEOUT = 3;
    public static final int BIOMETRIC_ERROR_UNABLE_TO_PROCESS = 2;
    public static final int BIOMETRIC_ERROR_USER_CANCELED = 10;
    public static final int BIOMETRIC_ERROR_VENDOR = 8;

    private static final boolean IS_PREVIEW_SDK_SUPPORTED = true;

    static final String TAG = BiometricPromptCompat.class.getSimpleName();

    // TODO: Use PackageManager.FEATURE_XXX instead. They are missing in current sdk releases.
    private static final String FEATURE_IRIS = "android.hardware.iris";
    private static final String FEATURE_FACE = "android.hardware.face";

    private static final String[] SUPPORTED_BIOMETRIC_FEATURES = new String[] {
            PackageManager.FEATURE_FINGERPRINT,
            FEATURE_IRIS,
            FEATURE_FACE
    };

    @RestrictTo({RestrictTo.Scope.LIBRARY, RestrictTo.Scope.LIBRARY_GROUP})
    static boolean isApiPSupported() {
        return (IS_PREVIEW_SDK_SUPPORTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                && Build.VERSION.PREVIEW_SDK_INT >= 1)
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static boolean isHardwareDetected(@NonNull Context context) {
        if (isApiPSupported()) {
            final PackageManager pm = context.getPackageManager();
            return Arrays.stream(SUPPORTED_BIOMETRIC_FEATURES).anyMatch(pm::hasSystemFeature);
        } if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManager fm = context.getSystemService(FingerprintManager.class);
            return fm != null && fm.isHardwareDetected();
        } else {
            Log.e(TAG, "Device software version is too low so we return " +
                    "isHardwareDetected=false instead. Recommend to check software version " +
                    "by yourself before using BiometricPromptCompat.");
            return false;
        }
    }

    public static boolean hasEnrolledFingerprints(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManager manager = context.getSystemService(FingerprintManager.class);
            return manager != null && manager.hasEnrolledFingerprints();
        } else {
            Log.e(TAG, "Device software version is too low so we return " +
                    "hasEnrolledFingerprints=false instead. Recommend to check software version " +
                    "by yourself before using BiometricPromptCompat.");
            return false;
        }
    }

    @NonNull
    private final Context context;

    @NonNull
    private final IBiometricPromptImpl impl;

    private BiometricPromptCompat(@NonNull IBiometricPromptImpl impl) {
        this.context = impl.getContext();
        this.impl = impl;
    }

    IBiometricPromptImpl getImpl() {
        return impl;
    }

    public void authenticate(@NonNull BiometricPromptCompat.AuthenticationCallback callback) {
        impl.authenticate(null, null, callback);
    }

    public void authenticate(@Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptCompat.AuthenticationCallback callback) {
        impl.authenticate(null, cancel, callback);
    }

    public void authenticate(@Nullable BiometricPromptCompat.ICryptoObject crypto,
                             @Nullable CancellationSignal cancel,
                             @NonNull BiometricPromptCompat.AuthenticationCallback callback) {
        impl.authenticate(crypto, cancel, callback);
    }

    public static final class Builder {

        @NonNull
        private final Context context;

        @Nullable
        private CharSequence title;

        @Nullable
        private CharSequence subtitle;

        @Nullable
        private CharSequence description;

        @Nullable
        private CharSequence negativeButtonText;

        @Nullable
        private DialogInterface.OnClickListener negativeButtonListener;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        @NonNull
        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        @NonNull
        public Builder setSubtitle(CharSequence subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        @NonNull
        public Builder setDescription(CharSequence description) {
            this.description = description;
            return this;
        }

        @NonNull
        public Builder setNegativeButton(@NonNull CharSequence text,
                                         @Nullable DialogInterface.OnClickListener listener) {
            this.negativeButtonText = text;
            this.negativeButtonListener = listener;
            return this;
        }

        @NonNull
        public Builder setNegativeButton(@StringRes int textResId,
                                         @Nullable DialogInterface.OnClickListener listener) {
            this.negativeButtonText = context.getString(textResId);
            this.negativeButtonListener = listener;
            return this;
        }

        @SuppressLint("NewApi")
        @NonNull
        public BiometricPromptCompat build() {
            if (title == null) {
                throw new IllegalArgumentException("You should set a title for BiometricPrompt.");
            }
            if (isApiPSupported()) {
                BiometricPrompt.Builder builder = new BiometricPrompt.Builder(context);
                builder.setTitle(title);
                if (subtitle != null) {
                    builder.setSubtitle(subtitle);
                }
                if (description != null) {
                    builder.setDescription(description);
                }
                if (negativeButtonText != null) {
                    builder.setNegativeButton(
                            negativeButtonText, context.getMainExecutor(), negativeButtonListener);
                }
                return new BiometricPromptCompat(
                        new BiometricPromptApi28Impl(context, builder.build())
                );
            } else {
                return new BiometricPromptCompat(
                        new BiometricPromptApi23Impl(
                                context, title, subtitle, description,
                                negativeButtonText, negativeButtonListener
                        )
                );
            }
        }

    }

    public abstract static class AuthenticationCallback {

        /**
         * Called when an unrecoverable error has been encountered and the operation is complete.
         * No further callbacks will be made on this object.
         * @param errorCode An integer identifying the error message
         * @param errString A human-readable error string that can be shown in UI
         */
        public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {

        }

        /**
         * Called when a recoverable error has been encountered during authentication. The help
         * string is provided to give the user guidance for what went wrong, such as
         * "Sensor dirty, please clean it."
         * @param helpCode An integer identifying the error message
         * @param helpString A human-readable string that can be shown in UI
         */
        public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {

        }

        /**
         * Called when a fingerprint is recognized.
         * @param result An object containing authentication-related data
         */
        public void onAuthenticationSucceeded(@NonNull BiometricPromptCompat.IAuthenticationResult result) {

        }

        /**
         * Called when a fingerprint is valid but not recognized.
         */
        public void onAuthenticationFailed() {

        }

    }

    public interface IAuthenticationResult {

        @Nullable BiometricPromptCompat.ICryptoObject getCryptoObject();

    }

    public interface ICryptoObject {

        /**
         * Get {@link Signature} object.
         * @return {@link Signature} object or null if this doesn't contain one.
         */
        @Nullable Signature getSignature();

        /**
         * Get {@link Cipher} object.
         * @return {@link Cipher} object or null if this doesn't contain one.
         */
        @Nullable Cipher getCipher();

        /**
         * Get {@link Mac} object.
         * @return {@link Mac} object or null if this doesn't contain one.
         */
        @Nullable Mac getMac();

    }

}
