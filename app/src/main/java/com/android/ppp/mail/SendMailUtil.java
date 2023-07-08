package com.android.ppp.mail;

import androidx.annotation.NonNull;
import com.android.ppp.adapter.BaseApplication;
import java.io.File;

public class SendMailUtil
{
    public static void send(final File file, String toAddress, String sContent)
    {
        final MailInfo mailInfo = creatMail(toAddress, sContent);
        final MailSender sms = new MailSender();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                sms.sendFileMail(mailInfo, file);
            }
        }).start();
    }

    public static void send(String toAddress, String sContent)
    {
        final MailInfo mailInfo = creatMail(toAddress, sContent);
        final MailSender sms = new MailSender();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                sms.sendTextMail(mailInfo);
            }
        }).start();
    }

    @NonNull
    private static MailInfo creatMail(String toAddress, String sContent)
    {
        String HOST = ShareUtils.getString(BaseApplication.GetInstance(), "HOST", "");
        String PORT = ShareUtils.getString(BaseApplication.GetInstance(), "PORT", "");
        String FROM_ADD = ShareUtils.getString(BaseApplication.GetInstance(), "FROM_ADD", "");
        String FROM_PSW = ShareUtils.getString(BaseApplication.GetInstance(), "FROM_PSW", "");
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);//发送方邮箱服务器
        mailInfo.setMailServerPort(PORT);//发送方邮箱端口号
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // 发送者邮箱地址
        mailInfo.setPassword(FROM_PSW);// 发送者邮箱授权码
        mailInfo.setFromAddress(FROM_ADD); // 发送者邮箱
        mailInfo.setToAddress(toAddress); // 接收者邮箱
        mailInfo.setSubject("星空互联"); // 邮件主题
        mailInfo.setContent("尊敬的用户：\n   感谢您注册星空互联！\n   您的验证码是：" + sContent); // 邮件文本
        return mailInfo;
    }
}