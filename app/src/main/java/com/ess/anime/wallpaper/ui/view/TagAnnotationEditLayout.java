package com.ess.anime.wallpaper.ui.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.UIUtils;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TagAnnotationEditLayout extends FrameLayout {

    private final static int ANNOTATION_MAX_LENGTH = 100;

    @BindView(R.id.et_annotation)
    EditText mEtAnnotation;
    @BindView(R.id.tv_limit)
    TextView mTvLimit;

    private boolean mIsEditing = true;
    private OnEditModeChangeListener mOnEditModeChangeListener;

    public TagAnnotationEditLayout(Context context) {
        super(context);
    }

    public TagAnnotationEditLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TagAnnotationEditLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        initEditView();
        exitEditMode();
    }

    private void initEditView() {
        InputFilter.LengthFilter lengthFilter = new InputFilter.LengthFilter(ANNOTATION_MAX_LENGTH);
        InputFilter customFilter = (source, start, end, dest, dstart, dend) -> {
            return source.toString().replaceAll("[\n\r]", "");
        };
        mEtAnnotation.setFilters(new InputFilter[]{lengthFilter, customFilter});

        mEtAnnotation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTvLimit.setText(mEtAnnotation.getText().length() + "/" + ANNOTATION_MAX_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setAnnotation(String annotation) {
        mEtAnnotation.setText(annotation.trim());
    }

    public String getAnnotation() {
        return mEtAnnotation.getText().toString().trim();
    }

    public void showSoftInput() {
        UIUtils.showSoftInput(getContext(), mEtAnnotation);
    }

    public void closeSoftInput() {
        if (getContext() instanceof Activity) {
            UIUtils.closeSoftInput((Activity) getContext());
        }
    }

    public void enterEditMode() {
        if (!isEditing()) {
            mIsEditing = true;
            setActivated(true);
            mEtAnnotation.setHint(R.string.dialog_tag_annotation_edit_hint);
            mEtAnnotation.setEnabled(true);
            mEtAnnotation.requestFocus();
            mEtAnnotation.setSelection(mEtAnnotation.getText().length());
            mTvLimit.setVisibility(View.VISIBLE);
            showSoftInput();
        }
        if (mOnEditModeChangeListener != null) {
            mOnEditModeChangeListener.onEditModeChanged(isEditing());
        }
    }

    public void exitEditMode() {
        if (isEditing()) {
            mIsEditing = false;
            setActivated(false);
            mEtAnnotation.setHint(R.string.favorite_tag_annotation_empty);
            mEtAnnotation.setEnabled(false);
            mEtAnnotation.clearFocus();
            mTvLimit.setVisibility(View.INVISIBLE);
            closeSoftInput();
        }
        if (mOnEditModeChangeListener != null) {
            mOnEditModeChangeListener.onEditModeChanged(isEditing());
        }
    }

    public boolean isEditing() {
        return mIsEditing;
    }

    public void setOnEditModeChangeListener(OnEditModeChangeListener listener) {
        mOnEditModeChangeListener = listener;
    }

    public interface OnEditModeChangeListener {
        void onEditModeChanged(boolean isEditing);
    }
}
