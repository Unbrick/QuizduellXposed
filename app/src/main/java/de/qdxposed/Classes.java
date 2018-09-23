package de.qdxposed;

/**
 * Created by Admin on 02.05.2018.
 */

public class Classes {

    public static Class AlternativeButton;
    public static Class AlternativeButtons;
    public static Class QuestionInterface;
    public static Class AlternativeInterface;
    public static Class QkSettingsHelper;


    public static class old {
        public static String AlternativeButton = "se.feomedia.quizkampen.act.game.AlternativeButton";
        public static String AlternativeButtons = "se.feomedia.quizkampen.act.game.AlternativeButtons";
        public static String QuestionInterface = "se.feomedia.quizkampen.modelinterfaces.Question";
        public static String AlternativeInterface = "se.feomedia.quizkampen.modelinterfaces.Alternative";
        public static String QkSettingsHelper = "se.feomedia.quizkampen.helpers.QkSettingsHelper";

    }

    public static class pre_4_8_9 {
        public static String AlternativeButton = "se.feomedia.quizkampen.view.activities.game.AlternativeButton";
        public static String AlternativeButtons = "se.feomedia.quizkampen.view.activities.game.AlternativeButtons";
        public static String QuestionInterface = "se.feomedia.quizkampen.data.db.base.Question";
        public static String AlternativeInterface = "se.feomedia.quizkampen.data.db.base.Alternative";
        public static String QkSettingsHelper = "se.feomedia.quizkampen.data.helpers.QkSettingsHelper";
    }

    //Recent version is 4.8.9 at 20.06.2018
    public static class recent {
        public static String BaseQuestion = "se.feomedia.quizkampen.domain.BaseQuestion";
    }
}
