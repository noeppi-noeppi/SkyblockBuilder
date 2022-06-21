package de.melanx.skyblockbuilder.config;

import de.melanx.skyblockbuilder.template.TemplateInfo;
import de.melanx.skyblockbuilder.util.WorldUtil;
import net.minecraft.core.BlockPos;
import org.moddingx.libx.annotation.config.RegisterConfig;
import org.moddingx.libx.config.Config;

import java.util.List;
import java.util.Map;

@RegisterConfig("templates")
public class TemplateConfig {

    @Config("The list of templates being available. The first entry is the default template.")
    public static List<TemplateInfo> templates = List.of(new TemplateInfo("default", "default.nbt", "default", WorldUtil.Directions.SOUTH));

    @Config
    public static Map<String, List<BlockPos>> spawns = Map.of("default", List.of(
            new BlockPos(6, 3, 5)
    ));
}
