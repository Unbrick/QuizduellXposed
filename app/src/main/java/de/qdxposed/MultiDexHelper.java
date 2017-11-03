package de.qdxposed;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Adapted from https://stackoverflow.com/questions/26623905/android-multidex-list-all-classes
 */

public class MultiDexHelper {
    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator +
            "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                        ? Context.MODE_PRIVATE
                        : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    public static int getDexCount(ApplicationInfo mAppInfo){
        try {
            Context mContext = getSys().createPackageContext(mAppInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
            return getMultiDexPreferences(mContext).getInt(KEY_DEX_NUMBER, 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static Context getSys() {
        return (Context) callMethod(callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread"), "getSystemContext");
    }
}
