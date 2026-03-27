/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import box2dLight.PointLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.cairn4.moonbase.AdditiveImage;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.ScienceLabBehavior;
import com.cairn4.moonbase.ui.ResearchPopup;
import java.util.Observable;
import java.util.Observer;

public class ScienceLab
extends BaseModule
implements Observer {
    private static final Color progMeterOnColor = Color.valueOf("0fa829");
    private static final Color progMeterOffColor = Color.valueOf("a8330f");
    private PointLight glowLight;
    public ScienceLabBehavior scienceLabBehavior;
    private Image buildFinishedIcon;
    Group beamGroup;
    Image progMeter;
    Image progMeterBg;

    public ScienceLab(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerPriority = 3;
        this.powerDrawRate = 5.0f;
        this.powerGenRate = 0.0f;
        this.scienceLabBehavior = new ScienceLabBehavior();
        this.scienceLabBehavior.baseModule = this;
        this.behaviorList.add(this.scienceLabBehavior);
        this.scienceLabBehavior.addObserver(this);
        this.setupPhysics("crafting-station");
        this.buildFinishedIcon = new Image(world.gameScreen.skin.getDrawable("crafting-notification"));
        this.buildFinishedIcon.setOrigin(4);
        this.buildFinishedIcon.setScale(0.3f);
        this.buildFinishedIcon.setPosition(this.getWorldXPos() + TILE_SIZE / 2.0f, this.getWorldYPos() + TILE_SIZE * 0.7f, 4);
        world.gameScreen.worldUiGroup.addActor(this.buildFinishedIcon);
        this.scienceLabBehavior.setFinishedIcon(this.buildFinishedIcon);
        this.updateBases();
    }

    @Override
    public float getPowerDrawRate() {
        if (this.scienceLabBehavior != null && this.scienceLabBehavior.researching) {
            return this.powerDrawRate;
        }
        return 2.0f;
    }

    @Override
    public void startBehaviors() {
        this.scienceLabBehavior.start();
        if (this.scienceLabBehavior != null && this.scienceLabBehavior.researching) {
            this.startResearchAnim();
        }
    }

    private void startResearchAnim() {
        if (this.hasPower) {
            this.beamGroup.setVisible(true);
        }
        if (this.image != null) {
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("scilab-active"));
        }
        if (this.group != null) {
            this.group.addAction(Actions.forever(Actions.sequence((Action)Actions.scaleTo(1.0f, 0.99f, 0.2f), (Action)Actions.scaleTo(1.0f, 1.007f, 0.2f))));
        }
        this.beamGroup.clearActions();
        this.beamGroup.addAction(Actions.sequence((Action)Actions.alpha(0.0f), (Action)Actions.moveToAligned(Tile.TILE_SIZE / 2.0f, 40.0f, 1, 1.0f, Interpolation.sine), (Action)Actions.fadeIn(0.1f), (Action)Actions.forever(Actions.sequence((Action)Actions.parallel((Action)Actions.moveToAligned(Tile.TILE_SIZE / 2.0f, 62.0f, 1, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(1.05f, 1.05f, 1.0f, Interpolation.sine)), (Action)Actions.parallel((Action)Actions.moveToAligned(Tile.TILE_SIZE / 2.0f, 40.0f, 1, 1.0f, Interpolation.sine), (Action)Actions.scaleTo(1.0f, 1.0f, 1.0f, Interpolation.sine))))));
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "sciencelab";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("scilab", this.world.gameScreen.mainGroup);
        this.beamGroup = new Group();
        this.group.addActor(this.beamGroup);
        AdditiveImage beam = new AdditiveImage(this.world.gameScreen.skin.getDrawable("scilab-beam"));
        beam.setSize(beam.getWidth(), 70.0f);
        beam.setPosition(0.0f, 0.0f, 1);
        this.beamGroup.addActor(beam);
        beam.addAction(Actions.forever(Actions.sequence((Action)Actions.alpha(0.7f, 0.3f), (Action)Actions.alpha(0.25f, 0.3f))));
        Image beamSides = new Image(this.world.gameScreen.skin.getDrawable("scilab-beam-sides"));
        beamSides.setPosition(0.0f, 0.0f, 1);
        this.beamGroup.addActor(beamSides);
        this.beamGroup.setPosition(Tile.TILE_SIZE / 2.0f, 38.0f, 1);
        this.beamGroup.setVisible(false);
        this.beamGroup.getColor().a = 0.0f;
        this.progMeter = new Image(this.world.gameScreen.skin.getDrawable("white"));
        this.progMeter.setSize(50.0f, 15.0f);
        this.progMeter.setColor(progMeterOnColor);
        this.progMeter.setPosition(TILE_SIZE / 2.0f, 87.0f, 4);
        this.group.addActor(this.progMeter);
        this.progMeter.toBack();
        this.progMeterBg = new Image(this.world.gameScreen.skin.getDrawable("white"));
        this.progMeterBg.setSize(50.0f, 15.0f);
        this.progMeterBg.setColor(Color.valueOf("042209"));
        this.progMeterBg.setPosition(TILE_SIZE / 2.0f, 87.0f, 4);
        this.group.addActor(this.progMeterBg);
        this.progMeterBg.toBack();
        this.glowLight = new PointLight(this.world.rayHandler, 8, new Color(0.0f, 1.0f, 0.0f, 1.0f), 0.390625f, this.getXCenter() / 256.0f, (this.getYCenter() - 20.0f) / 256.0f);
        this.glowLight.setXray(true);
        this.glowLight.setActive(true);
        this.glowLight.setIgnoreAttachedBody(true);
    }

    @Override
    public void interact(Player player) {
        this.world.gameScreen.showMenu(new ResearchPopup(this.world.gameScreen, this.scienceLabBehavior, player));
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (this.hasPower) {
            for (Behavior b : this.behaviorList) {
                b.update(delta);
            }
        }
        if (this.scienceLabBehavior != null) {
            if (this.scienceLabBehavior.researching) {
                this.glowLight.setActive(this.hasPower);
                this.progMeter.setScaleX(this.scienceLabBehavior.timer / 120.0f);
            } else {
                this.progMeter.setScaleX(1.0f);
                this.glowLight.setActive(false);
            }
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        if (o == "startResearch") {
            System.out.println("startResearch");
            this.startResearchAnim();
        }
        if (o == "done") {
            this.clearResearchAnim();
            this.image.setDrawable(this.world.gameScreen.skin.getDrawable("scilab"));
        }
    }

    private void clearResearchAnim() {
        this.beamGroup.clearActions();
        this.beamGroup.addAction(Actions.sequence((Action)Actions.fadeOut(0.5f), (Action)Actions.visible(false)));
        this.group.clearActions();
        this.group.setScale(1.0f, 1.0f);
    }

    @Override
    public void dropPickup() {
        this.scienceLabBehavior.dropItems(this.world, this.chunk, this.getWorldXPos(), this.getWorldYPos());
        super.dropPickup();
    }

    @Override
    public void destroy() {
        this.buildFinishedIcon.remove();
        this.glowLight.remove();
        super.destroy();
    }

    @Override
    public void setPower(boolean p) {
        if (p && !this.hasPower) {
            this.beamGroup.setVisible(true);
            this.progMeter.setColor(progMeterOnColor);
            if (this.scienceLabBehavior != null && this.scienceLabBehavior.researching) {
                this.startResearchAnim();
            }
        } else if (!p && this.hasPower) {
            this.beamGroup.setVisible(false);
            this.progMeter.setColor(progMeterOffColor);
            if (this.scienceLabBehavior != null && this.scienceLabBehavior.researching) {
                this.clearResearchAnim();
            }
        }
        super.setPower(p);
    }
}

