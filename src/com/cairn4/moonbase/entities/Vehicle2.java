/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.ParticleActor;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Vehicle;
import com.cairn4.moonbase.tiles.Tile;
import java.util.HashMap;

public class Vehicle2
extends Vehicle {
    public Vehicle2(World world, float xPos, float yPos, float rotation) {
        super(world, xPos, yPos, rotation, "rover");
        this.setState(Vehicle.STATES.empty);
    }

    @Override
    public void createDrawable(String sprite) {
        super.createDrawable(sprite);
        this.image = new Image(this.world.gameScreen.skin.getDrawable(sprite));
        this.image.setSize(128.0f, 128.0f);
        this.image.setOrigin(1);
        this.image.setPosition(0.0f, 0.0f, 1);
        this.group.addActor(this.image);
        this.image.setTouchable(Touchable.enabled);
        this.image.addListener(new ClickListener(){

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Gdx.app.debug("MewnBase", "Buggie image click touchdown");
                Vehicle2.this.setClicked();
                return false;
            }
        });
        this.headLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-headlights"));
        this.headLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.headLightSprite.setOrigin(1);
        this.headLightSprite.setPosition(0.0f, 0.0f, 1);
        this.headLightSprite.setTouchable(Touchable.disabled);
        this.group.addActor(this.headLightSprite);
        this.tailLightSprite = new AdditiveImage(this.world.gameScreen.skin.getDrawable("buggie-taillight"));
        this.tailLightSprite.setSize(Tile.TILE_SIZE, Tile.TILE_SIZE);
        this.tailLightSprite.setOrigin(1);
        this.tailLightSprite.setPosition(0.0f, 0.0f, 1);
        this.tailLightSprite.setTouchable(Touchable.disabled);
        this.group.addActor(this.tailLightSprite);
    }

    @Override
    public HashMap<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("health", Float.valueOf(this.health));
        return properties;
    }

    @Override
    public void setState(Vehicle.STATES newState) {
        if (newState == Vehicle.STATES.inUse) {
            this.engineSoundId = Audio.getInstance().playSoundLoop(this.engineSound, 0.35f, 1.0f, 0.0f);
            this.dustTrail.pfx.start();
            this.toggleAllLights(true);
            this.toggleDriverHead(true);
        } else if (newState == Vehicle.STATES.empty) {
            this.engineSound.stop();
            this.setAcceleration(Vehicle.acceleration.none);
            this.dustTrail.pfx.allowCompletion();
            this.toggleAllLights(false);
            this.toggleDriverHead(false);
        }
        super.setState(newState);
    }

    @Override
    public void playerActionSecondary(Player player) {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        float pitch = this.getSpeedKMH() / this.vd.maxSpeed + 0.3f;
        this.engineSound.setPitch(this.engineSoundId, pitch);
        this.dustTrail.setPosition(this.group.getX(), this.group.getY());
        if (this.health < this.vd.damagedThreshold) {
            this.damageSmoke.setPosition(this.group.getX(), this.group.getY());
        }
    }

    @Override
    public void remove() {
        this.damageSmoke.pfx.allowCompletion();
        Group fxGroup = new Group();
        this.world.gameScreen.topGroup.addActor(fxGroup);
        fxGroup.setPosition(this.getXPos(), this.getYPos());
        fxGroup.addAction(Actions.sequence((Action)Actions.delay(3.0f), (Action)Actions.removeActor()));
        ParticleActor particleExplosion = new ParticleActor(Gdx.files.internal("particles/meteor-explosion.p"), this.world.gameScreen.atlas, true);
        particleExplosion.pfx.start();
        fxGroup.addActor(particleExplosion);
        ParticleActor fireParticle = new ParticleActor(Gdx.files.internal("particles/base-fire.p"), this.world.gameScreen.atlas, true);
        fireParticle.pfx.start();
        fxGroup.addActor(fireParticle);
        this.world.gameScreen.cameraShake.shake(10.0f, 60.0f, 0.4f);
        super.remove();
    }
}

