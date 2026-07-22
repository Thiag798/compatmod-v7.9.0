package com.compatmod.compat;

import java.util.*;

public final class RegistryMappingTable {

    private RegistryMappingTable() { throw new UnsupportedOperationException("Utility class"); }

    private static final Map<String, String> MAPPINGS = new LinkedHashMap<>();

    static {
        put("minecraft:grass", "minecraft:grass_block");
        put("minecraft:stone_slab", "minecraft:smooth_stone_slab");
        put("minecraft:wooden_slab", "minecraft:oak_slab");
        put("minecraft:melon_block", "minecraft:melon");
        put("minecraft:pumpkin_block", "minecraft:pumpkin");
        put("minecraft:noteblock", "minecraft:note_block");
        put("minecraft:lit_furnace", "minecraft:furnace");
        put("minecraft:redstone_wire", "minecraft:redstone_wire");
        put("minecraft:apple", "minecraft:apple");
        put("minecraft:golden_apple", "minecraft:golden_apple");
        put("minecraft:wooden_sword", "minecraft:wooden_sword");
        put("minecraft:stone_pickaxe", "minecraft:stone_pickaxe");
        put("minecraft:entity_horse", "minecraft:horse");
        put("minecraft:snowman", "minecraft:snow_golem");
        put("minecraft:random.click", "minecraft:ui.button.click");
        put("minecraft:random.orb", "minecraft:entity.experience_orb.pickup");
        put("minecraft:random.anvil_land", "minecraft:block.anvil.land");
    }

    private static void put(String legacy, String modern) { MAPPINGS.put(legacy, modern); }

    public static Optional<String> lookup(String legacyKey) { return Optional.ofNullable(MAPPINGS.get(legacyKey)); }
    public static Optional<String> lookup(String namespace, String path) { return lookup(namespace + ":" + path); }
    public static int size() { return MAPPINGS.size(); }
    public static Map<String, String> getAll() { return Collections.unmodifiableMap(MAPPINGS); }
}
