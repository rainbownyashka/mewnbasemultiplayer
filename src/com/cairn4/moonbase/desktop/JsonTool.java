/*
 * Decompiled with CFR 0.152.
 */
package com.cairn4.moonbase.desktop;

import com.badlogic.gdx.utils.Json;
import com.cairn4.moonbase.ItemActions;
import com.cairn4.moonbase.ItemData;
import com.cairn4.moonbase.ItemDataSet;
import com.cairn4.moonbase.MoonBase;
import com.cairn4.moonbase.RecipieRequirement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

public class JsonTool {
    private static ArrayList<ItemData> itemDataList;

    public static void main(String[] arg) {
        String filePath = "/home/steve/Workspace/MoonBase/gameData/data/items.json";
        String dataString = JsonTool.readLineByLineJava8(filePath);
        Json json = new Json();
        ItemDataSet itemDataSet = json.fromJson(ItemDataSet.class, dataString);
        itemDataList = itemDataSet.itemDataList;
        JsonTool.foodCheck();
        JsonTool.maxCarryCheck();
    }

    private static void maxCarryCheck() {
        for (ItemData iData : itemDataList) {
            if (iData.maxCarry != 99 || iData.canStackMultiples()) continue;
            MoonBase.log("MAX CARRY DEFAULT: " + iData.id);
        }
    }

    private static void foodCheck() {
        for (ItemData iData : itemDataList) {
            if (iData.actions.isEmpty()) continue;
            for (ItemActions action : iData.actions) {
                if (!action.type.equals("eat")) continue;
                System.out.println("Food: " + iData.id + ": " + action.value);
            }
        }
    }

    private static void rawResourceCheck() {
        for (ItemData itemData : itemDataList) {
            if (!itemData.id.equals("sciencelab-builder")) continue;
            ArrayList<RecipieRequirement> allRaw = new ArrayList<RecipieRequirement>();
            for (RecipieRequirement r : itemData.requires) {
                ArrayList<RecipieRequirement> ingredient = JsonTool.getRequirements(r);
                allRaw.addAll(ingredient);
            }
            for (RecipieRequirement r : allRaw) {
                System.out.println(" -- " + r.id + " x" + r.quantity);
            }
            System.out.println("Condensed:");
            ArrayList<RecipieRequirement> finalList = JsonTool.condenseIngredients(allRaw);
            if (finalList.isEmpty()) continue;
            System.out.println("Item " + itemData.id);
            for (RecipieRequirement r : finalList) {
                System.out.println(" -- " + r.id + " x" + r.quantity);
            }
            System.out.println("===================================\n");
        }
    }

    private static ArrayList<RecipieRequirement> condenseIngredients(ArrayList<RecipieRequirement> allRaw) {
        ArrayList<RecipieRequirement> finalist = new ArrayList<RecipieRequirement>();
        for (RecipieRequirement r : allRaw) {
            boolean exists = false;
            for (RecipieRequirement f : finalist) {
                if (!f.id.equals(r.id)) continue;
                exists = true;
                f.quantity += r.quantity;
                break;
            }
            if (exists) continue;
            RecipieRequirement newReq = new RecipieRequirement();
            newReq.id = r.id;
            newReq.quantity = r.quantity;
            finalist.add(newReq);
        }
        return finalist;
    }

    private static ArrayList<RecipieRequirement> getRequirements(RecipieRequirement req) {
        ArrayList<RecipieRequirement> allReqs = new ArrayList<RecipieRequirement>();
        ItemData itemData = JsonTool.getItemData(req.id);
        if (itemData != null) {
            if (itemData.requires.isEmpty()) {
                allReqs.add(req);
                return allReqs;
            }
            for (RecipieRequirement r : itemData.requires) {
                allReqs.addAll(JsonTool.getRequirements(r));
            }
        }
        return allReqs;
    }

    private static ItemData getItemData(String id) {
        for (ItemData itemData : itemDataList) {
            if (!itemData.id.equals(id)) continue;
            return itemData;
        }
        return null;
    }

    private static String readLineByLineJava8(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath, new String[0]), StandardCharsets.UTF_8);){
            stream.forEach(s -> contentBuilder.append((String)s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}

