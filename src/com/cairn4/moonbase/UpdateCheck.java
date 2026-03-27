/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.cairn4.moonbase.SettingsLoader;
import java.util.Observable;

public class UpdateCheck
extends Observable {
    public static boolean UPDATE_AVAILABLE = false;
    public static boolean SHOW_WHATSNEW = false;

    public static void sendRequest() {
        Net.HttpRequest request = new Net.HttpRequest("GET");
        String url = "https://itch.io/api/1/x/wharf/latest?target=cairn4/mewnbase&channel_name=win";
        request.setUrl("https://itch.io/api/1/x/wharf/latest?target=cairn4/mewnbase&channel_name=win");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Accept", "application/json");
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener(){

            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                if (statusCode != 200) {
                    System.out.println("Request Failed");
                    return;
                }
                String responseJson = httpResponse.getResultAsString();
                try {
                    JsonReader jr = new JsonReader();
                    JsonValue serverJson = jr.parse(responseJson);
                    String serverVersion = serverJson.get("latest").asString();
                    UpdateCheck.isUpdateAvailable("1.0.1", serverVersion);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("MewnBase", "UpdateCheck: Update check failed.");
            }

            @Override
            public void cancelled() {
                Gdx.app.log("MewnBase", "UpdateCheck: request cancelled");
            }
        });
    }

    private static int[] parseVersionNumber(String versionText) {
        String[] separatedStrings = versionText.split("\\.");
        int[] versionNums = new int[]{0, 0, 0};
        for (int i = 0; i < separatedStrings.length; ++i) {
            try {
                versionNums[i] = Integer.valueOf(separatedStrings[i]);
                continue;
            }
            catch (NumberFormatException e) {
                Gdx.app.error("MewnBase", "UpdateCheck: version string has non-numbers in it: " + separatedStrings[i]);
            }
        }
        return versionNums;
    }

    private static boolean versionCheck(String version1, String version2) {
        int[] serverVersionNums;
        boolean updateAvailable = false;
        int[] localVersionNums = UpdateCheck.parseVersionNumber(version1);
        if (localVersionNums[0] < (serverVersionNums = UpdateCheck.parseVersionNumber(version2))[0]) {
            updateAvailable = true;
        } else if (localVersionNums[0] == serverVersionNums[0]) {
            if (localVersionNums[1] < serverVersionNums[1]) {
                updateAvailable = true;
            } else if (localVersionNums[1] == serverVersionNums[1] && localVersionNums[2] < serverVersionNums[2]) {
                updateAvailable = true;
            }
        }
        return updateAvailable;
    }

    public static boolean isUpdateAvailable(String localVersion, String serverVersion) {
        boolean updateAvailable = UpdateCheck.versionCheck(localVersion, serverVersion);
        String message = "Game is up to date!";
        if (updateAvailable) {
            message = "A new update is available! (" + serverVersion + ") - you have " + localVersion;
        }
        if (Gdx.app != null) {
            Gdx.app.log("MewnBase", "UpdateCheck: " + message);
        }
        UPDATE_AVAILABLE = updateAvailable;
        return UPDATE_AVAILABLE;
    }

    public static void whatsNewCheck() {
        String lastVersion = SettingsLoader.getInstance().settingsData.LAST_WHATSNEW_VERSION;
        if (lastVersion == null) {
            lastVersion = "0.0";
        }
        SHOW_WHATSNEW = UpdateCheck.versionCheck(lastVersion, "1.0.1");
    }

    public static boolean whatsNewCheckTest(String lastNewVersion, String appVersion) {
        if (lastNewVersion == null) {
            lastNewVersion = "0.0";
        }
        return UpdateCheck.versionCheck(lastNewVersion, appVersion);
    }
}

