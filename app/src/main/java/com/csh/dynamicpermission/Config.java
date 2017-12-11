package com.csh.dynamicpermission;

import android.Manifest;

public class Config {
    //权限code
    public final static int PERMS_APPLY_CODE = 0;
    //需要申请的权限列表
    public static String[] PERMISSIONS = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
}
