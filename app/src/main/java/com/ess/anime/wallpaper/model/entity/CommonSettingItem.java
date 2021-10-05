package com.ess.anime.wallpaper.model.entity;

import android.view.View;

import com.ess.anime.wallpaper.MyApp;
import com.mixiaoxiao.smoothcompoundbutton.SmoothCompoundButton;

import androidx.annotation.StringRes;

public class CommonSettingItem {

    private String title;
    private String desc;
    private String tips;
    private boolean checkboxShown;
    private boolean checkboxChecked;
    private SmoothCompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private boolean dividerShown = true;
    private boolean rippleDelayClick = true;
    private View.OnClickListener onClickListener;

    public CommonSettingItem setTitle(@StringRes int titleRes) {
        this.title = MyApp.getInstance().getString(titleRes);
        return this;
    }

    public CommonSettingItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CommonSettingItem setDesc(@StringRes int descRes) {
        this.desc = MyApp.getInstance().getString(descRes);
        return this;
    }

    public CommonSettingItem setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public CommonSettingItem setTips(@StringRes int tipRes) {
        this.tips = MyApp.getInstance().getString(tipRes);
        return this;
    }

    public CommonSettingItem setTips(String tips) {
        this.tips = tips;
        return this;
    }

    public String getTips() {
        return tips;
    }

    public CommonSettingItem setCheckboxShown(boolean shown) {
        this.checkboxShown = shown;
        return this;
    }

    public boolean isCheckboxShown() {
        return checkboxShown;
    }

    public CommonSettingItem setCheckboxChecked(boolean checked) {
        this.checkboxChecked = checked;
        return this;
    }

    public boolean isCheckboxChecked() {
        return checkboxChecked;
    }

    public CommonSettingItem setOnCheckedChangeListener(SmoothCompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
        return this;
    }

    public SmoothCompoundButton.OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public CommonSettingItem setDividerShown(boolean shown) {
        this.dividerShown = shown;
        return this;
    }

    public boolean isDividerShown() {
        return dividerShown;
    }

    public CommonSettingItem setRippleDelayClick(boolean rippleDelayClick) {
        this.rippleDelayClick = rippleDelayClick;
        return this;
    }

    public boolean isRippleDelayClick() {
        return rippleDelayClick;
    }

    public CommonSettingItem setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

}
