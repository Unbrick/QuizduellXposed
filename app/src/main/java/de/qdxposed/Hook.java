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
        if (XposedHelpers.findClassIfExists(Classes.old.AlternativeButton, lpparam.classLoader) != null) {
            log("findClasses: using old classes");
            Classes.AlternativeButton = XposedHelpers.findClass(Classes.old.AlternativeButton, lpparam.classLoader);
            Classes.AlternativeButtons = XposedHelpers.findClass(Classes.old.AlternativeButtons, lpparam.classLoader);
            Classes.QuestionInterface = XposedHelpers.findClass(Classes.old.QuestionInterface, lpparam.classLoader);
            Classes.AlternativeInterface = XposedHelpers.findClass(Classes.old.AlternativeInterface, lpparam.classLoader);
            Classes.QkSettingsHelper = XposedHelpers.findClass(Classes.old.QkSettingsHelper, lpparam.classLoader);
        } else if (XposedHelpers.findClassIfExists(Classes.pre_4_8_9.AlternativeButton, lpparam.classLoader) != null) {
            log("findClasses: using pre 4.8.9 classes");
            Classes.AlternativeButton = XposedHelpers.findClass(Classes.pre_4_8_9.AlternativeButton, lpparam.classLoader);
            Classes.AlternativeButtons = XposedHelpers.findClass(Classes.pre_4_8_9.AlternativeButtons, lpparam.classLoader);
            Classes.QuestionInterface = XposedHelpers.findClass(Classes.pre_4_8_9.QuestionInterface, lpparam.classLoader);
            Classes.AlternativeInterface = XposedHelpers.findClass(Classes.pre_4_8_9.AlternativeInterface, lpparam.classLoader);
            Classes.QkSettingsHelper = XposedHelpers.findClass(Classes.pre_4_8_9.QkSettingsHelper, lpparam.classLoader);
        }
    }

    private void startHooking(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.contains("lite")) {
            try {
                removeAds(lpparam);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            appendAnswer(lpparam);
            appendOpponentAnswer(lpparam);
        } catch (Exception e) {
            log("Failed using old method, trying new...");
            try {
                new NewHook().hook(lpparam);
            } catch (Exception e2) {
                log("Failed using new Method: ");
                try {
                    hookRecent(lpparam);
                } catch (Exception e3) {
                    log("Can't hook QD using recent hooks, fuck.");
                }
            }
        }
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

    private void hookRecent(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod(Classes.recent.BaseQuestion, lpparam.classLoader, "getCorrect", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String correct = (String) getObjectField(param.thisObject, "correct");
                correct = (correct != null && correct.charAt(correct.length() - 1) != '✓') ? correct + " ✓" : correct;
                callMethod(param.thisObject, "setCorrect", correct);
            }
        });
    }

    private void log(String data) {
        XposedBridge.log("QuizduellXposed: " + data);
    }
}
