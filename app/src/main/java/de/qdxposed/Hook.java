package de.qdxposed;

import android.app.Application;
import android.content.Context;
import android.text.SpannableStringBuilder;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * Created by Admin on 24.10.2016.
 */

public class Hook implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.contains("quizkampen")) {
            return;
        }

        log("**********************************************************");
        log("************** QuizDuell Xposed Starting...***************");
        log("**********************************************************");


        findClasses(lpparam);

        if (MultiDexHelper.getDexCount(lpparam.appInfo) == 1) {
            startHooking(lpparam);
        } else if (MultiDexHelper.getDexCount(lpparam.appInfo) > 1) {
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    startHooking(lpparam);
                }
            });
        } else log("NO DEX FILES FOUND, CANNOT DETERMINATE WHERE TO HOOK!");
    }

    private void findClasses(XC_LoadPackage.LoadPackageParam lpparam) {
        if (XposedHelpers.findClassIfExists(Classes.old_names.AlternativeButton, lpparam.classLoader) != null) {
            log("findClasses: using old classes");
            Classes.AlternativeButton = XposedHelpers.findClass(Classes.old_names.AlternativeButton, lpparam.classLoader);
            Classes.AlternativeButtons = XposedHelpers.findClass(Classes.old_names.AlternativeButtons, lpparam.classLoader);
            Classes.QuestionInterface = XposedHelpers.findClass(Classes.old_names.QuestionInterface, lpparam.classLoader);
            Classes.AlternativeInterface = XposedHelpers.findClass(Classes.old_names.AlternativeInterface, lpparam.classLoader);
            Classes.QkSettingsHelper = XposedHelpers.findClass(Classes.old_names.QkSettingsHelper, lpparam.classLoader);
        } else {
            log("findClasses: using new classes");
            Classes.AlternativeButton = XposedHelpers.findClass(Classes.new_names.AlternativeButton, lpparam.classLoader);
            Classes.AlternativeButtons = XposedHelpers.findClass(Classes.new_names.AlternativeButtons, lpparam.classLoader);
            Classes.QuestionInterface = XposedHelpers.findClass(Classes.new_names.QuestionInterface, lpparam.classLoader);
            Classes.AlternativeInterface = XposedHelpers.findClass(Classes.new_names.AlternativeInterface, lpparam.classLoader);
            Classes.QkSettingsHelper = XposedHelpers.findClass(Classes.new_names.QkSettingsHelper, lpparam.classLoader);
        }
    }

    private void startHooking(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.contains("lite")) {
            //removeAds(lpparam);
        }

        appendAnswer(lpparam);
        appendOpponentAnswer(lpparam);
    }

    private void appendAnswer(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(Classes.AlternativeButtons, "setAlternatives", Classes.QuestionInterface, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object correctButton = callMethod(param.thisObject, "getCorrectButton");

                Object alternativeTextView = getObjectField(correctButton, "alternativeTextView");
                if (!(
                        callMethod(alternativeTextView, "getText") instanceof SpannableStringBuilder
                                ? (String) callMethod(callMethod(alternativeTextView, "getText"), "toString")
                                : (String) callMethod(alternativeTextView, "getText")).contains("✓")) {
                    callMethod(alternativeTextView, "append", " ✓");
                    log("Solution appended");
                }
            }
        });
    }

    private void appendOpponentAnswer(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(Classes.AlternativeButton, "changeAlternative", Classes.AlternativeInterface, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean mIsOpponentAnswer = false;
                try {
                    mIsOpponentAnswer = XposedHelpers.getBooleanField(param.thisObject, "mIsOpponentAnswer");
                    log("Opponent answer: " + callMethod(callMethod(getObjectField(param.thisObject, "alternativeTextView"), "getText"), "toString") + " Value: " + mIsOpponentAnswer);
                } catch (Exception e) {
                    log("Field mIsOpponentAnswer not found");
                }
                if (mIsOpponentAnswer) {
                    Object alternativeTextView = getObjectField(param.thisObject, "alternativeTextView");
                    if (!(
                            callMethod(alternativeTextView, "getText") instanceof SpannableStringBuilder
                                    ? (String) callMethod(callMethod(alternativeTextView, "getText"), "toString")
                                    : (String) callMethod(alternativeTextView, "getText")).contains("⊕")) {
                        callMethod(alternativeTextView, "append", " ⊕");
                        log("Oppenent answer appended");
                    }
                }
            }
        });

    }

    private void removeAds(final XC_LoadPackage.LoadPackageParam lpparam) {
        log("Found lite version, trying to remove ads!");

        findAndHookMethod(Classes.QkSettingsHelper, "shouldShowAds", Context.class, "se.feomedia.quizkampen.models.User", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(false);
                log("Ads removed!");
                return false;
            }
        });
    }

    private void log(String data) {
        XposedBridge.log("QuizduellXposed: " + data);
    }
}
