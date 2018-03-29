package com.ess.anime.wallpaper.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ess.anime.wallpaper.bean.ApkBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.greenrobot.eventbus.EventBus;

public class FireBase {

    private static class FirebaseHolder {
        private static final FireBase instance = new FireBase();
    }

    public static FireBase getInstance() {
        return FirebaseHolder.instance;
    }

    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = mStorage.getReference();
    private StreamDownloadTask mCheckUpdateTask;

    private Context mContext = mStorage.getApp().getApplicationContext();

    private FireBase() {
    }

    public void checkUpdate() {
        cancelCheckUpdate();

        StorageReference islandRef = mStorageRef.child("latest_version");
        mCheckUpdateTask = islandRef.getStream();
        mCheckUpdateTask.addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final StreamDownloadTask.TaskSnapshot taskSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String json = FileUtils.streamToString(taskSnapshot.getStream());
                        Log.i("rrr", "load txt Success  " + json);
                        ApkBean apkBean = ApkBean.getApkDetailFromJson(json);
                        if (apkBean.versionCode > ComponentUtils.getVersionCode(mContext)) {
                            Log.i("rrr", "has new version  " + apkBean.versionName);
//                    FileUtils.streamToFile(taskSnapshot.getStream(),new File(mContext.getExternalFilesDir(null),"updata"));
                            // 发送通知到 MainActivity
                            EventBus.getDefault().postSticky(new MsgBean(Constants.CHECK_UPDATE, apkBean));
                        }
                    }
                }).start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof StorageException
                        && ((StorageException) exception).getErrorCode() == StorageException.ERROR_CANCELED) {
                    return;
                }
                Log.i("rrr", "load txt failed " + exception.getMessage());
            }
        });
    }

    private void cancelCheckUpdate() {
        if (mCheckUpdateTask != null) {
            mCheckUpdateTask.cancel();
        }
    }

    public void cancelAll() {
        cancelCheckUpdate();
    }
}
