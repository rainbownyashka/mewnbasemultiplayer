
package com.cairn4.moonbase.tiles;

import com.cairn4.moonbase.Chunk;
import com.cairn4.moonbase.ItemFactory;
import com.cairn4.moonbase.ItemStack;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.World;
import com.cairn4.moonbase.worlddata.GroundTileData;
import com.cairn4.moonbase.worlddata.TileData;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class TileFactory {
    private static TileFactory instance;
  
    public static synchronized TileFactory getInstance() {
        if (instance == null)
            instance = new TileFactory(); 
        return instance;
    }
  
    public Tile createTile(Chunk chunk, TileData tileData) throws NoSuchFieldException, IllegalAccessException {
        Class[] classArgs;
        Object[] arguments;
        MoonBase.debug("TileFactory: trying to create tile " + tileData.name + " in chunk[" + chunk.chunkX + "," + chunk.chunkY + " - at [" + tileData.x + "," + tileData.y);
        Object object = null;
        if (tileData.name.equals("com.cairn4.moonbase.tiles.ItemPile")) {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class, ItemStack.class };
            ItemStack stack = new ItemStack(ItemFactory.getInstance().createItem(tileData.itemId));
            stack.setAmount(tileData.itemNum);
            arguments = new Object[] { chunk.world, chunk, new Integer(tileData.x), new Integer(tileData.y), stack };
        } else if (tileData.name.equals("com.cairn4.moonbase.tiles.ItemDropper")) {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class, String.class };
            arguments = new Object[] { chunk.world, chunk, new Integer(tileData.x), new Integer(tileData.y), new String(tileData.itemDropperId) };
        } else if (tileData.name.equals("com.cairn4.moonbase.tiles.ResearchObject")) {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class, String.class };
            arguments = new Object[] { chunk.world, chunk, new Integer(tileData.x), new Integer(tileData.y), new String(tileData.itemDropperId) };
        } else if (tileData.name.equals("com.cairn4.moonbase.tiles.Airlock") || tileData.name
            .equals("com.cairn4.moonbase.tiles.WallLight") || tileData.name
            .equals("com.cairn4.moonbase.tiles.AutoAirlock")) {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class, BaseModule.ORIENTATIONS.class };
            if (tileData.orientation == null)
                tileData.orientation = "west"; 
            BaseModule.ORIENTATIONS airlockOrientation = BaseModule.ORIENTATIONS.valueOf(tileData.orientation);
            arguments = new Object[] { chunk.world, chunk, new Integer(tileData.x), new Integer(tileData.y), airlockOrientation };
        } else {
            classArgs = new Class[] { World.class, Chunk.class, int.class, int.class };
            arguments = new Object[] { chunk.world, chunk, new Integer(tileData.x), new Integer(tileData.y) };
        } 
        try {
            Class<?> aClass = Class.forName(tileData.name);
            Constructor<?> constructor = null;
            constructor = aClass.getConstructor(classArgs);
            object = constructor.newInstance(arguments);
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (InvocationTargetException e) {
            e.getCause();
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
        if (object == null) {
            MoonBase.error("TileFactory: no such tile class " + tileData.name);
            return null;
        } 
        Tile tile = (Tile)object;
        tile.loadBehaviorData(tileData.behaviorDataList);
        if (tile instanceof BaseModule && tileData.disasterList != null)
            ((BaseModule)tile).loadDisasters(tileData.disasterList); 
        MoonBase.debug("New tile: " + tile.getClass().getSimpleName() + " - World:(" + tile.x + ", " + tile.y + ")");
        return tile;
    }
  
    public GroundTile createGroundTile(Chunk chunk, GroundTileData groundTileData) {
        Object object = null;
        Class[] classArgs = { World.class, Chunk.class, int.class, int.class };
        Object[] arguments = { chunk.world, chunk, new Integer(groundTileData.x), new Integer(groundTileData.y) };
        GroundTile gt1 = (GroundTile)chunk.world.groundTilePool.obtain();
        try {
            Class<GroundTile> aClass = GroundTile.class;
            Constructor<GroundTile> constructor = null;
            constructor = aClass.getConstructor(classArgs);
            gt1.init(chunk.world, chunk, groundTileData.x, groundTileData.y);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } 
        gt1.setBiome(groundTileData.biome);
        gt1.setDiscovered(groundTileData.disovered);
        gt1.createDrawables();
        return gt1;
    }
}
