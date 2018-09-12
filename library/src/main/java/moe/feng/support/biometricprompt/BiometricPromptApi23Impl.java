package moe.feng.support.biometricprompt;

import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.util.Log;
import android.view.View;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import java.security.Signature;

@RequiresApi(api = Build.VERSION_CODES.M)
@RestrictTo({RestrictTo.Scope.LIBRARY})
class BiometricPromptApi23Impl implements IBiometricPromptImpl {

    private final Context context;

    private final Handler mainHandler;
    private final Handler animateHandler;

    private final FingerprintManager fingerprintManager;

    private final BiometricPromptCompatDialog dialog;

    @Nullable
    private CancellationSignal cancellationSignal;

    private BiometricPromptCompat.IAuthenticationCallback callback;

    private FingerprintManager.AuthenticationCallback fmAuthCallback
            = new FingerprintManagerAuthenticationCallbackImpl();

    @Nullable
    private DialogInterface.OnClickListener negativeButtonListener;

    BiometricPromptApi23Impl(
            @NonNull Context context,
            @NonNull CharSequence title,
            @Nullable CharSequence subtitle,
            @Nullable CharSequence description,
            @Nullable CharSequence negativeButtonText,
            @Nullable DialogInterface.OnClickListener negativeButtonListener
    ) {
        this.context = context;
        this.mainHandler = new Handler(context.getMainLooper());
        this.animateHandler = new AnimateHandler(context.getMainLooper());
        this.fingerprintManager = context.getSystemService(FingerprintManager.class);
        this.negativeButtonListener = negativeButtonListener;

        dialog = new BiometricPromptCompatDialog(context);

        dialog.setTitle(title);
        if (subtitle == null) {
            dialog.getSubtitle().setVisibility(View.GONE);
        } else {
            dialog.getSubtitle().setText(subtitle);
        }
        if (description == null) {
            dialog.getDescription().setVisibility(View.GONE);
        } else {
            dialog.getDescription().setText(description);
        }
        if (negativeButtonText == null) {
            dialog.getNegativeButton().setVisibility(View.INVISIBLE);
        } else {
            dialog.getNegativeButton().setText(negativeButtonText);
            if (negativeButtonListener == null) {
                throw new IllegalArgumentException("Negative button listener should not be null.");
            }
            dialog.getNegativeButton().setOnClickListener(v -> {
                dialog.dismiss();
                negativeButtonListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
            });
        }
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
        if (dialog.isShowing()) {
            throw new IllegalArgumentException("BiometricPrompt has been started.");
        }
        this.cancellationSignal = cancel;
        this.callback = callback;
        if (cancellationSignal == null) {
            cancellationSignal = new CancellationSignal();
        }
        CancellationSignal innerCancel = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> {
            innerCancel.cancel();
            dialog.cancel();
        });

        dialog.setOnDismissListener(dialogInterface -> {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        });
        dialog.setOnCancelListener(dialogInterface -> {
            if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
                cancellationSignal.cancel();
            }
        });
        dialog.setOnShowListener(d -> {
            dialog.getFingerprintIcon().setState(
                    FingerprintIconView.State.ON, false);
            fingerprintManager.authenticate(
                    toCryptoObjectApi23(crypto), innerCancel,
                    0, fmAuthCallback, mainHandler);
        });

        dialog.show();
    }

    BiometricPromptCompatDialog getAuthenticateDialogForFragment(
            @Nullable BiometricPromptCompat.ICryptoObject crypto,
            @Nullable CancellationSignal cancel,
            @NonNull BiometricPromptCompat.IAuthenticationCallback callback
    ) {
        if (dialog.isShowing()) {
            Log.e(BiometricPromptCompat.TAG, "BiometricPrompt has been started.");
        }
        this.cancellationSignal = cancel;
        this.callback = callback;
        if (cancellationSignal == null) {
            cancellationSignal = new CancellationSignal();
        }
        CancellationSignal innerCancel = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> {
            innerCancel.cancel();
            dialog.cancel();
        });

        dialog.setOnShowListener(d -> {
            dialog.getFingerprintIcon().setState(
                    FingerprintIconView.State.ON, false);
            fingerprintManager.authenticate(
                    toCryptoObjectApi23(crypto), innerCancel,
                    0, fmAuthCallback, mainHandler);
        });

        return dialog;
    }

    private static FingerprintManager.CryptoObject toCryptoObjectApi23(
            BiometricPromptCompat.ICryptoObject ico) {
        if (ico == null) {
            return null;
        } else if (ico.getCipher() != null) {
            return new FingerprintManager.CryptoObject(ico.getCipher());
        } else if (ico.getMac() != null) {
            return new FingerprintManager.CryptoObject(ico.getMac());
        } else if (ico.getSignature() != null) {
            return new FingerprintManager.CryptoObject(ico.getSignature());
        } else {
            throw new IllegalArgumentException("ICryptoObject doesn\'t include any data.");
        }
    }

    private static class CryptoObjectApi23Impl implements BiometricPromptCompat.ICryptoObject {

        private final FingerprintManager.CryptoObject cryptoObject;

        CryptoObjectApi23Impl(FingerprintManager.CryptoObject cryptoObject) {
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

    private class AnimateHandler extends Handler {

        static final int WHAT_RESTORE_NORMAL_STATE = 0;

        AnimateHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_RESTORE_NORMAL_STATE:
                    dialog.getFingerprintIcon().setState(FingerprintIconView.State.ON);
                    dialog.getStatus().setText(R.string.touch_fingerprint_sensor_hint);
                    break;
            }
        }

    }

    private class FingerprintManagerAuthenticationCallbackImpl
            extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            animateHandler.removeMessages(AnimateHandler.WHAT_RESTORE_NORMAL_STATE);
            if (errString != null) {
                dialog.getStatus().setText(errString);
            }
            dialog.getFingerprintIcon().setState(FingerprintIconView.State.ERROR);
            animateHandler.sendEmptyMessageDelayed(AnimateHandler.WHAT_RESTORE_NORMAL_STATE, 2000);
            callback.onAuthenticationError(errorCode, errString);
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            if (helpString != null) {
                dialog.getStatus().setText(helpString);
            }
            dialog.getFingerprintIcon().setState(FingerprintIconView.State.ON);
            callback.onAuthenticationHelp(helpCode, helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            mainHandler.post(dialog::dismiss);
            callback.onAuthenticationSucceeded(() ->
                    new CryptoObjectApi23Impl(result.getCryptoObject()));
        }

        @Override
        public void onAuthenticationFailed() {
            animateHandler.removeMessages(AnimateHandler.WHAT_RESTORE_NORMAL_STATE);
            dialog.getFingerprintIcon().setState(FingerprintIconView.State.ERROR);
            dialog.getStatus().setText(R.string.not_recognized_fingerprint_hint);
            animateHandler.sendEmptyMessageDelayed(AnimateHandler.WHAT_RESTORE_NORMAL_STATE, 2000);
            callback.onAuthenticationFailed();
        }
    }

}
