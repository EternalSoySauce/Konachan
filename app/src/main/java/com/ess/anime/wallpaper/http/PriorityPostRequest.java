package com.ess.anime.wallpaper.http;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.Nullable;

public class PriorityPostRequest extends PriorityStringRequest {

    private final static String BOUNDARY = "---------" + UUID.randomUUID().toString();
    private final static String MULTIPART_FORM_DATA = "multipart/form-data";

    private Map<String, String> mBodyMap = new LinkedHashMap<>();

    public PriorityPostRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
    }

    public void addPostBody(Map<String, String> bodyMap) {
        mBodyMap.putAll(bodyMap);
    }

    @Override
    public String getBodyContentType() {
        return MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        if (mBodyMap == null || mBodyMap.size() == 0) {
            return super.getBody();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (Map.Entry<String, String> entry : mBodyMap.entrySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append("--").append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;");
            sb.append("name=\"");
            sb.append(entry.getKey());
            sb.append("\"");
            sb.append("\r\n");
            sb.append("\r\n");
            sb.append(entry.getValue());
            sb.append("\r\n");
            try {
                bos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String endLine = "--" + BOUNDARY + "--" + "\r\n";
        try {
            bos.write(endLine.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }
}
