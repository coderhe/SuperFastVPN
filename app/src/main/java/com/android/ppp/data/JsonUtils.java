package com.android.ppp.data;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class JsonUtils
{
    /*修改json数据*/
    public static void ModifyElement(String path, String firstKey, String key, Object value)
    {
        try {
            String jsonStr = ReadJsonFile(path);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject jsonObject = json.getJSONObject(firstKey);
            jsonObject.put(key, value);

            //将json转换为json字符串
            WriteJsonFile(json.toString(), path);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /*修改json数据*/
    public static void ModifyElement(String path, String firstKey, String secondKey, String key, Object value)
    {
        try {
            String jsonStr = ReadJsonFile(path);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject jsonObject = json.getJSONObject(firstKey);
            JSONObject jsonObject1 = jsonObject.getJSONObject(secondKey);
            jsonObject1.put(key, value);

            //将json转换为json字符串
            WriteJsonFile(json.toString(), path);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /*获取json数据*/
    public static Object GetElementValue(String path, String firstKey, String key)
    {
        try {
            String jsonStr = ReadJsonFile(path);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject jsonObject = json.getJSONObject(firstKey);

            return jsonObject.get(key);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    /*获取json数据*/
    public static Object GetElementValue(String path, String firstKey, String secondKey, String key)
    {
        try {
            String jsonStr = ReadJsonFile(path);
            JSONObject json = new JSONObject(jsonStr);
            JSONObject jsonObject = json.getJSONObject(firstKey);
            JSONObject jsonObject1 = jsonObject.getJSONObject(secondKey);
            return jsonObject1.get(key);
        }
        catch (JSONException e)
        {
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void DeleteElement(String path, String id)
    {
        try
        {
            String jsonStr = ReadJsonFile(path);
            JSONArray jsonArray = new JSONArray(jsonStr);
            for(int  i = 0; i < jsonArray.length(); i++)
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
            }

            //将json转换为json字符串
            WriteJsonFile(jsonArray.toString(), path);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**写入json文件**/
    public static void WriteJsonFile(String newJsonString, String path)
    {
        try
        {
            FileWriter fw = new FileWriter(path);
            PrintWriter out = new PrintWriter(fw);
            out.write(newJsonString);
            out.println();
            fw.close();
            out.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**读取json文件**/
    public static String ReadJsonFile(String path)
    {
        String datas = "";
        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(new File(path)));
            String temp = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((temp = reader.readLine()) != null)
            {
                datas = datas + temp;
                line++;
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return datas;
    }

    public static String ReadAssetJson(Context context, String fileName)
    {
        // 将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        // 使用IO流读取json文件内容
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    context.getAssets().open(fileName), "utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line.trim());
                stringBuilder.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}