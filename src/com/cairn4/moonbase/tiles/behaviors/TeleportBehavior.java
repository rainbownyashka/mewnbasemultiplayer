/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.tiles.behaviors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.GridPoint2;
import com.cairn4.moonbase.BaseGroup;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.Player;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.tiles.BaseModule;
import com.cairn4.moonbase.tiles.behaviors.Behavior;
import com.cairn4.moonbase.ui.Localization;
import com.cairn4.moonbase.worlddata.BehaviorData;
import java.util.ArrayList;

public class TeleportBehavior
implements Behavior {
    public ArrayList<BaseModule> targets = new ArrayList();
    public BaseModule baseModule;

    @Override
    public void setLoaded(boolean loaded) {
    }

    @Override
    public boolean isLoaded() {
        return false;
    }

    @Override
    public void setId(String s) {
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void start() {
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void interact(Player player) {
        this.findTarget();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public BehaviorData getData() {
        return null;
    }

    public BaseModule findTarget() {
        this.targets.clear();
        for (BaseGroup g : this.baseModule.world.baseManager.getBaseGroupList()) {
            for (BaseModule b : g.baseModuleList) {
                if (b.worldX == this.baseModule.worldX && b.worldY == this.baseModule.worldY || b.findBehavior(TeleportBehavior.class) == null) continue;
                this.targets.add(b);
            }
        }
        Gdx.app.debug("MewnBase", "TeleportBehavior: found " + this.targets.size() + " teleport targets");
        if (!this.targets.isEmpty()) {
            BaseModule first = this.targets.get(0);
            return first;
        }
        this.baseModule.world.gameScreen.hud.hudNotifications.newMessage(Localization.get("rocket_noLaunchPadsToLandAt"));
        return null;
    }

    public void teleport(int worldX, int worldY) {
        World cfr_ignored_0 = this.baseModule.world;
        GridPoint2 newChunk = World.convertWorldPosToChunkCoord(World.gridPointPool.obtain(), worldX, worldY);
        this.baseModule.world.player.moveToTile(worldX, worldY);
        this.baseModule.world.player.forcePositionUpdate();
        this.baseModule.world.chunkLoader.changeChunks(newChunk.x, newChunk.y);
        MoonBase.log("Teleporting player to " + worldX + ", " + worldY);
    }
}

