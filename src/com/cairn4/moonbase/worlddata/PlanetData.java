/*
 * Planet metadata stored in gameSave.json for multi-planet saves.
 */
package com.cairn4.moonbase.worlddata;

public class PlanetData {
    public int planetId;
    public String planetName;
    public String planetType;
    public int terrainGenSeed;
    public long lastVisited;
    public PlayerData playerData;

    public PlanetData() {
    }
}
