package com.android.ppp.config;

public class Config
{
    //Print Log
    public static boolean CanLog = true;
    //website
    public static final String STAR_LINK ="https://starrylink.net/";
    //SIGN_IN_URL
    public static final String SIGN_IN_URL = "https://ppp.supersocksr.xyz/api/account/signin"; //http://193.187.119.139/api/account/signin";
    //SIGN_UP_URL
                public static final String SIGN_UP_URL = "https://ppp.supersocksr.xyz/api/account/signup"; //ppp.supersocksr.xyz
    //PAY_URL
    public static final String PAY_URL = "https://ppp.supersocksr.xyz/api/pay/createorder";
    //PAY_QUERY_URL
    public static final String PAY_QUERY_URL = "https://ppp.supersocksr.xyz/api/pay/queryorder";
    //Update_URL
    public static final String ACCOUNT_UPDATE_URL = "https://ppp.supersocksr.xyz/api/account/update";

    //GLOBAL_SETTINGS
    public static final String GLOBAL_SETTINGS ="https://ppp.supersocksr.xyz/res/globalsettings.android.json";//globalsettings.windows.json
    //appsettings-config-path
    public static String APP_SETTINGS_PATH = "src/main/assets/appsettings.json";
    //ip-address
    public static String APP_IP_ADDRESS = "ip.txt";
    //appsettings-config-name
    public static String APP_SETTINGS_NAME = "appsettings.json";
    //appuser.json
    public static String APP_USER_NAME = "appuser.json";
    //package-path
    public static final String APP_PACKAGE_PATH = "/data/data/com.android.ppp/cache";
    //log.txt
    public static final String LOG_TXT = "log.txt";
    //log_update-time:1.5s
    public static final int LogScheudlerTimer = 1000;
    //update-time:10s
    public static final int ScheudlerTimer = 10000;
    //vpn-tun-ip
    public static String vpnTunAddress = "10.0.0.1";
    //defaultDnsAddresses
    public static String[] defaultDnsAddresses = {"1.1.1.1", "8.8.8.8"}; //"8.8.8.8", "8.8.4.4"
    //代理port设置类
    public static int[] allAgentPorts = {8080, 1080};
    //1000
    public static int ONE_THOUSAND = 1000;
    //10000
    public static int TEN_THOUSAND = 10000;
    //用户类型
    public enum UserType {
        //免费
        Free,
        //计时
        Time,
        //流量
        Data,
        //内部
        Inner
    };

    //设置类
    public static String[] allSettings = {
            "快速发包",
            "DNS",
            "FULL NAT",
            "代理",
            "路由",
    };

    //代理port设置类
    public static String[] allAgentPortSettings = {
            "web代理",
            "Socks5代理"
    };

    //路由设置类
    public static String[] allRouteSettings = {
            "自定义",
            "地区"
    };

    //路由设置类
    public static String[] allAppSettings = {
            "退出登录",
            "允许使用流量的App"
    };

    //邮箱主机白名单
    public static String[] mailHostWhitelist = {
            "@ppp.com",
            "@56.com",
            "@88.com",
            "@111.com",
            "@126.com",
            "@139.com",
            "@163.com",
            "@189.com",
            "@263.com",
            "@2925.com",
            "@2980.com",
            "@21cn.com",
            "@qq.com",
            "@vip.qq.com",
            "@jd.com",
            "@foxmail.com",
            "@china.com",
            "@huawei.com",
            "@icloud.com",
            "@netease.com",
            "@yahoo.com",
            "@163.net",
            "@vip.tom.com",
            "@163vip.com",
            "@yeah.net",
            "@vip.163.com",
            "@vip.126.com",
            "@188.com",
            "@vip.188.com",
            "@sina.com",
            "@sina.cn",
            "@vip.sina.com",
            "@sina.com.cn",
            "@vip.sina.com.cn",
            "@opera.com",
            "@gmail.com",
            "@msn.com",
            "@live.com",
            "@live.cn",
            "@outlook.com",
            "@hotmail.com",
            "@aliyun.com",
            "@email.cn",
            "@sohu.com",
            "@wo.cn",
            "@dingtalk.com",
            "@mail.com",
            "@walla.com",
            "@aim.com",
            "@inbox.com",
            "@email.com",
            "@chinaren.com",
            "@sogou.com",
            "@citiz.com",
            "@eyou.com",
            "@etang.com",
            "@aol.com",
            "@letv.com",
            "@shop888.tw",
            "@m168.com.tw",
            "@kiss99.com",
            "@mail2000.com.tw"
    };

    //全局路由
    public static String[] gloablRoutes = {
            "0.0.0.0/0",
            "0.0.0.0/1",
            "128.0.0.0/1"
    };

    //匹配Log.txt的字符串
    public static String IP = "IPv4";
    public static String Tcp = "Tcp";
    public static String Udp = "Udp";
    public static String Icmp = "Icmp";
    public static String Outgoing_Traffic_Size = "Outgoing Traffic Size";
    public static String Incoming_Traffic_Size = "Incoming Traffic Size";
    public static String Tunnel_Traffic_Size = "Tunnel Traffic Size";
    public static String Outgoing_Unicast_Packet = "Outgoing Unicast Packet";
    public static String Connect_Connections = "Connect Connections";

    public static String Outgoing_Speed_Seconds = "Outgoing Speed Per Seconds";
    public static String Incoming_Speed_Seconds = "Incoming Speed Per Seconds";
    public static String Tunnel_Speed_Seconds = "Tunnel Speed Per Seconds";
    public static String Activity_All_Ports = "Activity All Ports";
}