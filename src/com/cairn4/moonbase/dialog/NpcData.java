/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.dialog;

import com.badlogic.gdx.math.GridPoint2;

public class NpcData {
    public String id;
    public float idleMinTime;
    public float idleMaxTime;
    public float patrolAccel;
    public float maxPatrolSpeed;
    public float chaseAccel;
    public float maxChaseSpeed;
    public int maxPatrolTileDistanceFromSpawn;
    public int patrolPathNodeLimit;
    public float targetSearchTileDist;
    public GridPoint2 npcHomeLocation;
    public boolean bonusActive;
}

