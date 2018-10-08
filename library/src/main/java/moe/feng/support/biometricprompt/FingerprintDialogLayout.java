package moe.feng.support.biometricprompt;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

@RestrictTo({RestrictTo.Scope.LIBRARY})
public class FingerprintDialogLayout extends LinearLayout {

    private FingerprintIconView iconView;
    private TextView statusView;

    public FingerprintDialogLayout(Context context) {
        super(context);
    }

    public FingerprintDialogLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FingerprintDialogLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        iconView = findViewById(R.id.fingerprint_icon);
        statusView = findViewById(R.id.status);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int newMargin = getResources().getDimensionPixelSize(R.dimen.fingerprint_status_layout_margin_vertical);
        ((MarginLayoutParams) iconView.getLayoutParams()).topMargin = newMargin;
        ((MarginLayoutParams) statusView.getLayoutParams()).bottomMargin = newMargin;
    }
}
