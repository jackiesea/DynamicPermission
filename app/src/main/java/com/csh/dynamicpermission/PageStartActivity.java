package com.csh.dynamicpermission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.csh.dynamicpermission.Config.PERMS_APPLY_CODE;

public class PageStartActivity extends AppCompatActivity {
    private final static int PERM_SETTING_APPLY_CODE = 10;
    private Activity mActivity;
    private AlertDialog mPermApplyDialog;
    private AlertDialog mPermSettingDialog;
    private Disposable mShowViewDis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_page_start);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        initView();
    }

    private void initView() {
        mActivity = this;
        //动态申请权限
        if (!Util.isPermissionsApply(this, true)) {
            createShowViewSub();
        }
    }

    private void createShowViewSub() {
        Flowable flowable = Flowable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        startActivity(new Intent(mActivity, MainActivity.class));
                        finish();
                    }
                });

        mShowViewDis = flowable.subscribe();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PERM_SETTING_APPLY_CODE:
                if (!Util.isPermissionsApply(this, false)) {     //权限已经全部申请
                    if (mPermSettingDialog != null && mPermSettingDialog.isShowing()) {
                        mPermSettingDialog.dismiss();
                    }
                    createShowViewSub();
                }
                break;
        }
    }

    /**
     * 注册权限申请回调
     *
     * @param requestCode  申请码
     * @param permissions  申请的权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMS_APPLY_CODE:
                if (!Util.isPermissionOk(grantResults)) {   //有未申请的权限
                    boolean isCanShowApplyAgain = false;
                    for (int i = 0; i < permissions.length; i++) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissions[i])) {    //只要有一个权限显示可以弹出继续申请，则可以弹出申请权限对话框
                            isCanShowApplyAgain = true;
                            break;
                        }
                    }
                    if (isCanShowApplyAgain) {  //可以继续弹出权限申请
                        showPermApplyDialog();
                    } else {    //不能够继续弹出
                        showPermSettingDialog();
                    }
                } else {
                    createShowViewSub();
                }
                break;
        }
    }

    /**
     * 有未申请的权限，未选择不再询问，继续弹出权限申请
     */
    private void showPermApplyDialog() {
        mPermApplyDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.perm_apply_title)
                .setMessage(R.string.perm_apply_msg)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Util.isPermissionsApply(mActivity, true);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        mPermApplyDialog.show();
    }

    /**
     * 有未申请的权限，并选了不再询问，进入设置设置应用权限
     */
    private void showPermSettingDialog() {
        mPermSettingDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.perm_apply_title)
                .setMessage(R.string.perm_setting_msg)
                .setPositiveButton(R.string.goto_setting, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setCancelable(false)
                .create();
        mPermSettingDialog.show();
        //以下方式可以让alertdialog点击时不自动消失
        mPermSettingDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivityForResult(intent, PERM_SETTING_APPLY_CODE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.disposeSubscribe(mShowViewDis);
    }
}
