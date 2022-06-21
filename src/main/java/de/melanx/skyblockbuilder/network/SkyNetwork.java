package de.melanx.skyblockbuilder.network;

import com.mojang.authlib.GameProfile;
import de.melanx.skyblockbuilder.SkyblockBuilder;
import de.melanx.skyblockbuilder.data.SkyblockSavedData;
import de.melanx.skyblockbuilder.util.RandomUtility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import org.moddingx.libx.network.NetworkX;

import java.util.List;
import java.util.Set;

public class SkyNetwork extends NetworkX {

    public SkyNetwork() {
        super(SkyblockBuilder.getInstance());
    }

    @Override
    protected Protocol getProtocol() {
        return Protocol.of("6");
    }

    @Override
    protected void registerPackets() {
        this.register(new SaveStructureHandler.Serializer(), () -> SaveStructureHandler::handle, NetworkDirection.PLAY_TO_SERVER);
        this.register(new DeleteTagsHandler.Serializer(), () -> DeleteTagsHandler::handle, NetworkDirection.PLAY_TO_SERVER);

        this.register(new SkyblockDataUpdateHandler.Serializer(), () -> SkyblockDataUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new ProfilesUpdateHandler.ProfilesUpdateSerializer(), () -> ProfilesUpdateHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
        this.register(new UpdateTemplateNamesHandler.Serializer(), () -> UpdateTemplateNamesHandler::handle, NetworkDirection.PLAY_TO_CLIENT);
    }

    public void updateData(Level level) {
        if (!level.isClientSide) {
            this.channel.send(PacketDistributor.ALL.noArg(), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(level)));
        }
    }

    public void updateData(Player player) {
        if (!player.getCommandSenderWorld().isClientSide) {
            this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new SkyblockDataUpdateHandler.Message(SkyblockSavedData.get(player.getCommandSenderWorld())));
        }
    }

    public void deleteTags(ItemStack stack) {
        this.channel.sendToServer(new DeleteTagsHandler.Message(stack));
    }

    public void saveStructure(ItemStack stack, String name, boolean ignoreAir) {
        this.channel.sendToServer(new SaveStructureHandler.Message(stack, name, ignoreAir));
    }

    public void updateProfiles(Player player) {
        if (player.getCommandSenderWorld().isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new ProfilesUpdateHandler.Message(this.getProfilesTag((ServerLevel) player.getCommandSenderWorld())));
    }

    public void updateProfiles(Level level) {
        if (level.isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.ALL.noArg(), new ProfilesUpdateHandler.Message(this.getProfilesTag((ServerLevel) level)));
    }

    public void updateTemplateNames(Player player, List<String> names) {
        if (player.level.isClientSide) {
            return;
        }

        this.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UpdateTemplateNamesHandler.Message(names));
    }

    public void updateTemplateNames(List<String> names) {
        this.channel.send(PacketDistributor.ALL.noArg(), new UpdateTemplateNamesHandler.Message(names));
    }

    private CompoundTag getProfilesTag(ServerLevel level) {
        Set<GameProfile> profileCache = RandomUtility.getGameProfiles(level);
        CompoundTag profiles = new CompoundTag();
        ListTag tags = new ListTag();

        // load the cache and look for all profiles
        profileCache.forEach(profile -> {
            if (profile.getId() != null && profile.getName() != null) {
                CompoundTag tag = new CompoundTag();
                tag.putUUID("Id", profile.getId());
                tag.putString("Name", profile.getName());
                tags.add(tag);
            }
        });

        profiles.put("Profiles", tags);
        return profiles;
    }
}
