package iskallia.vault.world.data;

import iskallia.vault.Vault;
import iskallia.vault.init.ModFeatures;
import iskallia.vault.init.ModStructures;
import iskallia.vault.world.raid.ArenaRaid;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.BiomeRegistry;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArenaRaidData extends WorldSavedData {

    protected static final String DATA_NAME = Vault.MOD_ID + "_ArenaRaid";

    private Map<UUID, ArenaRaid> activeRaids = new HashMap<>();
    private int xOffset = 0;

    public ArenaRaidData() {
        this(DATA_NAME);
    }

    public ArenaRaidData(String name) {
        super(name);
    }

    public ArenaRaid getAt(BlockPos pos) {
        return this.activeRaids.values().stream().filter(raid -> raid.box.isVecInside(pos)).findFirst().orElse(null);
    }

    public ArenaRaid getActiveFor(ServerPlayerEntity player) {
        return this.activeRaids.get(player.getUniqueID());
    }

    public ArenaRaid startNew(ServerPlayerEntity player) {
        player.sendStatusMessage(new StringTextComponent("Generating arena, please wait...").mergeStyle(TextFormatting.GREEN), true);

        ArenaRaid raid = new ArenaRaid(player.getUniqueID(), new MutableBoundingBox(
                this.xOffset, 0, 0, this.xOffset += ArenaRaid.REGION_SIZE, 256, ArenaRaid.REGION_SIZE
        ));

        if(this.activeRaids.containsKey(player.getUniqueID())) {
            //TODO: The hell do we do if an arena raid is already ongoing?
        }

        this.activeRaids.put(raid.getPlayerId(), raid);
        this.markDirty();

        player.getServer().runAsync(() -> {
            try {
                ServerWorld world = player.getServer().getWorld(Vault.ARENA_KEY);
                ChunkPos chunkPos = new ChunkPos((raid.box.minX + raid.box.getXSize() / 2) >> 4, (raid.box.minZ + raid.box.getZSize() / 2) >> 4);

                StructureSeparationSettings settings = new StructureSeparationSettings(1, 0, -1);

                StructureStart<?> start = ModFeatures.ARENA_FEATURE.func_242771_a(world.func_241828_r(),
                        world.getChunkProvider().generator, world.getChunkProvider().generator.getBiomeProvider(),
                        world.getStructureTemplateManager(), world.getSeed(), chunkPos,
                        BiomeRegistry.PLAINS, 0, settings);

                //This is some cursed calculations, don't ask me how it works.
                int chunkRadius = ArenaRaid.REGION_SIZE >> 5;

                for(int x = -chunkRadius; x <= chunkRadius; x += 17) {
                    for(int z = -chunkRadius; z <= chunkRadius; z += 17) {
                        world.getChunk(chunkPos.x + x, chunkPos.z + z, ChunkStatus.EMPTY, true).func_230344_a_(ModStructures.ARENA, start);
                    }
                }

                raid.start(world, player, chunkPos);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });

        return raid;
    }

    public void tick(ServerWorld world) {
        this.activeRaids.values().forEach(vaultRaid -> vaultRaid.tick(world));

        boolean removed = false;

        for(ArenaRaid raid : this.activeRaids.values()) {
            if(raid.isComplete()) {
                removed |= this.activeRaids.values().remove(raid);
            }
        }

        if(removed || this.activeRaids.size() > 0) {
            this.markDirty();
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.WorldTickEvent event) {
        if(event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.START && event.world.getDimensionKey() == Vault.ARENA_KEY) {
            get((ServerWorld)event.world).tick((ServerWorld)event.world);
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        this.activeRaids.clear();

        nbt.getList("ActiveRaids", Constants.NBT.TAG_COMPOUND).forEach(raidNBT -> {
            ArenaRaid raid = ArenaRaid.fromNBT((CompoundNBT)raidNBT);
            this.activeRaids.put(raid.getPlayerId(), raid);
        });

        this.xOffset = nbt.getInt("XOffset");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        ListNBT raidsList = new ListNBT();
        this.activeRaids.values().forEach(raid -> raidsList.add(raid.serializeNBT()));
        nbt.put("ActiveRaids", raidsList);

        nbt.putInt("XOffset", this.xOffset);
        return nbt;
    }

    public static ArenaRaidData get(ServerWorld world) {
        return world.getServer().func_241755_D_().getSavedData().getOrCreate(ArenaRaidData::new, DATA_NAME);
    }

}