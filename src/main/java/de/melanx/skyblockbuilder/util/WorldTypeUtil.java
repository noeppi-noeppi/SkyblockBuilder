package de.melanx.skyblockbuilder.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.melanx.skyblockbuilder.ConfigHandler;
import de.melanx.skyblockbuilder.world.VoidWorldType;
import de.melanx.skyblockbuilder.world.dimensions.overworld.SkyblockOverworldChunkGenerator;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.storage.ServerWorldInfo;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

public class WorldTypeUtil {

    public static DimensionStructuresSettings STRONGHOLD_ONLY_STRUCTURE_SETTINGS = new DimensionStructuresSettings(Optional.of(DimensionStructuresSettings.field_236192_c_), Maps.newHashMap(ImmutableMap.of(Structure.STRONGHOLD, DimensionStructuresSettings.field_236191_b_.get(Structure.STRONGHOLD))));
    public static DimensionStructuresSettings EMPTY_SETTINGS = new DimensionStructuresSettings(Optional.of(DimensionStructuresSettings.field_236192_c_), Maps.newHashMap());

    private static boolean isServerLevelSkyblock(DedicatedServer server) {
        String levelType = Optional.ofNullable((String) server.getServerProperties().serverProperties.get("level-type")).map(str -> str.toLowerCase(Locale.ROOT)).orElse("default");
        return levelType.equals("custom_skyblock");
    }

    public static void setupForDedicatedServer(DedicatedServer server) {
        if (!isServerLevelSkyblock(server)) {
            return;
        }

        DynamicRegistries registries = server.func_244267_aX();
        ServerWorldInfo worldInfo = (ServerWorldInfo) server.getServerConfiguration().getServerWorldInfo();
        long seed = worldInfo.generatorSettings.getSeed();
        Registry<DimensionType> dimensions = registries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionSettings> dimensionSettings = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);

        SkyblockOverworldChunkGenerator generator = (SkyblockOverworldChunkGenerator) VoidWorldType.overworldChunkGenerator(biomes, dimensionSettings, seed);
        SimpleRegistry<Dimension> skyblock = DimensionGeneratorSettings.func_242749_a(dimensions, VoidWorldType.voidDimensions(registries, biomes, dimensionSettings, seed), generator);
        worldInfo.generatorSettings = new DimensionGeneratorSettings(seed, worldInfo.generatorSettings.doesGenerateFeatures(), worldInfo.generatorSettings.hasBonusChest(), skyblock);
    }

    public static Supplier<DimensionSettings> changeDimensionStructureSettings(DimensionStructuresSettings structuresSettings, DimensionSettings oldSettings) {
        return () -> new DimensionSettings(structuresSettings, oldSettings.getNoise(), oldSettings.getDefaultBlock(), oldSettings.getDefaultFluid(), oldSettings.func_236117_e_(), oldSettings.func_236118_f_(), oldSettings.func_236119_g_(), oldSettings.func_236120_h_());
    }

    public static Supplier<DimensionSettings> getOverworldSettings(Supplier<DimensionSettings> settings) {
        if (!ConfigHandler.overworldStructures.get()) {
            settings = WorldTypeUtil.changeDimensionStructureSettings(WorldTypeUtil.EMPTY_SETTINGS, settings.get());
        } else if (ConfigHandler.strongholdOnly.get()) {
            settings = WorldTypeUtil.changeDimensionStructureSettings(WorldTypeUtil.STRONGHOLD_ONLY_STRUCTURE_SETTINGS, settings.get());
        }

        return settings;
    }
}
