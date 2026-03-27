/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Mission;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.ui.Localization;

public class DayCycle {
    World world;
    Mission.dayCycleModes dayCycleMode;
    public float timeMultiplier = 1.0f;
    private static float dayCycleLength = 360.0f;
    private float dayCycleTimer = 0.0f;
    public static final float fullDayLength = 360.0f;
    private float dayLength;
    private float duskLength;
    private float nightLength;
    private float dawnLength;
    float timer;
    float timerEnd;
    Color dayColor = new Color(0.95f, 0.95f, 0.95f, 1.0f);
    Color nightColor = new Color(0.0f, 0.01f, 0.02f, 1.0f);
    private Color ambientLightColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private int dayNum;
    public Periods currentPeriod;
    Color lerpdColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    float daylightTransitionProgress;

    public Color getAmbientLightColor() {
        return this.ambientLightColor;
    }

    public int getDay() {
        return this.dayNum;
    }

    public DayCycle(World world) {
        this.world = world;
        // Default to standard day cycle mode to avoid null in callers
        try { this.dayCycleMode = Mission.dayCycleModes.defaultDay; } catch (Exception ignored) {}
        this.setPeriod(Periods.day);
        this.dayNum = 1;
        this.timer = 0.0f;
        this.timerEnd = this.dayLength;
        this.dayCycleTimer = 0.0f;
    }

    public float dayNightPercent() {
        switch (this.dayCycleMode) {
            case onlyDay: {
                return 1.0f;
            }
            case onlyNight: {
                return 0.0f;
            }
        }
        return this.dayLength / (this.dayLength + this.duskLength + this.nightLength + this.dawnLength);
    }

    public float getDayProgress() {
        float p = 0.0f;
        block0 : switch (this.dayCycleMode) {
            case onlyDay: {
                p = this.dayCycleTimer;
                break;
            }
            case onlyNight: {
                p = this.dayCycleTimer;
                break;
            }
            default: {
                switch (this.currentPeriod) {
                    case day: {
                        p = this.timer;
                        break block0;
                    }
                    case dusk: {
                        p = this.dayLength + this.timer;
                        break block0;
                    }
                    case night: {
                        p = this.dayLength + this.duskLength + this.timer;
                        break block0;
                    }
                    case dawn: {
                        p = this.dayLength + this.duskLength + this.nightLength + this.timer;
                    }
                }
            }
        }
        return p / dayCycleLength;
    }

    private Color lerpColor(Color c1, Color c2, float alpha) {
        float invAlpha = 1.0f - alpha;
        float r = c1.r * invAlpha + c2.r * alpha;
        float g = c1.g * invAlpha + c2.g * alpha;
        float b = c1.b * invAlpha + c2.b * alpha;
        float a = c1.a * invAlpha + c2.a * alpha;
        this.lerpdColor.set(r, g, b, a);
        return this.lerpdColor;
    }

    public void update(float delta) {
        if (this.dayCycleMode == Mission.dayCycleModes.onlyNight || this.dayCycleMode == Mission.dayCycleModes.onlyDay) {
            this.dayCycleTimer += delta * this.timeMultiplier;
            if (this.dayCycleTimer >= dayCycleLength) {
                this.dayCycleTimer = 0.0f;
                this.changeDay();
                this.world.gameScreen.hud.showDay(this.dayNum);
            }
        } else {
            this.timer += delta * this.timeMultiplier;
            if (this.timer >= this.timerEnd) {
                this.changePeriod();
            }
            this.daylightTransitionProgress = this.timer / this.timerEnd;
            if (this.currentPeriod == Periods.dusk) {
                this.ambientLightColor.set(this.lerpColor(this.dayColor, this.nightColor, this.daylightTransitionProgress));
                this.world.rayHandler.setAmbientLight(this.ambientLightColor);
            }
            if (this.currentPeriod == Periods.dawn) {
                this.ambientLightColor.set(this.lerpColor(this.nightColor, this.dayColor, this.daylightTransitionProgress));
                this.world.rayHandler.setAmbientLight(this.ambientLightColor);
            }
        }
    }

    private void changePeriod() {
        switch (this.currentPeriod) {
            case day: {
                this.setPeriod(Periods.dusk);
                break;
            }
            case dusk: {
                this.setPeriod(Periods.night);
                break;
            }
            case night: {
                this.setPeriod(Periods.dawn);
                break;
            }
            case dawn: {
                this.setPeriod(Periods.day);
                this.changeDay();
                this.world.gameScreen.hud.showDay(this.dayNum);
            }
        }
    }

    public void setDay(int day) {
        this.dayNum = day;
        if (this.world.gameScreen.hud != null) {
            this.world.gameScreen.hud.showDay(this.dayNum);
        }
        this.checkForMissionWin();
    }

