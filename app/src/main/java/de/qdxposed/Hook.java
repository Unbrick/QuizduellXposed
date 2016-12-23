package de.qdxposed;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
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

        /*
        * Hook classes.dex
        * */
        try {
            findAndHookMethod("se.feomedia.quizkampen.act.game.AlternativeButton", lpparam.classLoader, "changeAlternative", "se.feomedia.quizkampen.modelinterfaces.Alternative", boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object alternativeTextView = getObjectField(param.thisObject, "alternativeTextView");
                    Object alternative = getObjectField(param.thisObject, "alternative");
                    boolean isCorrect = (boolean) callMethod(alternative, "isCorrectAnswer");
                    if (isCorrect)
                        callMethod(alternativeTextView, "append", "max");
                }
            });
            Log.d("QuizduellXposed", "I'm in classes.dex");
        } catch (Exception ignored) {
            Log.d("QuizduellXposed", "Hooking classes.dex failed :(");
        }

        /*
        * Hook classes2.dex
        * */
        try {
            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                /*
                * Hook the buttons with the solutions
                * */
                    findAndHookMethod("se.feomedia.quizkampen.act.game.AlternativeButton", lpparam.classLoader, "changeAlternative", "se.feomedia.quizkampen.modelinterfaces.Alternative", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object alternativeTextView = getObjectField(param.thisObject, "alternativeTextView");
                            Object alternative = getObjectField(param.thisObject, "alternative");
                            boolean isCorrect = (boolean) callMethod(alternative, "isCorrectAnswer");
                            if (isCorrect)
                                callMethod(alternativeTextView, "append", "max");
                        }
                    });
                    Log.d("QuizduellXposed", "I'm in classes2.dex");
                }
            });
        } catch (Exception ignored) {
            Log.d("QuizduellXposed", "Hooking classes2.dex failed :(");
        }
    }
}
