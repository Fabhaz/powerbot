package Fabhaz;


import org.powerbot.script.Tile;

import java.util.TreeMap;
// file to hold all information
class Resources {

    final Tile EDGEVILLE_BANK_TILE = new Tile(3096, 3494, 0);
    final Tile EDGEVILLE_FURNACE_TILE = new Tile(3109, 3499, 0);
    final Tile AL_KHARID_BANK_TILE = new Tile(3269, 3167, 0);
    final Tile AL_KHARID_FURNACE_TILE = new Tile(3274, 3186, 0);
    final int EDGEVILLE_FURNACE = 16469;
    final int AL_KHARID_FURNACE = 24009;
    final int GOLD = 2357;
    final int WIDGET = 446;

    String[] gems = {
            "None",
            "Sapphire",
            "Emerald",
            "Ruby",
            "Diamond",
            "Dragonstone",
            "Onyx",
            "Enchanted",
            "Zenyte"
    };

    String[] moulds = {
            "Ring",
            "Necklace",
            "Amulet",
            "Bracelet"
    };

    String[] furnaces = {
            "Edgeville",
            "Al Kharid"
    };

    //map which takes the mould and gemID chosen and return the correct component
    final TreeMap<Integer, Integer> ringComponentMap = new TreeMap<Integer, Integer>() {
        {
            put(0, 7);
            put(1607, 8);
            put(1605, 9);
            put(1603, 10);
            put(1601, 11);
            put(1615, 12);
            put(6573, 13);
            put(19493, 14);
            put(27052, 15);
        }
    };

    final TreeMap<Integer, Integer> necklaceComponentMap = new TreeMap<Integer, Integer>() {
        {
            put(0, 21);
            put(1607, 22);
            put(1605, 23);
            put(1603, 24);
            put(1601, 25);
            put(1615, 26);
            put(6573, 27);
            put(19493, 28);
        }
    };

    final TreeMap<Integer, Integer> amuletComponentMap = new TreeMap<Integer, Integer>() {
        {
            put(0, 34);
            put(1607, 35);
            put(1605, 36);
            put(1603, 37);
            put(1601, 38);
            put(1615, 39);
            put(6573, 40);
            put(19493, 41);
        }
    };

    final TreeMap<Integer, Integer> braceletComponentMap = new TreeMap<Integer, Integer>() {
        {
            put(0, 47);
            put(1607, 48);
            put(1605, 49);
            put(1603, 50);
            put(1601, 51);
            put(1615, 52);
            put(6573, 53);
            put(19493, 54);
        }
    };

    final TreeMap<String, Integer> gemMap = new TreeMap<String, Integer>() {
        {
            put("None", 0);
            put("Sapphire", 1607);
            put("Emerald", 1605);
            put("Ruby", 1603);
            put("Diamond", 1601);
            put("Dragonstone", 1615);
            put("Onyx", 6573);
            put("Zenyte", 19493);
            put("Enchanted", 27052);
        }
    };

}
