package com.ess.anime.wallpaper.bean;

public class CommentBean {

    public String id;
    public String author;
    public String date;
    public String avatar;
    public CharSequence quote;
    public CharSequence comment;

    public CommentBean(String id, String author, String date, String avatar, CharSequence quote, CharSequence comment) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.avatar = avatar;
        this.quote = quote;
        this.comment = comment;
    }
}
