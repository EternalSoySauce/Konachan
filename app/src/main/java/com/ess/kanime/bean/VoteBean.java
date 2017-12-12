package com.ess.kanime.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class VoteBean implements Parcelable {

    protected VoteBean(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VoteBean> CREATOR = new Creator<VoteBean>() {
        @Override
        public VoteBean createFromParcel(Parcel in) {
            return new VoteBean(in);
        }

        @Override
        public VoteBean[] newArray(int size) {
            return new VoteBean[size];
        }
    };
}