    public void setPeriod(String period) {
        try {
            Periods p = Periods.valueOf(period);
            this.setPeriod(p);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void setTime(float time) {
        this.timer = time;
    }

    private void setPeriod(Periods newPeriod) {
        this.timer = 0.0f;
        this.currentPeriod = newPeriod;
        switch (this.currentPeriod) {
            case day: {
                this.timerEnd = this.dayLength;
                this.world.rayHandler.setAmbientLight(this.dayColor);
                break;
            }
            case dusk: {
                this.timerEnd = this.duskLength;
                this.world.rayHandler.setAmbientLight(this.dayColor);
                break;
            }
            case night: {
                this.timerEnd = this.nightLength;
                this.world.rayHandler.setAmbientLight(this.nightColor);
                break;
            }
            case dawn: {
                this.timerEnd = this.dawnLength;
                this.world.rayHandler.setAmbientLight(this.nightColor);
            }
        }
        Gdx.app.log("MewnBase", "DayCycle: Period is now: " + (Object)((Object)this.currentPeriod));
    }

    private void changeDay() {
        ++this.dayNum;
        Gdx.app.log("MewnBase", "----------------------\n\nDayCycle: Day: " + this.dayNum);
        this.world.gameScreen.updateRichPresence();
        this.world.gameScreen.gameLoader.autoSave(this.world);
        this.world.gameScreen.hud.hudNotifications.newMessage("hud/save-icon", Localization.get("autoSaveNotification"));
        this.checkForMissionWin();
    }

    private void checkForMissionWin() {
        if (!MoonBase.getCurrentMission().missionCompleteReady && this.dayNum >= MoonBase.getCurrentMission().getMissionDayLength() + 1) {
            MoonBase.getCurrentMission().setMissionCompleteReady();
        }
        MoonBase.achievementAdapter.daySurvived(MoonBase.getCurrentMission(), this.dayNum);
    }

    public float getDayLight() {
        switch (this.currentPeriod) {
            case day: {
                return 1.0f;
            }
            case dusk: {
                return 1.0f - this.timer / this.timerEnd;
            }
            case night: {
                return 0.0f;
            }
            case dawn: {
                return this.timer / this.timerEnd;
            }
        }
        return 0.0f;
    }

    public String getClockTime() {
        float timeNum = this.timer;
        if (this.dayCycleMode == Mission.dayCycleModes.defaultDay || this.dayCycleMode == Mission.dayCycleModes.longNight) {
            if (this.currentPeriod == Periods.dusk) {
                timeNum += this.dayLength;
            } else if (this.currentPeriod == Periods.night) {
                timeNum += this.dayLength + this.duskLength;
            } else if (this.currentPeriod == Periods.dawn) {
                timeNum += this.dayLength + this.duskLength + this.nightLength;
            }
        } else {
            timeNum = this.dayCycleTimer;
        }
        int minPerHour = 6;
        int hoursPerDay = 12;
        int minPerDay = minPerHour * hoursPerDay;
        int hours = MathUtils.floor(timeNum / dayCycleLength * (float)hoursPerDay);
        int totalMin = MathUtils.floor(timeNum / dayCycleLength * (float)minPerDay);
        int min = totalMin - hours * minPerHour;
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append(hours);
        sb.append(":");
        sb.append(min);
        sb.append("0");
        return sb.toString();
    }

    public static String convertToClockString(Mission.dayCycleModes dayCycleMode, Periods current, float periodTime) {
        float timeNum = periodTime;
        if (dayCycleMode == Mission.dayCycleModes.defaultDay) {
            if (current == Periods.dusk) {
                timeNum += 240.0f;
            } else if (current == Periods.night) {
                timeNum += 255.0f;
            } else if (current == Periods.dawn) {
                timeNum += 345.0f;
            }
        } else if (dayCycleMode == Mission.dayCycleModes.longNight) {
            if (current == Periods.dusk) {
                timeNum += 140.0f;
            } else if (current == Periods.night) {
                timeNum += 155.0f;
            } else if (current == Periods.dawn) {
                timeNum += 345.0f;
            }
        } else {
            timeNum = periodTime;
        }
        int minPerHour = 6;
        int hoursPerDay = 12;
        int minPerDay = minPerHour * hoursPerDay;
        int hours = MathUtils.floor(timeNum / dayCycleLength * (float)hoursPerDay);
        int totalMin = MathUtils.floor(timeNum / dayCycleLength * (float)minPerDay);
        int min = totalMin - hours * minPerHour;
        return "" + hours + ":" + min + "0";
    }

    public void setDayCycleMode(Mission.dayCycleModes newMode) {
        this.dayCycleMode = newMode;
        Gdx.app.log("MewnBase", "Setting dayCycle to : " + (Object)((Object)newMode));
        switch (newMode) {
            case onlyDay: {
                this.setPeriod(Periods.day);
                break;
            }
            case defaultDay: {
                this.dayLength = 240.0f;
                this.duskLength = 15.0f;
                this.nightLength = 90.0f;
                this.dawnLength = 15.0f;
                this.setPeriod(Periods.day);
                break;
            }
            case longNight: {
                this.dayLength = 140.0f;
                this.duskLength = 15.0f;
                this.nightLength = 190.0f;
                this.dawnLength = 15.0f;
                this.setPeriod(Periods.day);
                break;
            }
            case onlyNight: {
                this.setPeriod(Periods.night);
            }
        }
    }

    public static enum Periods {
        day,
        dusk,
        night,
        dawn;

    }
}

