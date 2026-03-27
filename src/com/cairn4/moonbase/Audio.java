/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AssetManagerSingleton;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.SettingsLoader;
import com.cairn4.moonbase.World;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Audio {
    private static MoonBase game;
    private static Audio instance;
    private static Music music;
    private HashMap<Sound, ArrayList<Object>> loopingVolumeMap = new HashMap();
    public static float SOUNDFX_VOLUME;
    public static float MUSIC_VOLUME;
    private static float DEFAULT_PAN_SPREAD;
    private Vector2 listenerPos = new Vector2(0.0f, 0.0f);

    private Audio() {
    }

    public static void setup(MoonBase game1) {
        game = game1;
        instance = new Audio();
        SOUNDFX_VOLUME = MathUtils.clamp(SettingsLoader.getInstance().settingsData.SOUNDFX_VOLUME.floatValue(), 0.0f, 1.0f);
        MUSIC_VOLUME = MathUtils.clamp(SettingsLoader.getInstance().settingsData.MUSIC_VOLUME.floatValue(), 0.0f, 1.0f);
        Gdx.app.debug("MewnBase", "Audio: setting volumes to " + SOUNDFX_VOLUME + ", " + MUSIC_VOLUME);
    }

    public static Audio getInstance() {
        if (instance == null) {
            instance = new Audio();
        }
        return instance;
    }

    public float playerDistanceMultiplier(World world, Vector2 soundSource, float radius, float maxVolume) {
        return this.playerDistanceMultiplier(world, soundSource.x, soundSource.y, radius, maxVolume);
    }

    public float playerDistanceMultiplier(World world, float sourceX, float sourceY, float radius, float maxVolume) {
        this.listenerPos.set(0.0f, 0.0f);
        if (world.player != null) {
            this.listenerPos.set(world.player.getXPos(), world.player.getYPos());
        } else if (world.gameScreen.camera != null) {
            this.listenerPos.set(world.gameScreen.camera.position.x, world.gameScreen.camera.position.y);
        } else {
            Gdx.app.error("MoonBase", "Audio: null game camera for some reason.");
        }
        float distance = this.listenerPos.dst(sourceX, sourceY);
        if (distance <= radius) {
            float p = 1.0f - distance / radius;
            return maxVolume * p;
        }
        return 0.0f;
    }

    public float playerDistancePan(World world, Vector2 soundSource) {
        return this.playerDistancePan(world, soundSource.x, soundSource.y);
    }

    public float playerDistancePan(World world, float soundSourceX, float soundSourceY) {
        this.listenerPos.set(0.0f, 0.0f);
        if (world.player != null) {
            this.listenerPos.set(world.player.getXPos(), world.player.getYPos());
        } else if (world.gameScreen.camera != null) {
            this.listenerPos.set(world.gameScreen.camera.position.x, world.gameScreen.camera.position.y);
        } else {
            Gdx.app.error("MoonBase", "Audio: null game camera for some reason.");
        }
        return MathUtils.clamp((soundSourceX - this.listenerPos.x) / DEFAULT_PAN_SPREAD, -1.0f, 1.0f);
    }

    public long playSound(String file, float volume, float pitch) {
        Sound s = AssetManagerSingleton.getInstance().get(file, Sound.class);
        long id = s.play(volume * SOUNDFX_VOLUME, pitch, 0.0f);
        return id;
    }

    public long playSound(String file, float volume, float pitch, float pan) {
        Sound s = AssetManagerSingleton.getInstance().get(file, Sound.class);
        long id = s.play(volume * SOUNDFX_VOLUME, pitch, pan);
        return id;
    }

    public long playSound(String file) {
        return this.playSound(file, 1.0f * SOUNDFX_VOLUME, 1.0f);
    }

    public long playSound(String file, float volume) {
        return this.playSound(file, volume * SOUNDFX_VOLUME, 1.0f);
    }

    public Music startMusic(String file, float volume, boolean loop) {
        music = AssetManagerSingleton.getInstance().get(file, Music.class);
        music.setVolume(volume * MUSIC_VOLUME);
        music.setLooping(loop);
        music.play();
        return music;
    }

    public void toggleMusic() {
        if (music.isPlaying()) {
            music.pause();
        } else {
            music.play();
        }
    }

    public void stopMusic() {
        if (music != null && music.isPlaying()) {
            music.stop();
        }
    }

    public void setupButtonSounds(Button button) {
        button.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int btn) {
                Audio.this.playSound("sounds/menubutton_down.ogg", 0.08f, 1.0f);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int btn) {
                Audio.this.playSound("sounds/menubutton_up.ogg", 0.08f, 1.0f);
            }
        });
    }

    public Sound getSound(String fileName) {
        return AssetManagerSingleton.getInstance().get(fileName, Sound.class);
    }

    public void setSoundVolume(float volume) {
        SOUNDFX_VOLUME = MathUtils.clamp(volume, 0.0f, 1.0f);
        for (Map.Entry<Sound, ArrayList<Object>> entry : this.loopingVolumeMap.entrySet()) {
            entry.getKey().setVolume(Long.parseLong(entry.getValue().get(0).toString()), Float.parseFloat(entry.getValue().get(1).toString()) * SOUNDFX_VOLUME);
        }
    }

    public void setMusicVolume(float volume) {
        MUSIC_VOLUME = MathUtils.clamp(volume, 0.0f, 1.0f);
        if (music != null && music.isPlaying()) {
            music.setVolume(0.5f * volume);
        }
    }

    public long playSoundLoop(Sound sound, float volume, float pitch, float pan) {
        long id = sound.loop(volume * SOUNDFX_VOLUME, pitch, pan);
        ArrayList<Number> params = new ArrayList<Number>();
        params.clear();
        params.add(id);
        params.add(Float.valueOf(volume));
        params.add(Float.valueOf(pitch));
        params.add(Float.valueOf(pan));
        this.loopingVolumeMap.put(sound, params);
        return id;
    }

    public void stopLoopSound(Sound ambientWorldSound, long ambientWorldSoundId) {
        Iterator<Map.Entry<Sound, ArrayList<Object>>> loopVolumeIterator = this.loopingVolumeMap.entrySet().iterator();
        while (loopVolumeIterator.hasNext()) {
            long id;
            Map.Entry<Sound, ArrayList<Object>> entry = loopVolumeIterator.next();
            if (!entry.getKey().equals(ambientWorldSound) || (id = Long.valueOf(entry.getValue().get(0).toString()).longValue()) != ambientWorldSoundId) continue;
            ambientWorldSound.stop(id);
            entry.getValue().clear();
            loopVolumeIterator.remove();
        }
    }

    public void updateLoopingSoundVolume(Sound targetSoundFx, long targetSoundFxId, float newVolume, float newPan) {
        for (Map.Entry<Sound, ArrayList<Object>> s : this.loopingVolumeMap.entrySet()) {
            long id;
            if (!s.getKey().equals(targetSoundFx) || targetSoundFxId != (id = Long.parseLong(s.getValue().get(0).toString()))) continue;
            ArrayList<Object> params = s.getValue();
            params.set(1, Float.valueOf(newVolume));
            params.set(2, Float.valueOf(newPan));
            s.setValue(params);
            s.getKey().setPan(id, newPan, newVolume * SOUNDFX_VOLUME);
            break;
        }
    }

    static {
        SOUNDFX_VOLUME = 0.1f;
        MUSIC_VOLUME = 0.0f;
        DEFAULT_PAN_SPREAD = (float)MoonBase.SCREEN_WIDTH * 0.6f;
    }
}

