/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.Audio;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.Tile;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.tiles.behaviors.CraftingBotBehavior;
import com.cairn4.moonbase.ui.CraftingBotUI;

public class CraftingBot
extends BaseModule {
    public CraftingBotBehavior craftingBotBehavior;
    Label debugLabel;
    public Group topGroup;
    public Group armGroup;
    public Image panelImage;
    public Image armImage;
    public Image itemImage;
    private float itemXPos = 100.0f;
    private Image clawTop;
    private Image clawBot;
    private Group forearmGroup;
    private float animSpeed = 0.25f;
    private Image headImage;
    private Image forearmImage;
    private Image arrow;
    private Group headGroup;
    Vector2 targetBasePos = new Vector2(0.0f, 0.0f);
    int panelIndex = 1;
    int panelImages = 4;
    float panelTimer = 0.0f;
    float panelDelay = 0.3f;
    float turnSpeed = 55.0f;
    Vector2 facing = new Vector2(1.0f, 0.0f);

    public CraftingBot(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.powerDrawRate = 4.0f;
        this.powerGenRate = 0.0f;
        this.setupPhysics("rtg");
        this.updateBases();
        this.craftingBotBehavior = new CraftingBotBehavior();
        this.craftingBotBehavior.baseModule = this;
        this.behaviorList.add(this.craftingBotBehavior);
        this.craftingBotBehavior.debugLabel = this.debugLabel;
        this.craftingBotBehavior.armGroup = this.armGroup;
    }

    @Override
    public void startBehaviors() {
        this.craftingBotBehavior.start();
    }

    @Override
    public boolean usesComms() {
        return false;
    }

    @Override
    public boolean usesBaseGroup() {
        return true;
    }

    @Override
    public boolean visuallyConnectToConduits() {
        return true;
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "autocrafter";
    }

    @Override
    protected void createDrawables() {
        super.createDrawables("autocrafter-base", this.world.gameScreen.mainGroup);
        this.group.setUserObject(Float.valueOf((float)this.worldY * TILE_SIZE + TILE_SIZE / 4.0f));
        this.image.setSize(128.0f, 114.0f);
        this.image.setOrigin(1);
        this.image.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f, 1);
        this.panelImage = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-panel1"));
        this.panelImage.setPosition(73.0f, 26.0f);
        this.group.addActor(this.panelImage);
        this.debugLabel = new Label((CharSequence)"DEBUG", this.world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.27f);
        this.debugLabel.setAlignment(12, 12);
        this.debugLabel.setPosition(this.getWorldXPos(), this.getYCenter() + 100.0f, 4);
        this.debugLabel.setVisible(false);
        this.debugLabel.setTouchable(Touchable.disabled);
        this.topGroup = new Group();
        this.topGroup.setPosition(Tile.TILE_SIZE / 2.0f, Tile.TILE_SIZE / 2.0f);
        this.group.addActor(this.topGroup);
        this.armGroup = new Group();
        this.topGroup.addActor(this.armGroup);
        this.armGroup.setPosition(0.0f, 15.0f);
        this.forearmGroup = new Group();
        this.forearmGroup.setPosition(0.0f, 0.0f);
        this.armGroup.addActor(this.forearmGroup);
        this.clawTop = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-arm-claw"));
        this.clawTop.setSize(37.0f, 25.0f);
        this.clawTop.setOrigin(12);
        this.clawTop.setPosition(45.0f, 1.0f);
        this.forearmGroup.addActor(this.clawTop);
        this.clawBot = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-arm-claw"));
        this.clawBot.setSize(37.0f, -25.0f);
        this.clawBot.setOrigin(12);
        this.clawBot.setPosition(45.0f, -1.0f);
        this.forearmGroup.addActor(this.clawBot);
        this.forearmImage = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-arm-end"));
        this.forearmImage.setSize(64.0f, 20.0f);
        this.forearmImage.setPosition(0.0f, 0.0f, 8);
        this.forearmGroup.addActor(this.forearmImage);
        this.armImage = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-arm"));
        this.armImage.setSize(73.0f, 40.0f);
        this.armImage.setOrigin(30.0f, 20.0f);
        this.armImage.setPosition(-this.armImage.getOriginX(), -this.armImage.getOriginY());
        this.armGroup.addActor(this.armImage);
        this.headGroup = new Group();
        this.headGroup.setPosition(0.0f, -15.0f);
        this.topGroup.addActor(this.headGroup);
        this.headImage = new Image(this.world.gameScreen.skin.getDrawable("autocrafter-head"));
        this.headImage.setSize(75.0f, 115.0f);
        this.headImage.setPosition(0.0f, 0.0f, 4);
        this.headGroup.addActor(this.headImage);
        this.itemImage = new Image(this.world.gameScreen.skin.getDrawable("metal-refined"));
        this.itemImage.setSize(64.0f, 64.0f);
        this.itemImage.setOrigin(1);
        this.itemImage.setPosition(this.itemXPos, 0.0f, 1);
        this.forearmGroup.addActor(this.itemImage);
        this.itemImage.setColor(1.0f, 1.0f, 1.0f, 0.0f);
        this.itemImage.setVisible(false);
        this.world.gameScreen.worldUIStage.addActor(this.debugLabel);
        this.closeClaw(0.0f);
    }

    public void setArmImage(String itemId) {
        this.itemImage.setVisible(true);
        this.itemImage.setDrawable(this.world.gameScreen.skin.getDrawable(ItemFactory.getItemSprite(itemId)));
    }

    public void setArmDirection(BaseModule baseModuleToPointAt) {
        float angle = this.getAngleToBase(baseModuleToPointAt);
        if (this.armGroup != null) {
            this.armGroup.setRotation(angle);
            this.armGroup.setVisible(true);
        }
    }

    private float getAngleToBase(BaseModule base) {
        float currentAngle = this.armGroup.getRotation();
        float angle = new Vector2(base.worldX, base.worldY).sub(this.worldX, this.worldY).angle();
        System.out.println(angle);
        return angle;
    }

    private float getAngleDiffToBase(BaseModule base) {
        Vector2 targetBasePos = new Vector2(base.getXCenter(), base.getYCenter());
        Vector2 currentPos = new Vector2(this.getXCenter(), this.getYCenter());
        Vector2 currentDir = new Vector2(1.0f, 0.0f).setAngle(this.armGroup.getRotation()).nor();
        Vector2 targetDir = targetBasePos.sub(currentPos).nor();
        float rotationDiff = currentDir.angle() - targetDir.angle();
        if (Math.abs(rotationDiff) > 180.0f) {
            rotationDiff += rotationDiff > 0.0f ? -360.0f : 360.0f;
        }
        System.out.println("rotaton diff: " + rotationDiff);
        return rotationDiff;
    }

    public void animateArm(String itemId, BaseModule fromBase, BaseModule toBase) {
        this.armGroup.setVisible(true);
        if (fromBase == this || toBase == this) {
            if (fromBase == this) {
                this.setArmImage(itemId);
                this.setArmDirection(toBase);
                this.armGroup.addAction(Actions.sequence((Action)Actions.scaleTo(0.25f, 1.0f, 0.0f), (Action)Actions.delay(0.1f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.25f), (Action)Actions.delay(0.1f), (Action)Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.itemImage.setVisible(false);
                    }
                })));
            } else {
                this.setArmImage(itemId);
                this.setArmDirection(fromBase);
                this.itemImage.setVisible(false);
                this.armGroup.addAction(Actions.sequence(Actions.scaleTo(1.0f, 1.0f, 0.0f), Actions.delay(0.1f), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.itemImage.setVisible(true);
                    }
                }), Actions.scaleTo(0.25f, 1.0f, 0.25f), Actions.delay(0.1f), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.itemImage.setVisible(false);
                    }
                })));
            }
        } else {
            this.setArmImage(itemId);
            this.armGroup.setVisible(true);
            final float angleToBase = this.getAngleToBase(fromBase);
            if (Math.abs(angleToBase - this.armGroup.getRotation()) < 10.0f) {
                this.armGroup.addAction(Actions.sequence(Actions.rotateTo(angleToBase, 0.02f, Interpolation.sine), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.openClaw(0.0f);
                        CraftingBot.this.armAnimOut(0.0f);
                        CraftingBot.this.itemImageShowAnim(CraftingBot.this.animSpeed);
                        CraftingBot.this.closeClaw(CraftingBot.this.animSpeed);
                    }
                }), Actions.delay(this.animSpeed * 2.0f), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.armWirrSound(angleToBase, CraftingBot.this.armGroup.getRotation());
                    }
                }), Actions.rotateTo(this.getAngleToBase(toBase), this.animSpeed, Interpolation.sine), Actions.delay(this.animSpeed), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.openClaw(0.0f);
                        CraftingBot.this.itemImageHideAnim(0.0f);
                        CraftingBot.this.armAnimIn(CraftingBot.this.animSpeed);
                    }
                }), Actions.delay(this.animSpeed)));
            } else {
                this.armGroup.addAction(Actions.sequence(Actions.rotateTo(angleToBase, this.animSpeed, Interpolation.sine), Actions.delay(this.animSpeed), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.armWirrSound(angleToBase, CraftingBot.this.armGroup.getRotation());
                        CraftingBot.this.openClaw(0.0f);
                        CraftingBot.this.armAnimOut(0.0f);
                        CraftingBot.this.itemImageShowAnim(CraftingBot.this.animSpeed);
                        CraftingBot.this.closeClaw(CraftingBot.this.animSpeed);
                    }
                }), Actions.delay(this.animSpeed * 2.0f), Actions.rotateTo(this.getAngleToBase(toBase), this.animSpeed, Interpolation.sine), Actions.delay(this.animSpeed), Actions.run(new Runnable(){

                    @Override
                    public void run() {
                        CraftingBot.this.armWirrSound(angleToBase, CraftingBot.this.armGroup.getRotation());
                        CraftingBot.this.openClaw(0.0f);
                        CraftingBot.this.itemImageHideAnim(0.0f);
                        CraftingBot.this.armAnimIn(CraftingBot.this.animSpeed);
                    }
                }), Actions.delay(this.animSpeed)));
            }
        }
    }

    public void turnOffPanel() {
        if (this.panelImage != null) {
            this.panelImage.setVisible(false);
        }
        this.headGroup.clearActions();
        this.headGroup.addAction(Actions.sequence((Action)Actions.moveToAligned(0.0f, -15.0f, 4, 0.1f, Interpolation.sineIn)));
    }

    public void animatePanelLights(float delta) {
        if (!this.headGroup.hasActions()) {
            this.headGroup.addAction(Actions.forever(Actions.sequence((Action)Actions.moveToAligned(0.0f, -13.0f, 4, 0.3f, Interpolation.sine), (Action)Actions.moveToAligned(0.0f, -15.0f, 4, 0.3f, Interpolation.sine))));
        }
        this.panelTimer += delta;
        if (this.panelTimer > this.panelDelay) {
            this.panelTimer = 0.0f;
            ++this.panelIndex;
            if (this.panelIndex > this.panelImages) {
                this.panelIndex = 1;
            }
            if (this.panelImage != null) {
                this.panelImage.setVisible(true);
                this.panelImage.setDrawable(this.world.gameScreen.skin.getDrawable("autocrafter-panel" + this.panelIndex));
            }
        }
    }

    private void armWirrSound(float destAngle, float curAngle) {
        float dist = Math.abs(destAngle - curAngle);
        int i = 1;
        if (destAngle > 90.0f) {
            i = 2;
        }
        float volume = Audio.getInstance().playerDistanceMultiplier(this.world, this.getXCenter(), this.getYCenter(), 500.0f, 0.5f);
        float pan = Audio.getInstance().playerDistancePan(this.world, this.getXCenter(), this.getYCenter());
        Audio.getInstance().playSound("sounds/crafting-arm-" + i + ".ogg", volume, 1.1f, pan);
    }

    private void armAnimOut(float delay) {
        this.forearmGroup.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.moveTo(20.0f, 0.0f, this.animSpeed, Interpolation.sine)));
    }

    private void armAnimIn(float delay) {
        this.forearmGroup.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.moveTo(0.0f, 0.0f, this.animSpeed, Interpolation.sine)));
    }

    private void openClaw(float delay) {
        this.clawTop.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.rotateTo(30.0f, this.animSpeed, Interpolation.sine)));
        this.clawBot.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.rotateTo(-30.0f, this.animSpeed, Interpolation.sine)));
    }

    private void closeClaw(float delay) {
        this.clawTop.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.rotateTo(0.0f, this.animSpeed, Interpolation.sine)));
        this.clawBot.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.rotateTo(0.0f, this.animSpeed, Interpolation.sine)));
    }

    private void itemImageShowAnim(float delay) {
        this.itemImage.addAction(Actions.sequence(Actions.alpha(0.0f), Actions.delay(delay), Actions.moveToAligned(this.itemXPos, 0.0f, 1), Actions.scaleTo(0.5f, 0.5f, 0.0f), Actions.visible(true), Actions.parallel((Action)Actions.fadeIn(0.2f), (Action)Actions.scaleTo(1.0f, 1.0f, 0.2f, Interpolation.circleOut))));
    }

    private void itemImageHideAnim(float delay) {
        this.itemImage.addAction(Actions.sequence((Action)Actions.delay(delay), (Action)Actions.scaleTo(1.0f, 1.0f, 0.0f), (Action)Actions.alpha(1.0f), (Action)Actions.parallel((Action)Actions.fadeOut(0.3f), (Action)Actions.scaleTo(0.5f, 0.5f, 0.3f, Interpolation.circleOut), (Action)Actions.moveToAligned(this.itemXPos + 30.0f, 0.0f, 1, 0.3f, Interpolation.circleOut)), (Action)Actions.visible(false)));
    }

    public void resetArm() {
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        for (Behavior b : this.behaviorList) {
            b.update(delta);
        }
    }

    private void aimArrow(float delta) {
        if (this.world.player != null) {
            Vector2 playerPos = new Vector2(this.world.player.getXPos(), this.world.player.getYPos());
            Vector2 currentPos = new Vector2(this.getXCenter(), this.getYCenter());
            Vector2 currentDir = new Vector2(1.0f, 0.0f).setAngle(this.arrow.getRotation()).nor();
            Vector2 targetDir = playerPos.sub(currentPos).nor();
            float rotationDiff = currentDir.angle() - targetDir.angle();
            if (Math.abs(rotationDiff) > 180.0f) {
                rotationDiff += rotationDiff > 0.0f ? -360.0f : 360.0f;
            }
            if (rotationDiff < 0.0f) {
                this.arrow.rotateBy(30.0f * delta);
            }
            if (rotationDiff > 0.0f) {
                this.arrow.rotateBy(-30.0f * delta);
            }
        }
    }

    @Override
    public void interact(Player player) {
        if (!this.hasDisaster()) {
            this.interactMain(player);
        }
    }

    @Override
    public void interactMain(Player player) {
        CraftingBotUI botUI = new CraftingBotUI(this.world.gameScreen, this);
        this.world.gameScreen.showMenu(botUI);
        super.interactMain(player);
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on) {
            if (!this.hasPower) {
                this.animateColor(this.headImage, Color.WHITE, 0.25f);
                this.animateColor(this.armImage, Color.WHITE, 0.25f);
                this.animateColor(this.clawBot, Color.WHITE, 0.25f);
                this.animateColor(this.clawTop, Color.WHITE, 0.25f);
                this.animateColor(this.forearmImage, Color.WHITE, 0.25f);
                this.animateColor(this.panelImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.headImage, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.armImage, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.clawBot, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.clawTop, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.forearmImage, POWER_OFF_COLOR, 0.25f);
                this.animateColor(this.panelImage, Color.CLEAR, 0.25f);
            }
        }
        super.setPower(on);
    }

    @Override
    public void destroy() {
        this.headImage.remove();
        this.armGroup.remove();
        super.destroy();
    }
}

