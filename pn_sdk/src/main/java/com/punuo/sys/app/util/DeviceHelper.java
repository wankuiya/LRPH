package com.punuo.sys.app.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.punuo.sys.app.PnApplication;

/**
 * Created by han.chen.
 * Date on 2019/4/2.
 **/
public class DeviceHelper {

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = PnApplication.getInstance().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getVersionName() {
        PackageManager packageManager = PnApplication.getInstance().getPackageManager();
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(PnApplication.getInstance().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packInfo == null) {
            return "获取版本失败";
        }
        return "当前版本:v" + packInfo.versionName;
    }
}
