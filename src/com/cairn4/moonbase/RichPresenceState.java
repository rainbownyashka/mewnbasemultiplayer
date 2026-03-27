/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.cairn4.moonbase.Mission;

public class RichPresenceState {
    public Mission.MissionTypes missionType;
    public States state = States.menu;
    public int dayNum = -1;
    public boolean resetTimer = false;

    public String getDetailsText() {
        switch (this.state) {
            case menu: {
                return "In the menu.";
            }
            case playing: {
                switch (this.missionType) {
                    case tutorial: {
                        return "Playing tutorial!";
                    }
                    case normal: {
                        return "Playing | " + this.getDayNum();
                    }
                    case endless: {
                        return "Playing | " + this.getDayNum();
                    }
                    case creative: {
                        return "Playing | " + this.getDayNum();
                    }
                }
            }
        }
        return null;
    }

    private String getDayNum() {
        return "Day " + this.dayNum;
    }

    public static enum States {
        menu,
        playing;

    }
}

