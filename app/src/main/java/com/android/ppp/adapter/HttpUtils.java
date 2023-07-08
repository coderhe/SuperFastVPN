package com.android.ppp.adapter;

import android.util.Log;

import org.json.JSONObject;

import okhttp3.*;
import okhttp3.Response;
import okio.Buffer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpUtils
{
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final MediaType JSONType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient().newBuilder().connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .build();

    public static void post(String url, RequestBody body, okhttp3.Callback callback)
    {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static Response get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        return client.newCall(request).execute();
    }

    private static String canonicalize(String input, int pos, int limit, String encodeSet, boolean strict, boolean plusIsSpace, boolean asciiOnly)
    {
        int codePoint;
        for(int i = pos; i < limit; i += Character.charCount(codePoint)) {
            codePoint = input.codePointAt(i);
            if (codePoint < 32 || codePoint == 127 || codePoint >= 128 && asciiOnly || encodeSet.indexOf(codePoint) != -1 || codePoint == 37 && (strict) || codePoint == 43 && plusIsSpace)
            {
                Buffer out = new Buffer();
                out.writeUtf8(input, pos, i);
                canonicalize(out, input, i, limit, encodeSet, strict, plusIsSpace, asciiOnly);
                return out.readUtf8();
            }
        }

        return input.substring(pos, limit);
    }

    private static void canonicalize(Buffer out, String input, int pos, int limit, String encodeSet, boolean strict, boolean plusIsSpace, boolean asciiOnly)
    {
        Buffer utf8Buffer = null;
        int codePoint;
        for(int i = pos; i < limit; i += Character.charCount(codePoint))
        {
            codePoint = input.codePointAt(i);
            if (codePoint != 9 && codePoint != 10 && codePoint != 12 && codePoint != 13)
            {
                if (codePoint == 43 && plusIsSpace)
                {
                    out.writeUtf8("%2B");
                }
                else if (codePoint >= 32 && codePoint != 127 && (codePoint < 128 || !asciiOnly) && encodeSet.indexOf(codePoint) == -1)
                {
                    out.writeUtf8CodePoint(codePoint);
                }
                else {
                    if (utf8Buffer == null) {
                        utf8Buffer = new Buffer();
                    }

                    utf8Buffer.writeUtf8CodePoint(codePoint);

                    while(!utf8Buffer.exhausted()) {
                        int b = utf8Buffer.readByte() & 255;
                        out.writeByte(37);
                        out.writeByte(HEX_DIGITS[b >> 4 & 15]);
                        out.writeByte(HEX_DIGITS[b & 15]);
                    }
                }
            }
        }
    }

    private static String Canonicalize(String input, String encodeSet, boolean strict, boolean plusIsSpace, boolean asciiOnly)
    {
        return canonicalize(input, 0, input.length(), encodeSet, strict, plusIsSpace, asciiOnly);
    }

    public static RequestBody OKHttpPost(JSONObject params)
    {
        String urlencode = Canonicalize(params.toString(), " \"':;<=>@[]^`{}|/\\?#&!$(),~", false, true, true);
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), "json=" + urlencode);
        Log.e("OKHttpPost", urlencode);

        return body;
        /*
        try
        {
            response = HttpUtils.post(url, body);
            //解析返回的json数据
            result = response.body().string();
            Log.e("SIGNUP_POST", result);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
         */
    }
}