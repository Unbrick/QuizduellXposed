package de.qdxposed;

import android.app.Application;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
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

        FormatStrategy mFormatting = PrettyFormatStrategy
                .newBuilder()
                .showThreadInfo(false)
                .methodCount(0)
                .methodOffset(0)
                .tag("QuizduellXposed")
                .build();
        Logger.addLogAdapter(new AndroidLogAdapter(mFormatting));

        Log.d(TAG, "**********************************************************");
        Log.d(TAG, "************** QuizDuell Xposed Starting...***************");
        Log.d(TAG, "**********************************************************");


        if (MultiDexHelper.getDexCount(lpparam.appInfo) == 1) {
            startHooking(lpparam);
        } else if (MultiDexHelper.getDexCount(lpparam.appInfo) > 1) {
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    startHooking(lpparam);
                }
            });
        } else Log.e(TAG, "NO DEX FILES FOUND, CANNOT DETERMINATE WHERE TO HOOK!");
    }

    private void startHooking(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.contains("lite")) {
            removeAds(lpparam);
        }

        appendAnswer(lpparam);
        appendOpponentAnswer(lpparam);
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

    private void appendOpponentAnswer(XC_LoadPackage.LoadPackageParam lpparam){
        findAndHookMethod("se.feomedia.quizkampen.act.game.AlternativeButton", lpparam.classLoader, "changeAlternative","se.feomedia.quizkampen.modelinterfaces.Alternative", boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean mIsOpponentAnswer = false;
                try {
                    mIsOpponentAnswer = XposedHelpers.getBooleanField(param.thisObject, "mIsOpponentAnswer");
                    Log.d(TAG, "Opponent answer: "+callMethod(callMethod(getObjectField(param.thisObject,"alternativeTextView"),"getText"),"toString")+" Value: "+mIsOpponentAnswer);
                } catch (Exception e) {
                    Log.d(TAG, "Field mIsOpponentAnswer not found");
                }
                    if (mIsOpponentAnswer) {
                        Object alternativeTextView = getObjectField(param.thisObject, "alternativeTextView");
                        if (!(
                                callMethod(alternativeTextView, "getText") instanceof SpannableStringBuilder
                                        ? (String) callMethod(callMethod(alternativeTextView, "getText"), "toString")
                                        : (String) callMethod(alternativeTextView, "getText")).contains("⊕")) {
                            callMethod(alternativeTextView, "append", " ⊕");
                            Log.d(TAG, "Oppenent answer appended");
                        }
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
