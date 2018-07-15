package com.lss.anime.wallpaper.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.lss.anime.wallpaper.bean.ApkBean;
import com.lss.anime.wallpaper.bean.MsgBean;
import com.lss.anime.wallpaper.bean.UserBean;
import com.lss.anime.wallpaper.global.Constants;
import com.lss.anime.wallpaper.utils.ComponentUtils;
import com.lss.anime.wallpaper.utils.FileUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class FireBase {

    private static class FirebaseHolder {
        private static final FireBase instance = new FireBase();
    }

    public static FireBase getInstance() {
        return FirebaseHolder.instance;
    }

    public final static String UPDATE_FILE_NAME = "latest_version";
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef = mStorage.getReference();
    private StreamDownloadTask mCheckUpdateTask;

    private FirebaseFirestore mDatabase = FirebaseFirestore.getInstance();
    private Task<DocumentSnapshot> mCheckUserTask;
    private Task<Void> mAddUserTask;

    private Context mContext = mStorage.getApp().getApplicationContext();
    private SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(mContext);

    private FireBase() {
    }

    public void checkUpdate() {
        cancelCheckUpdate();

        StorageReference islandRef = mStorageRef.child(UPDATE_FILE_NAME);
        mCheckUpdateTask = islandRef.getStream();
        mCheckUpdateTask.addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final StreamDownloadTask.TaskSnapshot taskSnapshot) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String json = FileUtils.streamToString(taskSnapshot.getStream());
                        FileUtils.stringToFile(json, new File(mContext.getExternalFilesDir(null), UPDATE_FILE_NAME));
                        ApkBean apkBean = ApkBean.getApkDetailFromJson(mContext, json);
                        if (apkBean.versionCode > ComponentUtils.getVersionCode(mContext)) {
                            // 发送通知到 MainActivity
                            EventBus.getDefault().postSticky(new MsgBean(Constants.CHECK_UPDATE, apkBean));
                        }
                    }
                }).start();
            }
        })/*.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof StorageException
                        && ((StorageException) exception).getErrorCode() == StorageException.ERROR_CANCELED) {
                    return;
                }
            }
        })*/;
    }

    private void cancelCheckUpdate() {
        if (mCheckUpdateTask != null) {
            mCheckUpdateTask.cancel();
        }
    }

    public void checkToAddUser() {
        if (mCheckUserTask != null || mAddUserTask != null
                || mPreference.getBoolean(Constants.ALREADY_ADD_USER, false)) {
            return;
        }

        UserBean user = new UserBean(mContext);
        String docId = FileUtils.encodeMD5String(user.id);
        DocumentReference docRef = mDatabase.collection("users").document(docId);
        checkUser(docRef, user);
    }

    private void checkUser(final DocumentReference docRef, final UserBean user) {
        mCheckUserTask = docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document == null || !document.exists()) {
                        addUser(docRef, user);
                    }else {
                        mPreference.edit().putBoolean(Constants.ALREADY_ADD_USER, true).apply();
                    }
                }
            }
        });
    }

    private void addUser(DocumentReference docRef, UserBean user) {
        mAddUserTask = docRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mPreference.edit().putBoolean(Constants.ALREADY_ADD_USER, true).apply();
            }
        });
    }

    public void cancelAll() {
        cancelCheckUpdate();
    }
}
