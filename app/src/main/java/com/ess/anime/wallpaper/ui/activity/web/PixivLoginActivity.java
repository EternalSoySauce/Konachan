package com.ess.anime.wallpaper.ui.activity.web;

import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.Toast;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.pixiv.login.PixivLoginManager;
import com.just.agentweb.MiddlewareWebClientBase;

public class PixivLoginActivity extends BaseWebActivity {

    @Override
    CharSequence title() {
        return getString(R.string.pixiv_login_webview_title);
    }

    @Override
    boolean showReceivedTitle() {
        return false;
    }

    @Override
    String webUrl() {
        return "https://app-api.pixiv.net/web/v1/login?code_challenge=NwwB0vuv6RnlTOYurWQYg2QrOqrBeXGKAXjjmpxdMx8&code_challenge_method=S256&client=pixiv-android";
    }

    @Override
    MiddlewareWebClientBase customWebViewClient() {
        return new MiddlewareWebClientBase() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    if (checkLoginCookie(request.getUrl().toString())) {
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (checkLoginCookie(url)) {
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            private boolean checkLoginCookie(String url) {
                if (url != null && url.startsWith("https://app-api.pixiv.net/web/v1/users/auth/pixiv/callback")) {
                    String cookie = CookieManager.getInstance().getCookie(url);
                    if (!TextUtils.isEmpty(cookie)) {
                        PixivLoginManager.getInstance().setCookie(cookie);
                        Toast.makeText(PixivLoginActivity.this, R.string.pixiv_toast_login_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PixivLoginActivity.this, R.string.pixiv_toast_login_failed, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    @Override
    void showHelpDialog() {
    }

    @Override
    boolean hasHelpDialog() {
        return false;
    }
}
