package de.qdxposed;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NewHook {
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedHelpers.findAndHookMethod("se.feomedia.quizkampen.domain.Alternative", lpparam.classLoader, "getAlternative", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String alternativeString = (String) XposedHelpers.getObjectField(param.thisObject, "alternative");

                    if (XposedHelpers.getBooleanField(param.thisObject, "isCorrect") && !alternativeString.contains("✓")) {
                        XposedHelpers.setObjectField(param.thisObject, "alternative", alternativeString + " ✓");
                        XposedBridge.log("QuizduellXposed: Found correct alternative: " + alternativeString);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        XposedHelpers.findAndHookMethod("se.feomedia.quizkampen.domain.ClassicQuestionModel", lpparam.classLoader, "getOpponentAnswer", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object alternative = XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "opponentAnswer"), "alternative");
                    String alternativeString = (String) XposedHelpers.getObjectField(alternative, "alternative");

                    if (!alternativeString.contains("⊕")) {
                        XposedHelpers.setObjectField(alternative, "alternative", alternativeString + " ⊕");
                        XposedBridge.log("QuizduellXposed: Found opponent answer in ClassicQuestionModel: " + alternativeString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        XposedHelpers.findAndHookMethod("se.feomedia.quizkampen.domain.QuizQuestionModel", lpparam.classLoader, "getOpponentAnswer", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object alternative = XposedHelpers.getObjectField(XposedHelpers.getObjectField(param.thisObject, "opponentAnswer"), "alternative");
                    String alternativeString = (String) XposedHelpers.getObjectField(alternative, "alternative");

                    if (!alternativeString.contains("⊕")) {
                        XposedHelpers.setObjectField(alternative, "alternative", alternativeString + " ⊕");
                        XposedBridge.log("QuizduellXposed: Found opponent answer in QuizQuestionModel: " + alternativeString);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
