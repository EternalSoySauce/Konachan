package com.ess.anime.wallpaper.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.ess.anime.wallpaper.download.image.DownloadBeanDao;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

public class MyDevOpenHelper extends DaoMaster.DevOpenHelper {

    public MyDevOpenHelper(Context context, String name) {
        super(context, name);
    }

    public MyDevOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        MigrationHelper.migrate(db, SearchTagBeanDao.class, DownloadBeanDao.class);
    }

}