package com.app.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.R;
import com.app.ftp.Ftp;
import com.app.ftp.FtpListener;
import com.app.model.Constant;
import com.app.sip.BodyFactory;
import com.app.sip.SipInfo;
import com.app.sip.SipMessageFactory;
import com.app.tools.VersionXmlParse;
import com.punuo.sys.app.activity.ActivityCollector;
import com.punuo.sys.app.activity.BaseSwipeBackActivity;
import com.punuo.sys.app.httplib.HttpConfig;
import com.punuo.sys.app.util.DeviceHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.camera.FileOperateUtil.TAG;
import static com.app.sip.SipInfo.sipUser;

public class SoftwareInstructActivity extends BaseSwipeBackActivity {


    @Bind(R.id.tv_version)
    TextView tv_version;
    @Bind(R.id.tv_introduct)
    TextView tvIntroduct;
    @Bind(R.id.tv_update)
    TextView tvUpdate;
    @Bind(R.id.back)
    ImageView back;
    @Bind(R.id.title)
    TextView title;

    //手机内存卡路径
    private String SdCard;
    //当前版本
    private String version;
    //FTP上的版本
    private String FtpVersion;
    //用于版本xml解析
    private HashMap<String, String> versionHashMap = new HashMap<>();

    //进度条消失类型
    private String result;
    //下载进度条
    private ProgressDialog downloadDialog;
    private String apkPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_software_intruct);
        ButterKnife.bind(this);
        SdCard = Environment.getExternalStorageDirectory().getAbsolutePath();
        apkPath = SdCard + "/fanxin/download/apk/";
        title.setText("关于");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//因为不是所有的系统都可以设置颜色的，在4.4以下就不可以。。有的说4.1，所以在设置的时候要检查一下系统版本是否是4.1以上
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.image_bar));
        }
        tv_version.setText(DeviceHelper.getVersionName());
    }

    @OnClick({R.id.tv_introduct, R.id.tv_update, R.id.back})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_introduct:
                Toast.makeText(this, "该功能即将上线", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_update:
                result = "Finished";
                showLoadingDialog();
                //初始化FTP
                mFtp = new Ftp(HttpConfig.getHost(), 21, "ftpall", "123456", Dversion);
                //获取当前版本号
                PackageManager packageManager = this.getPackageManager();
                try {
                    PackageInfo pi = packageManager.getPackageInfo(this.getPackageName(), 0);
                    version = pi.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                new Thread(checkVersion).start();
                break;
            case R.id.back:
                scrollToFinishActivity();
                break;
            default:
                break;
        }
    }

    //Ftp对象
    Ftp mFtp;
    //版本信息下载监听器
    FtpListener Dversion = new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {
            Log.i(TAG, currentStep);
        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            if (currentStep.equals(Constant.FTP_DOWN_SUCCESS)) {
                Log.i(TAG, currentStep);
            } else if (currentStep.equals(Constant.FTP_DOWN_LOADING)) {
                Log.i(TAG, "-----下载---" + downProcess + "%");
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    //版本apk下载监听器
    FtpListener Dapk = new FtpListener() {
        @Override
        public void onStateChange(String currentStep) {

        }

        @Override
        public void onUploadProgress(String currentStep, long uploadSize, File targetFile) {

        }

        @Override
        public void onDownLoadProgress(String currentStep, long downProcess, File targetFile) {
            if (currentStep.equals(Constant.FTP_DOWN_SUCCESS)) {
                Log.d(TAG, currentStep);
                downloadDialog.dismiss();
                Message message = new Message();
                message.what = 0x0002;
                handler.sendMessage(message);
            }
            if (currentStep.equals(Constant.FTP_DOWN_LOADING)) {
                downloadDialog.setProgress((int) downProcess);
                Log.i(TAG, "-----下载---" + downProcess + "%");
            }
        }

        @Override
        public void onDeleteProgress(String currentStep) {

        }
    };
    Runnable checkVersion = new Runnable() {
        @Override
        public void run() {
            try {
                //下载版本信息xml文件
                mFtp.download("/apk/version_fanxin.xml", SdCard + "/fanxin/version/");
                File xml = new File(SdCard + "/fanxin/version/version_fanxin.xml");
                InputStream inputStream = new FileInputStream(xml);
                //解析xml文件
                versionHashMap = VersionXmlParse.parseXml(inputStream);
            } catch (Exception e) {
                result = e.getMessage();
            }
            //获取ftp上的版本号
            FtpVersion = versionHashMap.get("version");
            //根据result显示相应的对话框
            showVersionDialog(version, FtpVersion, result);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showVersionDialog(String currentVersion, final String FtpVersion, final String result) {
        //取消进度条
        dismissLoadingDialog();
        if (result.equals("Finished")) {
            Log.i(TAG, "当前版本为 " + version + "FTP上版本为 " + FtpVersion);
            if (!currentVersion.equals(FtpVersion)) {
                //版本不一致
                Message message = new Message();
                message.what = 0x0001;
                handler.sendMessage(message);
            } else {
                //版本一致
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = new AlertDialog.Builder(SoftwareInstructActivity.this)
                                .setTitle("当前为最新版本")
                                .setPositiveButton("确定", null)
                                .create();
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                    }
                });
            }
        } else {
            //失败
            showTip(result);
        }
    }

    //下载完成
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case 0x0001:
                    AlertDialog dialog = new AlertDialog.Builder(SoftwareInstructActivity.this)
                            .setTitle("有新版本")
                            .setMessage("当前版本为" + version + ",新版本为" + FtpVersion)
                            .setPositiveButton("下载并安装", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    downloadDialog = new ProgressDialog(SoftwareInstructActivity.this);
                                    downloadDialog.setTitle("下载进度");
                                    downloadDialog.setMessage("已经下载了");
                                    downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    downloadDialog.setCancelable(false);
                                    downloadDialog.setIndeterminate(false);
                                    downloadDialog.setMax(100);
                                    downloadDialog.show();
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            mFtp.setListener(Dapk);
                                            try {
                                                mFtp.download(versionHashMap.get("path"), apkPath);
                                            } catch (final Exception e) {
                                                downloadDialog.dismiss();
                                                showTip("网络连接失败,请检查网络或重试");
                                            }
                                        }
                                    }.start();
                                }
                            })
                            .setNegativeButton("取消", null).create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                    break;
                case 0x0002:
                    install(getApplicationContext());
                    sipUser.sendMessage(SipMessageFactory.createNotifyRequest(sipUser, SipInfo.user_to,
                            SipInfo.user_from, BodyFactory.createLogoutBody()));
                    //界面回到登录状态
                    ActivityCollector.finishToFirstView();
//                    }
                    break;
            }
            return true;
        }
    });

    private void showTip(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SoftwareInstructActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 通过隐式意图调用系统安装程序安装APK
     */
    public void install(Context context) {
        String localApkPath = apkPath + versionHashMap.get("name") + ".apk";
        Log.d(TAG, "handleMessage: " + localApkPath);
        File file = new File(localApkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(context, "com.alex.demo.FileProvider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }


}
