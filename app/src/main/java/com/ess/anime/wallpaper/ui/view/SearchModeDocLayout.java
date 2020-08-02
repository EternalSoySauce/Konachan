package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.model.helper.DocDataHelper;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchModeDocLayout extends NestedScrollView {

    @BindView(R.id.layout_doc_search_mode)
    LinearLayout mLayoutDocSearchMode;

    public SearchModeDocLayout(@NonNull Context context) {
        super(context);
    }

    public SearchModeDocLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchModeDocLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        List<String> docList = DocDataHelper.getSearchModeDocumentList(getContext());

        TextView tvDocSearchTag = findViewById(R.id.tv_doc_search_tag);
        tvDocSearchTag.setText(docList.get(0));

        TextView tvDocSearchId = findViewById(R.id.tv_doc_search_id);
        tvDocSearchId.setText(docList.get(1));

        TextView tvDocSearchChinese = findViewById(R.id.tv_doc_search_chinese);
        tvDocSearchChinese.setText(docList.get(2));

        TextView tvDocSearchAdvanced = findViewById(R.id.tv_doc_search_advanced);
        tvDocSearchAdvanced.setText(setLinkToShowTagTypeDoc(docList.get(3)));
        tvDocSearchAdvanced.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString setLinkToShowTagTypeDoc(String baseText) {
        SpannableString spanText = new SpannableString(getContext().getString(R.string.click_here, baseText));
        spanText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                CustomDialog.showAdvancedSearchHelpDialog(getContext());
            }
        }, baseText.length(), spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new UnderlineSpan(), baseText.length(),
                spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spanText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.color_link)),
                baseText.length(), spanText.length() - 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spanText;
    }

    public void changeDocumentColor(int selectedPos) {
        for (int i = 0; i < mLayoutDocSearchMode.getChildCount(); i++) {
            TextView tv = (TextView) mLayoutDocSearchMode.getChildAt(i);
            int textColor = i == selectedPos ? R.color.color_text_selected : R.color.color_text_unselected;
            tv.setTextColor(getResources().getColor(textColor));
        }
    }

}
