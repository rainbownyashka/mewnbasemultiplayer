/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.entities.Npc;
import com.cairn4.moonbase.entities.NpcManager;
import com.cairn4.moonbase.tiles.HabitatModule;
import com.cairn4.moonbase.ui.NpcAssignmentPopup;

public class NpcHome
extends HabitatModule {
    private Label debugLabel;
    Image componentImage;
    public Npc npcAssigned;
    private Label ownerName;

    public NpcHome(World world, Chunk chunk, int x, int y) {
        super(world, chunk, x, y);
        this.componentImage = new Image(world.gameScreen.skin.getDrawable("npchome"));
        this.componentImage.setSize(TILE_SIZE * 0.8f, TILE_SIZE * 0.8f);
        this.componentImage.setPosition(TILE_SIZE / 2.0f, TILE_SIZE / 2.0f, 1);
        this.componentGroup.addActor(this.componentImage);
        this.debugLabel = new Label((CharSequence)"", world.gameScreen.labelStyle);
        this.debugLabel.setFontScale(0.4f);
        this.debugLabel.setPosition(0.0f, 100.0f, 1);
        this.debugLabel.setAlignment(1);
        this.group.addActor(this.debugLabel);
        this.debugLabel.setTouchable(Touchable.disabled);
        this.debugLabel.setVisible(false);
    }

    @Override
    protected void setBuilderId() {
        this.builderId = "npchome";
    }

    public void assignNpc(Npc npc) {
        this.npcAssigned = npc;
        if (npc != null) {
            this.debugLabel.setText(npc.npcId);
        } else {
            this.debugLabel.setText("");
        }
    }

    @Override
    public void setPower(boolean on) {
        if (this.hasPower != on && this.componentImage != null) {
            if (!this.hasPower) {
                this.animateColor(this.componentImage, Color.WHITE, 0.25f);
            } else {
                this.animateColor(this.componentImage, POWER_OFF_COLOR, 0.25f);
            }
        }
        super.setPower(on);
    }

    @Override
    public void interact(Player player) {
        if (this.baseDisaster != null) {
            this.baseDisaster.fix();
        } else {
            NpcAssignmentPopup popup = new NpcAssignmentPopup(this.world.gameScreen, this);
            this.world.gameScreen.showMenu(popup);
            super.interact(player);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        NpcManager.getInstance().homeRemoved(this);
    }
}

