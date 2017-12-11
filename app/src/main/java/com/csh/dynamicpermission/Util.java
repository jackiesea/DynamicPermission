package com.csh.dynamicpermission;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

import static com.csh.dynamicpermission.Config.PERMISSIONS;
import static com.csh.dynamicpermission.Config.PERMS_APPLY_CODE;

public class Util {
    public static boolean isPermissionOk(@NonNull int[] grantResults) {
        boolean isOk = true;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) { //含有未申请到的权限
                isOk = false;
                break;
            }
        }
        return isOk;
    }

    //动态申请权限（Android6.0以上需要如此）
    public static boolean isPermissionsApply(Context context, boolean isApplyPerm) {
        boolean isNeedApplyPerm = false;
        //判断剩余还未申请的权限
        List<String> unApplyedList = new ArrayList<>();
        for (int i = 0; i < PERMISSIONS.length; i++) {
            if (ContextCompat.checkSelfPermission(context, PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                unApplyedList.add(PERMISSIONS[i]);   //申请过的权限将其删除，不要再提示给用户
            }
        }

        if (unApplyedList.size() > 0) { //有未申请的权限，去申请
            isNeedApplyPerm = true;
            if (isApplyPerm) {
                String[] unApplyedArray = new String[unApplyedList.size()];
                unApplyedList.toArray(unApplyedArray);
                ActivityCompat.requestPermissions((Activity) context, unApplyedArray, PERMS_APPLY_CODE);
            }
        }

        return isNeedApplyPerm;
    }

    //解注册观察者模式
    public static void disposeSubscribe(Disposable... disposables) {
        for (Disposable disposable : disposables) {
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }
}
