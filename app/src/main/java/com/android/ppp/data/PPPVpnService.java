package com.android.ppp.data;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LocalServerSocket;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

import com.android.ppp.R;
import com.android.ppp.config.Config;
import com.android.ppp.ui.home.RouteSettingActivity;
import com.android.ppp.ui.login.LoginActivity;
import com.android.ppp.ui.vpn.RoutesActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidProcess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PPPVpnService extends VpnService
{
    private static final String TAG = "PPP";

    public static final String BROADCAST_VPN_STATE = "com.ppp.android.data.status";
    public static final String BROADCAST_STOP_VPN  = "com.ppp.android.data.stop";

    private ParcelFileDescriptor vpnInterface = null;
    private ExecutorService executorService;
    private VPNRunnable vpnRunnable;
    private ServerRunnable serverRunnable;
    private Network[] mNets;

    @Override
    public void onCreate()
    {
        super.onCreate();

        registerReceiver(stopReceiver, new IntentFilter(BROADCAST_STOP_VPN));
        _UpdateNetWork();

        _KillProcess();
        _CopyPPxToPhone();
        _MovePPXToFileAndExecute();

        serverRunnable = new ServerRunnable(mNets, this);
        vpnRunnable = new VPNRunnable();
        executorService = Executors.newFixedThreadPool(5);
        executorService.submit(serverRunnable);
        executorService.submit(vpnRunnable);

        if(_SetupVPN())
        {
            vpnRunnable.SetVpnInerface(vpnInterface);
            sendBroadcast(new Intent(BROADCAST_VPN_STATE).putExtra("running", true));
        }
    }

    private void _UpdateNetWork()
    {
        final ConnectivityManager connectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            mNets = connectManager.getAllNetworks();
            if(mNets != null && mNets.length > 0)
            {
                setUnderlyingNetworks(mNets);
                try {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        ConnectivityManager.setProcessDefaultNetwork(mNets[0]);
                    } else {
                        connectManager.bindProcessToNetwork(mNets[0]);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean _SetupVPN()
    {
        try
        {
            if (vpnInterface == null)
            {
                Builder builder = new Builder();
                builder.addAddress("10.0.0.2", 30);
                if(AppData.isAllowGlobalRoute)
                {
                    for (int i = 0; i < Config.gloablRoutes.length; ++i)
                    {
                        String[] data = Config.gloablRoutes[i].split("/");
                        builder.addRoute(data[0], AppData.String2Int(data[1]));
                    }
                }
                else
                {
                    if(AppData.mSelfDefineRouteAddress != null && AppData.mSelfDefineRouteAddress != "")
                    {
                        String[] addresses = AppData.mSelfDefineRouteAddress.split("\n");
                        for (int i = 0; i < addresses.length; ++i)
                        {
                            String[] data = addresses[i].split("/");
                            builder.addRoute(data[0], AppData.String2Int(data[1]));
                        }
                    }

                    if(AppData.selectRegions.contains(this.getString(R.string.All)))
                    {
                        if(AppData.allRegionRouterInfos != null)
                        {
                            for (int i = 0; i < AppData.allRegionRouterInfos.length; ++i)
                            {
                                String[] data = AppData.allRegionRouterInfos[i].split("/");
                                builder.addRoute(data[0], AppData.String2Int(data[1]));
                            }
                        }
                    }
                    else if(AppData.selectRegions.size() > 0)
                    {
                        for (int i = 0; i < AppData.selectRegions.size(); ++i)
                        {
                            String name = AppData.selectRegions.get(i);
                            String[] info = JsonUtils.ReadAssetJson(this, _GetRegionRouteTxtName(name)).split("\n");
                            for (int j = 0; j < info.length; ++j)
                            {
                                String[] data = info[j].split("/");
                                builder.addRoute(data[0], AppData.String2Int(data[1]));
                            }
                        }
                    }
                    else
                    {
                        builder.addRoute("10.0.0.0", 30);
                        builder.addRoute("0.0.0.0", 0);
                    }
                }

                for (int i= 0; i < AppData.mDNSServerAddresses.length; ++i)
                {
                    builder.addDnsServer(AppData.mDNSServerAddresses[i]);
                }
                builder.setMtu(1500);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder.allowFamily(OsConstants.AF_INET);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder.setBlocking(true);

                if (Build.VERSION.SDK_INT >= 29)
                    builder.setMetered(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                {
                    if(mNets != null && mNets.length > 0)
                    {
                        builder.setUnderlyingNetworks(mNets);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    int webPort = (int)JsonUtils.GetElementValue(Config.APP_PACKAGE_PATH + "/" + Config.APP_SETTINGS_NAME, "ppp", "webProxy", "port");
                    builder.setHttpProxy(ProxyInfo.buildDirectProxy(AppData.isAllowLanConnect ? "0.0.0.0" : "127.0.0.1", webPort));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    for (int i = 0; i < AppData.allowUseVpnPackageNames.size(); ++i)
                    {
                        builder.addAllowedApplication(AppData.allowUseVpnPackageNames.get(i));
                    }
                }

                Intent configure = new Intent(this, RoutesActivity.class);
                PendingIntent pi = PendingIntent.getActivity(this, 0, configure, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setConfigureIntent(pi);

                vpnInterface = builder.setSession("ppp").establish();
            }

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private String _GetRegionRouteTxtName(String name)
    {
        if(AppData.regionRouterSettings == null)
            AppData.regionRouterSettings = JsonUtils.ReadAssetJson(this, "countryroutersettings.json");

        try {
            JSONObject obj = new JSONObject(AppData.regionRouterSettings);
            JSONArray array = obj.getJSONArray("region_routers");
            JSONObject regionObj;
            for (int i = 0; i < array.length(); ++i)
            {
                regionObj = array.getJSONObject(i);
                if(regionObj.getString("Name").equals(name))
                    return regionObj.getString("File");
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_NOT_STICKY;
    }

    private boolean _KillProcess()
    {
        List<AndroidProcess> processes = AndroidProcesses.getRunningProcesses();
        for (AndroidProcess process : processes)
        {
            if (process.name.contains("libppp"))
            {
                if (Config.CanLog)
                    Log.i("KILL", process.name + " : " + process.pid);

                android.os.Process.sendSignal(process.pid, android.os.Process.SIGNAL_KILL);
                return true;
            }
        }

        return false;
    }

    private void _CopyPPxToPhone()
    {
        //localsocket接收ppx服务器返回数据
        _DeleteFile(Config.APP_PACKAGE_PATH, "ppp_receive_path");
        _DeleteFile(Config.APP_PACKAGE_PATH, "ppp_send_path");
        //先删除ppp.lock文件
        _DeleteFile(Config.APP_PACKAGE_PATH,"libppp.so.lock");
        //_DeleteFile(Config.APP_PACKAGE_PATH,"appsettings.json");
    }

    private void _DeleteFile(String fileDirPath, String fileName)
    {
        // 文件路径
        String filePath = fileDirPath + "/" + fileName;
        try
        {
            // 目录路径
            File dir = new File(fileDirPath);
            if (!dir.exists())
                return;

            File file = new File(filePath);
            if(file.exists())
                file.delete();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String _GetNativeLibraryPath()
    {
        String dir = getApplicationContext().getApplicationInfo().nativeLibraryDir;
        if(Config.CanLog)
            Log.i("Application", "nativeLibraryDir： " + dir);
        return dir;
    }

    //把ppx程序放到相应文件夹并启动
    private void _MovePPXToFileAndExecute()
    {
        _ExecuteCMD("/system/bin/chmod a+rwx " + Config.APP_PACKAGE_PATH + "/appsettings.json");
        //"--reexec-lock=." + Config.APP_PACKAGE_PATH + "/libppp.so.lock " +
        String ppp = _GetNativeLibraryPath() + "/libppp.so " +
                "--config=" + Config.APP_PACKAGE_PATH + "/appsettings.json " +
                "--log-path=" + Config.APP_PACKAGE_PATH + "/log.txt " +
                "--add-all-routing=no " +
                "--set-dns-addresses=no " +
                "--interface-name=Ppp " +
                "--interface-uri=" + Config.APP_PACKAGE_PATH + "/ppp_send_path " +
                "--interface-timeout=3600000 " +
                "--protect-uri=" + Config.APP_PACKAGE_PATH + "/ppp_receive_path " +
                "--local-address=10.0.0.2 " +
                "--gateway-address=" + Config.vpnTunAddress + " " +
                "--netmask-prefix=30";
        //"--config=/data/data/com.fast.fastvpn/cache/appsettings.json --log-path= --add-all-routing=no --set-dns-addresses=no --interface-name=ppx --interface-uri=/data/data/com.fast.fastvpn/cache/ppp_send_path --interface-timeout=3600000 --protect-uri=/data/data/com.fast.fastvpn/cache/ppp_receive_path --local-address=10.0.0.2 --gateway-address=10.0.0.1 --netmask-prefix=30"
        //Log.e(TAG, "Command is : " + ppp);
        _ExecuteCMDNoResult(ppp);
    }

    private void _ExecuteCMDNoResult(String command)
    {
        try
        {
            Runtime.getRuntime().exec(command);
        }
        catch (IOException var)
        {
            throw new RuntimeException(var);
        }
    }

    private String _ExecuteCMD(String command)
    {
        try
        {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        }
        catch (IOException var)
        {
            throw new RuntimeException(var);
        }
        catch (InterruptedException var1)
        {
            throw new RuntimeException(var1);
        }
    }

    private void _StopVpn()
    {
        _KillProcess();
        if(vpnRunnable != null)
            vpnRunnable.Stop();

        if(serverRunnable != null)
            serverRunnable.Stop();

        if(vpnInterface !=null)
        {
            try {
                vpnInterface.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        vpnInterface = null;
        serverRunnable = null;
        vpnRunnable = null;
        executorService = null;
        sendBroadcast(new Intent(BROADCAST_VPN_STATE).putExtra("running", false));
    }

    private BroadcastReceiver stopReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent == null || intent.getAction() == null)
                return;

            if (BROADCAST_STOP_VPN.equals(intent.getAction()))
            {
                onRevoke();
                _StopVpn();
            }
        }
    };

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        unregisterReceiver(stopReceiver);
    }

    private static class ServerRunnable implements Runnable
    {
        private static final String TAG = "ServerRunnable";

        private libcor32 serverSocket;
        private VpnService vpnService;
        private Network[] mNetworks;
        private int sock;
        private boolean isStop;

        public ServerRunnable(Network[] nets, VpnService service)
        {
            isStop = false;
            this.mNetworks = nets;
            this.vpnService = service;
        }

        public void Stop()
        {
            isStop = true;
            _CloseSocket();
            serverSocket = null;
            sock = 0;
            mNetworks = null;
            vpnService = null;
        }

        private void _CloseSocket()
        {
            if(serverSocket != null)
            {
                serverSocket.closesocket(sock);
            }
        }

        private void _BindSocketToNetwork(Network network, int socketfd)
        {
            if(network == null)
                return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                FileDescriptor fd = new FileDescriptor();
                try {
                    Field field = FileDescriptor.class.getDeclaredField("descriptor");
                    field.setAccessible(true);
                    field.setInt(fd, socketfd);
                    try {
                        final SocketAddress peer = Os.getpeername(fd);
                        final InetAddress inetPeer = ((InetSocketAddress)peer).getAddress();
                        if (!inetPeer.isAnyLocalAddress())
                        {
                            if(Config.CanLog)
                                Log.e(TAG,"bindSocket failed-> hostAddress " + inetPeer.getHostAddress() + " hostName: " + inetPeer.getHostName());
                        }
                    }
                    catch (@SuppressLint("NewApi") ErrnoException e)
                    {
                        if (e.errno == OsConstants.ENOTCONN)
                        {
                            network.bindSocket(fd);
                            if(Config.CanLog)
                                Log.d(TAG,"bindSocket success-> network: " + network + " socketfd: " + socketfd);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run()
        {
            if(isStop)
                return;

            if(serverSocket == null)
            {
                serverSocket = new libcor32();
                sock = serverSocket.create_unix_socket(Config.APP_PACKAGE_PATH + "/ppp_receive_path");
                if(Config.CanLog)
                    Log.i(TAG, "serverSocket Create sock is " + sock);
            }

            try
            {
                while (true)
                {
                    //等待建立连接
                    int[] results = serverSocket.recvfd(sock);
                    if (Config.CanLog)
                        Log.i(TAG, "serverSocket accept fd " + results[0] + " conn is " + results[1] + " err is " + results[2]);

                    if(results[0] > 0)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                        {
                            for (int i = 0; i < mNetworks.length; ++i)
                            {
                                _BindSocketToNetwork(mNetworks[i], results[0]);
                            }
                        }
                        vpnService.protect(results[0]);
                        serverSocket.sendack(results[1], 0);
                    }
                    else {
                        serverSocket.sendack(results[1], results[2]);
                        break;
                    }
                }
            }
            finally {
                if (Config.CanLog)
                    Log.i(TAG, "finally close socket ");
                _CloseSocket();
            }
        }
    }

    private static class VPNRunnable implements Runnable
    {
        private static final String TAG = "VPNRunnable";
        //创建对象
        private LocalSocket sendSocket;
        private ParcelFileDescriptor vpnInterface;
        private boolean isSend;
        private boolean isStop;

        public VPNRunnable()
        {
            isStop = false;
            isSend = false;
        }

        public void SetVpnInerface(ParcelFileDescriptor vpnInterface)
        {
            this.vpnInterface = vpnInterface;
        }

        public void Stop()
        {
            isStop = true;
            isSend = false;
            sendSocket = null;
            vpnInterface = null;
        }

        private boolean _HasPPxProcess()
        {
            List<AndroidProcess> processes = AndroidProcesses.getRunningProcesses();
            for (AndroidProcess process : processes)
            {
                if (process.name.contains("libppp"))
                {
                    if (Config.CanLog)
                        Log.i("PPXProcess", process.name + " : " + process.pid);

                    return true;
                }
            }

            return false;
        }

        private boolean _HasFile(String fileDirPath, String fileName)
        {
            String filePath = fileDirPath + "/" + fileName;
            try
            {
                File file = new File(filePath);
                return file.exists();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        private void SendFd()
        {
            try
            {
                if(_HasPPxProcess() && _HasFile(Config.APP_PACKAGE_PATH, "ppp_send_path"))
                {
                    if(!sendSocket.isConnected())
                    {
                        if(Config.CanLog)
                            Log.i(TAG, "connect ppp_send_path");

                        sendSocket.connect(new LocalSocketAddress(Config.APP_PACKAGE_PATH + "/ppp_send_path", LocalSocketAddress.Namespace.FILESYSTEM));
                    }
                }

                if(sendSocket.isConnected() && vpnInterface != null &&
                        _HasFile(Config.APP_PACKAGE_PATH, "ppp_receive_path") && !isSend)
                {
                    FileDescriptor fd = vpnInterface.getFileDescriptor();
                    if(fd.valid())
                    {
                        if (Config.CanLog)
                            Log.i(TAG, "SendFd Done");
                        isSend = true;
                        FileDescriptor[] fds = new FileDescriptor[]{fd};
                        sendSocket.setFileDescriptorsForSend(fds);
                        sendSocket.getOutputStream().write(42);
                        sendSocket.getInputStream().read();
                        sendSocket.close();
                    }
                }
            }
            catch (IOException e)
            {
                Log.w(TAG, e.toString(), e);
            }
        }

        @Override
        public void run()
        {
            while (true)
            {
                if (isStop)
                {
                    if (Config.CanLog)
                        Log.i(TAG, "VPN_RUNNABLE BREAK");
                    break;
                }

                if (sendSocket == null || sendSocket.isClosed())
                {
                    sendSocket = new LocalSocket();
                    SendFd();
                }

                if (sendSocket != null && isSend)
                {
                    if(Config.CanLog)
                        Log.i(TAG, "sendSocket already send");
                    break;
                }
                else
                {
                    try
                    {
                        Thread.sleep(2000);
                        if(Config.CanLog)
                            Log.i(TAG, "sendfd sleep");
                        SendFd();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}