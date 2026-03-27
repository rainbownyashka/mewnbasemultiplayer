/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.worlddata;

import com.cairn4.moonbase.worlddata.WheelData;
import java.util.ArrayList;

public class VehicleData {
    public String id;
    public boolean drivable;
    public boolean trailerHitch;
    public String physicsBody;
    public float maxHealth;
    public float armorMul;
    public float damagedThreshold;
    public float veryDamagedThreshold;
    public float drift;
    public float maxSteeringAngle;
    public float steeringRate;
    public float headlightDistance;
    public float headlightAngle;
    public float maxSpeed;
    public float wheelPower;
    public ArrayList<WheelData> wheelDataArrayList = new ArrayList();
    public String engineSoundFile;
    public boolean driverHead;
    public float driverHeadX;
    public float driverHeadY;
    public int trunkStorageSize;
    public float physicsFriction;
    public float physicsDensity;
    public float physicsAngularDampening;
    public float maxBatteryCharge;
    public float rechargeRate;
}

