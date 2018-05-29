package com.ess.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map.Entry;

public class TagBean implements Parcelable {

    public ArrayList<String> copyright = new ArrayList<>();  //版权方，#DD00DD

    public ArrayList<String> character = new ArrayList<>();  //角色名，#00AA00

    public ArrayList<String> artist = new ArrayList<>();     //此作品的作者（非官方原作者），#CCCC00

    public ArrayList<String> circle = new ArrayList<>();     //元版权方（非正规版权），#00BBBB

    public ArrayList<String> style = new ArrayList<>();      //独特风格类型（如_vocaloid），#FF2020

    public ArrayList<String> general = new ArrayList<>();    //普通描述，#FFFFFF (#EE8887)

    public TagBean() {
    }

    public TagBean(JsonObject tagArray) {
        for (Entry<String, JsonElement> entry : tagArray.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            switch (value) {
                case "copyright":
                    copyright.add(key);
                    break;
                case "character":
                    character.add(key);
                    break;
                case "artist":
                    artist.add(key);
                    break;
                case "circle":
                    circle.add(key);
                    break;
                case "style":
                    style.add(key);
                    break;
                case "general":
                    general.add(key);
                    break;
            }
        }
    }

    protected TagBean(Parcel in) {
        copyright = in.createStringArrayList();
        character = in.createStringArrayList();
        artist = in.createStringArrayList();
        circle = in.createStringArrayList();
        style = in.createStringArrayList();
        general = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(copyright);
        dest.writeStringList(character);
        dest.writeStringList(artist);
        dest.writeStringList(circle);
        dest.writeStringList(style);
        dest.writeStringList(general);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TagBean> CREATOR = new Creator<TagBean>() {
        @Override
        public TagBean createFromParcel(Parcel in) {
            return new TagBean(in);
        }

        @Override
        public TagBean[] newArray(int size) {
            return new TagBean[size];
        }
    };
}
