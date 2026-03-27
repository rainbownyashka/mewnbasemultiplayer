/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4;

import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.utils.MusicTrack;
import java.util.ArrayList;
import java.util.Collections;

public class MusicManager {
    private ArrayList<MusicTrack> musicTracks = new ArrayList();
    private int currentTrackIndex = 0;
    private int prevTrackIndex = -1;
    private Music currentTrack;
    private boolean betweenTracks = true;
    private float delayTimer = 0.0f;
    private float delayBeforeNext = 0.0f;
    private float minDelayBetween = 10.0f;
    private float maxDelayBetween = 30.0f;
    private float minStartDelay = 8.0f;
    private int randomTries = 10;
    public boolean shuffling = true;
    float positionTimer = 0.0f;

    public MusicManager() {
        this.setupTracks();
        if (this.shuffling) {
            this.shuffleTracks();
        }
        this.firstTrackDelay();
    }

    private void setupTracks() {
        MusicTrack m4 = new MusicTrack();
        m4.filePath = "music/cynicmusic-mysterious-ambience-song21.mp3";
        m4.artist = "Cynicmusic";
        m4.trackName = "Mysterious Ambience (Song 21)";
        this.musicTracks.add(m4);
        MusicTrack m0 = new MusicTrack();
        m0.filePath = "music/gs/gs - Early.mp3";
        m0.artist = "Gravity Sound";
        m0.trackName = "Early";
        this.musicTracks.add(m0);
        MusicTrack m2 = new MusicTrack();
        m2.filePath = "music/gs/gs - Middle.mp3";
        m2.artist = "Gravity Sound";
        m2.trackName = "Middle";
        this.musicTracks.add(m2);
        MusicTrack m3 = new MusicTrack();
        m3.filePath = "music/dfs/dfs - Stars above.mp3";
        m3.artist = "Dark Fantasy Studio";
        m3.trackName = "Stars Above";
        this.musicTracks.add(m3);
        MusicTrack m6 = new MusicTrack();
        m6.filePath = "music/dfs/dfs - In search for.mp3";
        m6.artist = "Dark Fantasy Studio";
        m6.trackName = "In Search For";
        this.musicTracks.add(m6);
        MusicTrack m11 = new MusicTrack();
        m11.filePath = "music/dfs/dfs - Onirik.mp3";
        m11.artist = "Dark Fantasy Studio";
        m11.trackName = "Onirik";
        this.musicTracks.add(m11);
        for (int i = 1; i <= 10; ++i) {
            if (i == 4 || i == 10) continue;
            MusicTrack m = new MusicTrack();
            m.filePath = "music/pps/pps_sfa2_" + i + ".mp3";
            m.artist = "Phat Phrog Studio";
            m.trackName = "Scifi Ambient " + i;
            this.musicTracks.add(m);
        }
    }

    public void listTracks() {
        int index = 0;
        for (MusicTrack track : this.musicTracks) {
            String msg = index + " - ";
            if (!track.artist.equals("")) {
                msg = msg + track.artist + " - ";
            }
            msg = msg + track.trackName;
            MoonBase.log(msg);
            MessageManager.getInstance().dispatchMessage(999, msg);
            ++index;
        }
    }

    public void shuffleTracks() {
        int i;
        MoonBase.log("Prev Tracks:");
        int i3 = 0;
        for (MusicTrack m : this.musicTracks) {
            MoonBase.log(i3 + " - " + m.trackName);
            ++i3;
        }
        ArrayList<MusicTrack> cloneList = new ArrayList<MusicTrack>();
        cloneList.addAll(this.musicTracks);
        int lastIndex = cloneList.size() - 1;
        for (int i2 = 0; i2 < 3; ++i2) {
            if (lastIndex - i2 < 0) continue;
            cloneList.remove(lastIndex - i2);
        }
        Collections.shuffle(cloneList);
        ArrayList<MusicTrack> newList = new ArrayList<MusicTrack>();
        for (i = 2; i >= 0; --i) {
            if (i >= cloneList.size()) continue;
            newList.add((MusicTrack)cloneList.get(i));
            cloneList.remove(i);
        }
        MoonBase.log("Clonelist size now : " + cloneList.size());
        MoonBase.log("New starting tracks:");
        i = 0;
        for (MusicTrack m : newList) {
            MoonBase.log(i + " - " + m.trackName);
            ++i;
        }
        Collections.shuffle(this.musicTracks);
        for (MusicTrack m : this.musicTracks) {
            if (newList.contains(m)) continue;
            newList.add(m);
        }
        Collections.shuffle(cloneList);
        newList.addAll(cloneList);
        this.musicTracks = newList;
        MoonBase.log("TrackList shuffled:");
        int i2 = 0;
        for (MusicTrack m : this.musicTracks) {
            MoonBase.log(i2 + " - " + m.trackName);
            ++i2;
        }
        this.currentTrackIndex = 0;
        this.prevTrackIndex = -1;
    }

    public void update(float delta) {
        if (this.betweenTracks) {
            this.delayTimer += delta;
            if (this.delayTimer > this.delayBeforeNext) {
                MoonBase.debug("MusicManager: delay finished");
                this.delayTimer = 0.0f;
                this.getNextTrackIndex();
                this.startNextTrack();
                this.betweenTracks = false;
            }
        } else {
            this.positionTimer += delta;
            if (this.positionTimer > 1.0f) {
                this.positionTimer = 0.0f;
                if (this.currentTrack != null) {
                    MoonBase.debug("MusicManager: playing: " + this.currentTrack.getPosition());
                }
            }
        }
    }

    private void getNextTrackIndex() {
        this.prevTrackIndex = this.currentTrackIndex++;
        if (this.currentTrackIndex > this.musicTracks.size() - 1) {
            this.currentTrackIndex = 0;
        }
    }

    private void startNextTrack() {
        MusicTrack track = this.musicTracks.get(this.currentTrackIndex);
        MessageManager.getInstance().dispatchMessage(999, "Now playing: " + track.artist + " - " + track.trackName);
        this.currentTrack = Audio.getInstance().startMusic(track.filePath, 0.5f, false);
        this.currentTrack.setOnCompletionListener(new Music.OnCompletionListener(){

            @Override
            public void onCompletion(Music music) {
                MusicManager.this.nextTrackDelay();
                MusicManager.this.betweenTracks = true;
            }
        });
    }

    private void firstTrackDelay() {
        this.delayBeforeNext = MathUtils.random(this.minDelayBetween, this.maxDelayBetween) + this.minStartDelay;
    }

    private void nextTrackDelay() {
        this.delayBeforeNext = MathUtils.random(this.minDelayBetween, this.maxDelayBetween);
        MoonBase.debug("MusicManager: next delay is " + this.delayBeforeNext);
        if (this.currentTrack != null) {
            this.currentTrack.setOnCompletionListener(null);
        }
    }

    public void dispose() {
        if (this.currentTrack != null) {
            this.currentTrack.stop();
        }
    }

    public void nextTrack() {
        if (this.currentTrack.isPlaying()) {
            this.currentTrack.stop();
        }
        this.delayTimer = 0.0f;
        this.betweenTracks = false;
        this.getNextTrackIndex();
        this.startNextTrack();
    }

    public void playTrackNum(int i) {
        if (this.currentTrack.isPlaying()) {
            this.currentTrack.stop();
        }
        if (i > this.musicTracks.size() - 1) {
            i = 0;
            MoonBase.error("Invalid track index");
        }
        this.delayTimer = 0.0f;
        this.betweenTracks = true;
        this.currentTrackIndex = i;
        this.startNextTrack();
    }
}

