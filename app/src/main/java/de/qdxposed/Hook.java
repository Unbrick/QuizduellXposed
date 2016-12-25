package de.qdxposed;

import android.app.Application;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by Admin on 24.10.2016.
 */

public class Hook implements IXposedHookLoadPackage {

    private String TAG = "QuizduellXposed";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("quizkampen")) {
            return;
        }

        /*
        * Hook classes.dex
        * */
        try {
            if (lpparam.packageName.contains("lite")) {
                removeAds(lpparam);
            }

            appendAnswer(lpparam);
            Log.d(TAG, "I'm in classes.dex");
        } catch (Exception ignored) {
            Log.d(TAG, "Hooking classes.dex failed :(");
        }

        /*
        * Hook classes2.dex
        * */
        try {
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    if (lpparam.packageName.contains("lite")) {
                        removeAds(lpparam);
                    }

                    appendAnswer(lpparam);
                }
            });
        } catch (Exception ignored) {
            Log.d(TAG, "Hooking classes2.dex failed :(");
        }
    }

    private void appendAnswer(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("se.feomedia.quizkampen.act.game.AlternativeButtons", lpparam.classLoader, "setAlternatives", "se.feomedia.quizkampen.modelinterfaces.Question", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object correctButton = callMethod(param.thisObject, "getCorrectButton");
                Object alternativeTextView = getObjectField(correctButton, "alternativeTextView");
                if (!(
                        callMethod(alternativeTextView, "getText") instanceof SpannableStringBuilder
                                ? (String) callMethod(callMethod(alternativeTextView, "getText"), "toString")
                                : (String) callMethod(alternativeTextView, "getText")).contains("✓")) {
                    callMethod(alternativeTextView, "append", " ✓");
                    Log.d(TAG, "Solution appended");
                }
            }
        });
    }

    private void removeAds(final XC_LoadPackage.LoadPackageParam lpparam) {
        Log.d(TAG, "Found lite version, trying to remove ads!");

        findAndHookMethod("se.feomedia.quizkampen.helpers.QkSettingsHelper", lpparam.classLoader, "shouldShowAds", Context.class, "se.feomedia.quizkampen.models.User", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
                Log.d(TAG, "Ads removed!");
                return false;
            }
        });
    }
}
