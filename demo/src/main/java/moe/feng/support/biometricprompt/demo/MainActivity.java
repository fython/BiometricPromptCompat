package moe.feng.support.biometricprompt.demo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import moe.feng.support.biometricprompt.BiometricPromptCompat;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_auth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuth();
            }
        });
    }

    private void startAuth() {
        final BiometricPromptCompat biometricPrompt =
                new BiometricPromptCompat.Builder(MainActivity.this)
                        .setTitle("Title")
                        .setSubtitle("Subtitle")
                        .setDescription("Description: blablablablablablablablablablabla...")
                        .setNegativeButton("Use password", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "You requested password.",
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .build();
        final CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_SHORT).show();
            }
        });
        biometricPrompt.authenticate(cancellationSignal, new BiometricPromptCompat.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @Nullable CharSequence errString) {
                Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, @Nullable CharSequence helpString) {
                Toast.makeText(MainActivity.this, "help", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPromptCompat.IAuthenticationResult result) {
                Toast.makeText(MainActivity.this, "succeed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
