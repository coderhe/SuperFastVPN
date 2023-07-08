package com.android.ppp.adapter;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

public class PingNet
{
    private static final String TAG = "PingNet";

    /**
     * @param pingNetEntity 检测网络实体类
     * @return 检测后的数据
     */
    public static PingNetEntity ping(PingNetEntity pingNetEntity)
    {
        String line;
        Process process = null;
        BufferedReader successReader = null;
        //ping -c 次数 -w 超时时间（s） ip
        String command = "ping -c " + pingNetEntity.getPingCount() + " -w " + pingNetEntity.getPingWtime() + " " + pingNetEntity.getIp();
        try {
            process = Runtime.getRuntime().exec(command);
            if (process == null)
            {
                Log.e(TAG, "ping fail:process is null.");
                append(pingNetEntity.getResultBuffer(), "ping fail:process is null.");
                pingNetEntity.setPingTime(null);
                pingNetEntity.setResult(false);
                return pingNetEntity;
            }
            successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int count = 0;
            BigDecimal sum = new BigDecimal(0);
            while ((line = successReader.readLine()) != null)
            {
                Log.e(TAG, line);
                append(pingNetEntity.getResultBuffer(), line);
                BigDecimal time = getTime(line);
                if (time != null) {
                    sum = sum.add(time);
                    count++;
                }
            }
            //时间取平均值，四舍五入保留两位小数
            if (count > 0)
                pingNetEntity.setPingTime((sum.divide(new BigDecimal(count), 2, BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toPlainString()));
            else
                pingNetEntity.setPingTime(null);

            int status = process.waitFor();
            if (status == 0) {
                Log.e(TAG, "exec cmd success:" + command);
                append(pingNetEntity.getResultBuffer(), "exec cmd success:" + command);
                pingNetEntity.setResult(true);
            } else {
                append(pingNetEntity.getResultBuffer(), "exec cmd fail.");
                //pingNetEntity.setPingTime(null);
                pingNetEntity.setResult(false);
            }
            Log.e(TAG, "exec finished.");
            append(pingNetEntity.getResultBuffer(), "exec finished.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Log.e(TAG, "ping exit.");
            if (process != null) {
                process.destroy();
            }
            if (successReader != null) {
                try {
                    successReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e(TAG, pingNetEntity.getResultBuffer().toString());
        return pingNetEntity;
    }

    private static void append(StringBuffer stringBuffer, String text)
    {
        if (stringBuffer != null)
            stringBuffer.append(text + "\n");
    }

    /**
     * 获取ping接口耗时
     * @param lineInfo
     * @return BigDecimal避免float、double精准度问题
     */
    private static BigDecimal getTime(String lineInfo)
    {
        String[] lines = lineInfo.split("\n");
        String time = null;
        for (String line : lines)
        {
            if (!line.contains("time="))
                continue;

            int index = line.indexOf("time=");
            time = line.substring(index + "time=".length());
            index = time.indexOf("ms");
            time = time.substring(0, index);

            //int index = line.indexOf("time");
            //int index1 = line.indexOf("ms");
            //time = line.substring(index + "time ".length(), index1);
            //time = time.substring(0, index);
            Log.e(TAG, time);
        }

        return time == null ? null : new BigDecimal(time.trim());
    }
}