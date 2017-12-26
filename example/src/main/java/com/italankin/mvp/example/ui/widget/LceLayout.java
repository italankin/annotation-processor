package com.italankin.mvp.example.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.italankin.mvp.example.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class LceLayout extends FrameLayout {

    public static final int LAYER_CONTENT = 0;
    public static final int LAYER_EMPTY = 1;
    public static final int LAYER_ERROR = 2;
    public static final int LAYER_LOADING = 3;

    private SparseArray<View> mAddedViews = new SparseArray<>(0);
    private int mVisibleLayer = LAYER_CONTENT;

    private StubView empty;
    private StubView error;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public LceLayout(Context context) {
        this(context, null, 0);
    }

    public LceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public API
    ///////////////////////////////////////////////////////////////////////////

    public void showLoading() {
        showLayer(LAYER_LOADING);
    }

    public void showContent() {
        showLayer(LAYER_CONTENT);
    }

    @CheckResult(suggest = "show()")
    public Builder error() {
        return new Builder(error, LAYER_ERROR);
    }

    @CheckResult(suggest = "show()")
    public Builder empty() {
        return new Builder(empty, LAYER_EMPTY);
    }

    public View getVisibleLayerView() {
        return mAddedViews.get(mVisibleLayer);
    }

    public int getVisibleLayer() {
        return mVisibleLayer;
    }

    @NonNull
    public View getLoadingView() {
        return mAddedViews.get(LAYER_LOADING);
    }

    @NonNull
    public View getErrorView() {
        return mAddedViews.get(LAYER_ERROR);
    }

    @NonNull
    public View getEmptyView() {
        return mAddedViews.get(LAYER_EMPTY);
    }

    @NonNull
    public View getContentView() {
        return mAddedViews.get(LAYER_CONTENT);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Builder
    ///////////////////////////////////////////////////////////////////////////

    public final class Builder {
        private final StubView stub;
        private final Context context;
        private final int layer;

        private CharSequence message;
        private Drawable icon;
        private CharSequence buttonTitle;
        private OnClickListener buttonClickListener;

        Builder(StubView stub, int layer) {
            this.stub = stub;
            this.context = stub.getContext();
            this.layer = layer;
        }

        @CheckResult(suggest = "show()")
        public Builder message(@Nullable CharSequence message) {
            this.message = message;
            return this;
        }

        @CheckResult(suggest = "show()")
        public Builder message(@StringRes int message) {
            return message(context.getText(message));
        }

        @CheckResult(suggest = "show()")
        public Builder icon(Drawable icon) {
            this.icon = icon;
            return this;
        }

        @CheckResult(suggest = "show()")
        public Builder icon(@DrawableRes int icon) {
            return icon(ContextCompat.getDrawable(context, icon));
        }

        @CheckResult(suggest = "show()")
        public Builder reload(@Nullable CharSequence buttonTitle, @Nullable OnClickListener listener) {
            this.buttonTitle = buttonTitle;
            this.buttonClickListener = listener;
            return this;
        }

        @CheckResult(suggest = "show()")
        public Builder reload(@Nullable OnClickListener listener) {
            return reload(null, listener);
        }

        @CheckResult(suggest = "show()")
        public Builder reload(@StringRes int buttonTitle, @Nullable OnClickListener listener) {
            return reload(context.getText(buttonTitle), listener);
        }

        public void show() {
            stub.setMessage(message, icon);
            stub.setRetryButtonClickListener(buttonTitle, buttonClickListener);
            showLayer(layer);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////

    private void setup() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        addLayer(LAYER_ERROR, error = new StubView(inflater));
        addLayer(LAYER_EMPTY, empty = new StubView(inflater));
        addLayer(LAYER_LOADING, inflater.inflate(R.layout.state_loading, this, false));
    }

    private void showLayer(@Layer int layer) {
        if (layer == mVisibleLayer) {
            return;
        }
        final View entering = mAddedViews.get(layer);
        mVisibleLayer = layer;
        bringChildToFront(entering);
    }

    private void syncLayerVisibility() {
        View child;
        LayoutParams lp;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            lp = (LayoutParams) child.getLayoutParams();
            child.setVisibility(mVisibleLayer == lp.layer ? VISIBLE : GONE);
        }
    }

    private void addLayer(@Layer int layer, @NonNull View child) {
        LayoutParams lp = new LayoutParams(layer);
        addView(child, -1, lp);
    }

    private void replaceLayer(@Layer int layer, @NonNull View child) {
        if (mAddedViews.indexOfKey(layer) >= 0) {
            View view = mAddedViews.get(layer);
            mAddedViews.remove(layer);
            removeView(view);
        }
        addLayer(layer, child);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        LayoutParams lp = (LayoutParams) params;
        if (mAddedViews.indexOfKey(lp.layer) >= 0) {
            throw new IllegalArgumentException("Duplicate layer: " + layerName(lp.layer));
        }
        if (mAddedViews.size() >= 4) {
            throw new AssertionError("LceLayout can hold up to 4 children");
        }
        mAddedViews.put(lp.layer, child);
        if (mVisibleLayer == lp.layer) {
            child.setVisibility(VISIBLE);
        } else {
            child.setVisibility(GONE);
        }
        index = Math.min(lp.layer, getChildCount());
        super.addView(child, index, lp);
        syncLayerVisibility();
    }

    ///////////////////////////////////////////////////////////////////////////
    // LayoutParams
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public class LayoutParams extends FrameLayout.LayoutParams {
        public int layer;

        public LayoutParams() {
            this(LAYER_CONTENT);
        }

        public LayoutParams(@Layer int layer) {
            super(MATCH_PARENT, MATCH_PARENT);
            this.layer = layer;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            layer = ((LayoutParams) source).layer;
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.LceLayout_Layout);
            try {
                layer = a.getInt(R.styleable.LceLayout_Layout_layout_layer, LAYER_CONTENT);
            } finally {
                a.recycle();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // State
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState saved = (SavedState) state;
        super.onRestoreInstanceState(saved.getSuperState());
        mVisibleLayer = saved.visibleLayer;
        syncLayerVisibility();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        SavedState state = new SavedState(parcelable);
        state.visibleLayer = mVisibleLayer;
        return state;
    }

    public static class SavedState extends BaseSavedState {
        int visibleLayer;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel in) {
            super(in);
            this.visibleLayer = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(visibleLayer);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility
    ///////////////////////////////////////////////////////////////////////////

    private static String layerName(@Layer int layer) {
        switch (layer) {
            case LAYER_CONTENT:
                return "LAYER_CONTENT";
            case LAYER_EMPTY:
                return "LAYER_EMPTY";
            case LAYER_ERROR:
                return "LAYER_ERROR";
            case LAYER_LOADING:
                return "LAYER_LOADING";
        }
        throw new IllegalArgumentException("Invalid layer index: " + layer);
    }

    @IntDef({
            LAYER_CONTENT,
            LAYER_EMPTY,
            LAYER_ERROR,
            LAYER_LOADING,
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface Layer {
    }

    private static class StubView extends LinearLayout {

        private final TextView text;
        private final Button button;

        StubView(LayoutInflater inflater) {
            super(inflater.getContext());
            setGravity(Gravity.CENTER);
            setOrientation(VERTICAL);
            inflater.inflate(R.layout.state_stub, this, true);
            text = findViewById(R.id.text);
            button = findViewById(R.id.button);
        }

        void setMessage(@Nullable CharSequence message, @Nullable Drawable icon) {
            text.setText(message);
            text.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        }

        void setRetryButtonClickListener(@Nullable CharSequence title, @Nullable OnClickListener listener) {
            if (title != null) {
                button.setText(title);
            }
            button.setOnClickListener(listener);
            button.setVisibility(listener != null ? VISIBLE : INVISIBLE);
        }
    }

}
