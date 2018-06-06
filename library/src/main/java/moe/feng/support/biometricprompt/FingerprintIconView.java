package moe.feng.support.biometricprompt;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.RestrictTo;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

@RestrictTo({RestrictTo.Scope.LIBRARY})
class FingerprintIconView extends ImageView {

    // Keep in sync with attrs.
    public enum State {
        OFF,
        ON,
        ERROR,
    }

    private State state = State.OFF;

    public FingerprintIconView(Context context) {
        this(context, null);
    }

    public FingerprintIconView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setState(State.OFF, false);
    }

    public void setState(State state) {
        setState(state, true);
    }

    public void setState(State state, boolean animate) {
        if (state == this.state) return;

        @DrawableRes int resId = getDrawable(this.state, state, animate);
        if (resId == 0) {
            setImageDrawable(null);
        } else {
            Drawable icon = null;
            if (animate) {
                icon = AnimatedVectorDrawableCompat.create(getContext(), resId);
            }
            if (icon == null) {
                icon = VectorDrawableCompat.create(getResources(), resId, getContext().getTheme());
            }
            setImageDrawable(icon);

            if (icon instanceof Animatable) {
                ((Animatable) icon).start();
            }
        }

        this.state = state;
    }

    @DrawableRes
    private static int getDrawable(State currentState, State newState, boolean animate) {
        switch (newState) {
            case OFF:
                if (animate) {
                    if (currentState == State.ON) {
                        return R.drawable.fingerprint_draw_off_animation;
                    } else if (currentState == State.ERROR) {
                        return R.drawable.fingerprint_error_off_animation;
                    }
                }

                return 0;
            case ON:
                if (animate) {
                    if (currentState == State.OFF) {
                        return R.drawable.fingerprint_draw_on_animation;
                    } else if (currentState == State.ERROR) {
                        return R.drawable.fingerprint_error_state_to_fp_animation;
                    }
                }

                return R.drawable.fingerprint_fingerprint;
            case ERROR:
                if (animate) {
                    if (currentState == State.ON) {
                        return R.drawable.fingerprint_fp_to_error_state_animation;
                    } else if (currentState == State.OFF) {
                        return R.drawable.fingerprint_error_on_animation;
                    }
                }

                return R.drawable.fingerprint_error;
            default:
                throw new IllegalArgumentException("Unknown state: " + newState);
        }
    }
}
