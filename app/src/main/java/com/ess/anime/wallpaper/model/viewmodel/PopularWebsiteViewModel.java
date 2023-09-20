package com.ess.anime.wallpaper.model.viewmodel;

import com.ess.anime.wallpaper.utils.DateUtils;

import java.util.Calendar;
import java.util.Objects;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PopularWebsiteViewModel extends ViewModel {

    private final MutableLiveData<Calendar> mCalenderData = new MutableLiveData<>(Calendar.getInstance());

    public LiveData<Calendar> getCalenderLiveData() {
        return mCalenderData;
    }

    public void setCalenderData(Calendar calendar) {
        mCalenderData.setValue(calendar);
    }

    public void setCalenderData(int year, int month, int day) {
        if (getCalenderYear() != year || getCalenderMonth() != month || getCalenderDay() != day) {
            Calendar calendar = DateUtils.getCalendarByData(year, month + 1, day);
            mCalenderData.setValue(calendar);
        }
    }

    public int getCalenderYear() {
        return Objects.requireNonNull(mCalenderData.getValue()).get(Calendar.YEAR);
    }

    public int getCalenderMonth() {
        return Objects.requireNonNull(mCalenderData.getValue()).get(Calendar.MONTH);
    }

    public int getCalenderDay() {
        return Objects.requireNonNull(mCalenderData.getValue()).get(Calendar.DAY_OF_MONTH);
    }

    public int getRealYear() {
        return getCalenderYear();
    }

    // Calendar月份从0开始，此处需要返回真实月份
    public int getRealMonth() {
        return getCalenderMonth() + 1;
    }

    public int getRealDay() {
        return getCalenderDay();
    }

}
