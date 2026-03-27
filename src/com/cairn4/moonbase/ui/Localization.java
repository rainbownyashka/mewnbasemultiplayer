/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.I18NBundle;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SettingsLoader;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;

public class Localization {
    private static Localization instance;
    private Locale locale;
    private FileHandle baseFileHandle;
    private I18NBundle localeBundle;
    private I18NBundle languageNamesBundle;
    public static String[] supportedLocales;
    public static HashMap<String, Float> translationProgress;

    private Localization() {
        Gdx.app.log("MewnBase", "System Language: " + Locale.getDefault().toString());
        String settingLanguage = SettingsLoader.getInstance().settingsData.LANGUAGE;
        this.setLanguage(settingLanguage);
        translationProgress.put("en", Float.valueOf(1.0f));
        translationProgress.put("bg", Float.valueOf(0.99f));
        translationProgress.put("ca", Float.valueOf(1.0f));
        translationProgress.put("cs", Float.valueOf(0.85f));
        translationProgress.put("da", Float.valueOf(0.67f));
        translationProgress.put("de", Float.valueOf(1.0f));
        translationProgress.put("es", Float.valueOf(1.0f));
        translationProgress.put("fi", Float.valueOf(0.95f));
        translationProgress.put("fr", Float.valueOf(0.99f));
        translationProgress.put("it", Float.valueOf(0.99f));
        translationProgress.put("nl", Float.valueOf(0.83f));
        translationProgress.put("no", Float.valueOf(0.87f));
        translationProgress.put("lv", Float.valueOf(0.76f));
        translationProgress.put("pl", Float.valueOf(0.99f));
        translationProgress.put("pt", Float.valueOf(0.73f));
        translationProgress.put("pt_BR", Float.valueOf(1.0f));
        translationProgress.put("ru", Float.valueOf(1.0f));
        translationProgress.put("tr", Float.valueOf(0.99f));
        translationProgress.put("uk", Float.valueOf(0.99f));
    }

    public static synchronized void reload() {
        instance = new Localization();
    }

    private void setLanguage(String newLocale) {
        String[] localeString = newLocale.split("_");
        Gdx.app.log("MewnBase", "Setting language to: " + newLocale);
        this.locale = localeString.length > 1 ? new Locale(localeString[0], localeString[1]) : new Locale(localeString[0]);
        Gdx.app.log("MewnBase", "Locale: " + this.locale);
        this.baseFileHandle = Gdx.files.local(MoonBase.coreFolder + "locale/mewnbase");
        this.localeBundle = I18NBundle.createBundle(this.baseFileHandle, this.locale);
        FileHandle baseFileHandle_langNames = Gdx.files.local(MoonBase.coreFolder + "locale/langNames");
        this.languageNamesBundle = I18NBundle.createBundle(baseFileHandle_langNames);
    }

    public static void changeLanguage(String newLocale) {
        instance.setLanguage(newLocale);
    }

    public static synchronized I18NBundle getInstance() {
        if (instance == null) {
            instance = new Localization();
        }
        return Localization.instance.localeBundle;
    }

    public static synchronized String get(String key) {
        try {
            return Localization.getInstance().get(key);
        }
        catch (MissingResourceException e) {
            Gdx.app.error("Localization", e.getMessage());
            return "Format error: " + key;
        }
        catch (Exception e) {
            Gdx.app.error("Localization", e.getMessage());
            return "Format error: " + key;
        }
    }

    public static synchronized String format(String key, Object ... args) {
        try {
            return Localization.getInstance().format(key, args);
        }
        catch (MissingResourceException e) {
            Gdx.app.error("Localization", e.getMessage());
            return "Format error: " + key;
        }
        catch (IllegalArgumentException e) {
            Gdx.app.error("Localization", e.getMessage());
            return "Format error: " + key;
        }
        catch (Exception e) {
            Gdx.app.error("Localization", e.getMessage());
            return "Format error: " + key;
        }
    }

    public static synchronized String getLangName(String key) {
        if (instance == null) {
            instance = new Localization();
        }
        try {
            return Localization.instance.languageNamesBundle.get(key);
        }
        catch (MissingResourceException e) {
            Gdx.app.error("Localization", e.getMessage());
            return key;
        }
    }

    public static synchronized Localization getActualInstance() {
        if (instance == null) {
            instance = new Localization();
        }
        return instance;
    }

    public static String getSystemLanguage() {
        return Locale.getDefault().getLanguage().toString();
    }

    public static String getSystemLanguageOrDefault() {
        String systemLangauge = Locale.getDefault().getLanguage().toString();
        for (String s : supportedLocales) {
            if (!systemLangauge.equals(s)) continue;
            return systemLangauge;
        }
        Gdx.app.log("MewnBase", "System default language not supported, default to english");
        return "en";
    }

    static {
        supportedLocales = new String[]{"en", "bg", "ca", "cs", "da", "de", "es", "fi", "fr", "it", "lv", "nl", "no", "pl", "pt", "pt_BR", "ru", "tr", "uk"};
        translationProgress = new HashMap();
    }
}

