package com.lss.anime.wallpaper.bean;

public class CommentBean {

    public String id;
    public String author;
    public String date;
    public String headUrl;
    public CharSequence quote;
    public CharSequence comment;

    public CommentBean(String id, String author, String date, String headUrl, CharSequence quote, CharSequence comment) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.headUrl = headUrl;
        this.quote = quote;
        this.comment = comment;
    }
}
