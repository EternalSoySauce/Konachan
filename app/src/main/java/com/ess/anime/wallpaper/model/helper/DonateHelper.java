package com.ess.anime.wallpaper.model.helper;

import android.app.Activity;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.BitmapUtils;

import java.io.File;
import java.io.InputStream;

public class DonateHelper {

    /**
     * 支付宝支付
     */
    public static void donateByAlipay(Activity activity) {
        if (AlipayDonate.hasInstalledAlipayClient(activity)) {
            // 收款二维码里面的字符串，如 https://qr.alipay.com/stx00187oxldjvyo3ofaw60 ，
            // 则payCode = stx00187oxldjvyo3ofaw60
            String payCode = "tsx08108dsd2cunrfgqsr64";
            AlipayDonate.startAlipayClient(activity, payCode);
        } else {
            Toast.makeText(activity, "未安装支付宝", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     */
    public static void donateByWechat(Activity activity) {
        InputStream inputStream = activity.getResources().openRawResource(R.raw.donate_wechat);
        File qrImage = new File(Constants.IMAGE_DONATE, "donate_wechat.png");
        String qrPath = qrImage.getAbsolutePath();
        if (!qrImage.exists()) {
            WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(inputStream));
            BitmapUtils.insertToMediaStore(activity, qrImage);
        }
        WeiXinDonate.donateViaWeiXin(activity, qrPath);
    }

}
