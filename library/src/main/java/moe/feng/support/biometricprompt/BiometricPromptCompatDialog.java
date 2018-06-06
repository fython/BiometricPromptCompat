package moe.feng.support.biometricprompt;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@RestrictTo({RestrictTo.Scope.LIBRARY})
class BiometricPromptCompatDialog extends Dialog {

    private final TextView mSubtitle, mDescription, mStatus;
    private final Button mNegativeButton;
    private final FingerprintIconView mFingerprintIcon;

    BiometricPromptCompatDialog(@NonNull Context context) {
        super(context, R.style.Theme_BiometricPromptDialog);

        setCancelable(true);
        setCanceledOnTouchOutside(true);

        Window window = getWindow();
        window.setLayout(MATCH_PARENT, WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);

        View rootView = LayoutInflater.from(getContext())
                .inflate(R.layout.biometric_prompt_dialog_content, null);
        mSubtitle = rootView.findViewById(R.id.subtitle);
        mDescription = rootView.findViewById(R.id.description);
        mStatus = rootView.findViewById(R.id.status);
        mNegativeButton = rootView.findViewById(android.R.id.button1);
        mFingerprintIcon = rootView.findViewById(R.id.fingerprint_icon);
        addContentView(rootView, new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
    }

    public TextView getSubtitle() {
        return mSubtitle;
    }

    public TextView getDescription() {
        return mDescription;
    }

    public TextView getStatus() {
        return mStatus;
    }

    public Button getNegativeButton() {
        return mNegativeButton;
    }

    public FingerprintIconView getFingerprintIcon() {
        return mFingerprintIcon;
    }

}
