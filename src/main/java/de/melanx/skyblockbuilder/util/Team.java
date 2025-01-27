package de.melanx.skyblockbuilder.util;

import de.melanx.skyblockbuilder.world.IslandPos;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class Team {
    
    private final SkyblockSavedData data;
    private final Set<UUID> players;
    private final Set<BlockPos> possibleSpawns;
    private final Random random = new Random();
    private final Set<UUID> teamChatUsers = new HashSet<>();
    private IslandPos island;
    private String name;
    private boolean allowVisits;

    public Team(SkyblockSavedData data, IslandPos island) {
        this.data = data;
        this.island = island;
        this.players = new HashSet<>();
        this.possibleSpawns = new HashSet<>();
        this.allowVisits = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        this.data.markDirty();
    }

    public IslandPos getIsland() {
        return this.island;
    }

    public void setIsland(IslandPos island) {
        this.island = island;
        this.data.markDirty();
    }

    public Set<UUID> getPlayers() {
        return this.players;
    }

    public void setPlayers(Collection<UUID> players) {
        this.players.clear();
        this.players.addAll(players);
        this.data.markDirty();
    }

    public Set<BlockPos> getPossibleSpawns() {
        return this.possibleSpawns;
    }

    public void setPossibleSpawns(Collection<BlockPos> spawns) {
        this.possibleSpawns.clear();
        this.possibleSpawns.addAll(spawns);
        this.data.markDirty();
    }

    public void addPossibleSpawn(BlockPos pos) {
        this.possibleSpawns.add(pos);
        this.data.markDirty();
    }

    public boolean removePossibleSpawn(BlockPos pos) {
        if (this.possibleSpawns.size() <= 1) {
            return false;
        }

        boolean remove = this.possibleSpawns.remove(pos);
        this.data.markDirty();
        return remove;
    }

    public boolean allowsVisits() {
        return this.allowVisits;
    }

    public boolean toggleAllowVisits() {
        this.allowVisits = !this.allowVisits;
        this.data.markDirty();
        return this.allowVisits;
    }

    public void setAllowVisit(boolean enabled) {
        this.allowVisits = enabled;
        this.data.markDirty();
    }

    public boolean addPlayer(UUID player) {
        boolean added = this.players.add(player);
        this.data.markDirty();
        return added;
    }

    public boolean addPlayer(PlayerEntity player) {
        return this.addPlayer(player.getGameProfile().getId());
    }

    public boolean addPlayers(Collection<UUID> players) {
        boolean added = this.players.addAll(players);
        this.data.markDirty();
        return added;
    }

    public boolean removePlayer(PlayerEntity player) {
        return this.removePlayer(player.getGameProfile().getId());
    }

    public boolean removePlayer(UUID player) {
        boolean removed = this.players.remove(player);
        this.data.markDirty();
        return removed;
    }

    public void removePlayers(Collection<UUID> players) {
        for (UUID id : players) {
            this.players.remove(id);
        }
        this.data.markDirty();
    }

    public void removeAllPlayers() {
        this.players.clear();
        this.data.markDirty();
    }

    public boolean hasPlayer(UUID player) {
        return this.players.contains(player);
    }

    public boolean hasPlayer(PlayerEntity player) {
        return this.hasPlayer(player.getGameProfile().getId());
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    @Nonnull
    public ServerWorld getWorld() {
        return this.data.getWorld();
    }

    public void broadcast(ITextComponent msg) {
        PlayerList playerList = this.getWorld().getServer().getPlayerList();
        this.players.forEach(uuid -> {
            ServerPlayerEntity player = playerList.getPlayerByUUID(uuid);
            if (player != null) {
                IFormattableTextComponent component = new StringTextComponent("[" + this.name + "] ");
                player.sendMessage(component.append(msg), uuid);
            }
        });
    }

    public void setTeamChat(PlayerEntity player, boolean teamChat) {
        this.setTeamChat(player.getGameProfile().getId(), teamChat);
    }

    public void setTeamChat(UUID player, boolean teamChat) {
        if (this.teamChatUsers.contains(player)) {
            this.teamChatUsers.remove(player);
        } else {
            this.teamChatUsers.add(player);
        }
        this.data.markDirty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isInTeamChat(PlayerEntity player) {
        return this.isInTeamChat(player.getGameProfile().getId());
    }

    public boolean isInTeamChat(UUID player) {
        return this.teamChatUsers.contains(player);
    }

    @Nonnull
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.put("Island", this.island.toTag());
        nbt.putString("Name", this.name != null ? this.name : "");
        nbt.putBoolean("Visits", this.allowVisits);

        ListNBT players = new ListNBT();
        for (UUID player : this.players) {
            CompoundNBT playerTag = new CompoundNBT();
            playerTag.putUniqueId("Player", player);

            players.add(playerTag);
        }

        ListNBT spawns = new ListNBT();
        for (BlockPos pos : this.possibleSpawns) {
            CompoundNBT posTag = new CompoundNBT();
            posTag.putDouble("posX", pos.getX() + 0.5);
            posTag.putDouble("posY", pos.getY());
            posTag.putDouble("posZ", pos.getZ() + 0.5);

            spawns.add(posTag);
        }

        ListNBT teamChat = new ListNBT();
        for (UUID id : this.teamChatUsers) {
            CompoundNBT player = new CompoundNBT();
            player.putUniqueId("Player", id);

            teamChat.add(player);
        }

        nbt.put("Players", players);
        nbt.put("Spawns", spawns);
        nbt.put("TeamChat", teamChat);
        return nbt;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        this.island = IslandPos.fromTag(nbt.getCompound("Island"));
        this.name = nbt.getString("Name");
        this.allowVisits = nbt.getBoolean("Visits");

        ListNBT players = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        this.players.clear();
        for (INBT player : players) {
            this.players.add(((CompoundNBT) player).getUniqueId("Player"));
        }

        ListNBT spawns = nbt.getList("Spawns", Constants.NBT.TAG_COMPOUND);
        this.possibleSpawns.clear();
        for (INBT pos : spawns) {
            CompoundNBT posTag = (CompoundNBT) pos;
            this.possibleSpawns.add(new BlockPos(posTag.getDouble("posX"), posTag.getDouble("posY"), posTag.getDouble("posZ")));
        }

        this.teamChatUsers.clear();
        if (nbt.contains("TeamChat")) { // TODO 1.17 remove backwards compatibility
            ListNBT teamChat = nbt.getList("TeamChat", Constants.NBT.TAG_COMPOUND);
            for (INBT player : teamChat) {
                this.teamChatUsers.add(((CompoundNBT) player).getUniqueId("Player"));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Team)) {
            return false;
        }

        Team team = (Team) o;
        return this.name.equals(team.name) && this.island.equals(team.island);
    }

    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = result * this.island.hashCode();
        return result;
    }
}
