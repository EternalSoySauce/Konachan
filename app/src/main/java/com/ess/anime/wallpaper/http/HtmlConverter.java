package com.ess.anime.wallpaper.http;

import com.yanzhenjie.kalle.Response;
import com.yanzhenjie.kalle.simple.Converter;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import java.lang.reflect.Type;

public class HtmlConverter implements Converter {

    @Override
    public <S, F> SimpleResponse<S, F> convert(Type succeed, Type failed, Response response, boolean fromCache) throws Exception {
        S succeedData = null;
        F failedData = null;

        int code = response.code();
        if (code >= 200 && code < 300) {
            succeedData = (S) response.body().string();
        } else {
            failedData = (F) "Response Failed";
        }

        return SimpleResponse.<S, F>newBuilder()
                .code(response.code())
                .headers(response.headers())
                .fromCache(fromCache)
                .succeed(succeedData)
                .failed(failedData)
                .build();
    }

}
