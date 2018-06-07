package moe.feng.support.biometricprompt.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import moe.feng.support.biometricprompt.BiometricPromptCompat;

public class MainActivity extends Activity
        implements BiometricPromptCompat.IAuthenticationCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button authButton = findViewById(R.id.btn_auth);
        authButton.setOnClickListener(v -> startAuth());

        final TextView isHardwareDetectedText = findViewById(R.id.is_hardware_detected_text);
        final TextView hasEnrolledFingerprintsText =
                findViewById(R.id.has_enrolled_fingerprints_text);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            isHardwareDetectedText.setText(R.string.is_hardware_detected_text_api_too_old);
        } else {
            isHardwareDetectedText.setText(
                    BiometricPromptCompat.isHardwareDetected(this) ?
                            R.string.is_hardware_detected_text_true :
                            R.string.is_hardware_detected_text_false);
        }

        hasEnrolledFingerprintsText.setText(
                getString(R.string.has_enrolled_fingerprints_text_format,
                        Boolean.toString(
                                BiometricPromptCompat.hasEnrolledFingerprints(this)
                        )
                )
        );
    }

    private void startAuth() {
        final BiometricPromptCompat biometricPrompt =
                new BiometricPromptCompat.Builder(MainActivity.this)
                        .setTitle("Title")
                        .setSubtitle("Subtitle")
                        .setDescription("Description: blablablablablablablablablablabla...")
                        .setNegativeButton("Use password", (dialog, which) -> Toast.makeText(
                                MainActivity.this,
                                "You requested password.",
                                Toast.LENGTH_LONG).show())
                        .build();
        final CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(() -> Toast.makeText(
                MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show());
        biometricPrompt.authenticate(cancellationSignal, this);
    }

    @Override
    public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {
        Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {
        Toast.makeText(MainActivity.this, "help", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            @NonNull BiometricPromptCompat.IAuthenticationResult result) {
        Toast.makeText(MainActivity.this, "succeed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
    }

}
