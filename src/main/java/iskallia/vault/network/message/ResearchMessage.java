package iskallia.vault.network.message;

import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.research.ResearchTree;
import iskallia.vault.research.type.Research;
import iskallia.vault.world.data.PlayerResearchesData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// From Client to Server
// "Hey dude, I want to research dis. May I?"
public class ResearchMessage {

    public String researchName;

    public ResearchMessage() { }

    public ResearchMessage(String researchName) {
        this.researchName = researchName;
    }

    public static void encode(ResearchMessage message, PacketBuffer buffer) {
        buffer.writeString(message.researchName);
    }

    public static ResearchMessage decode(PacketBuffer buffer) {
        ResearchMessage message = new ResearchMessage();
        message.researchName = buffer.readString();
        return message;
    }

    public static void handle(ResearchMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();

            if (sender == null) return;

            Research research = ModConfigs.RESEARCHES.getByName(message.researchName);

            if (research == null) return;

            PlayerVaultStatsData statsData = PlayerVaultStatsData.get((ServerWorld) sender.world);
            PlayerResearchesData researchesData = PlayerResearchesData.get((ServerWorld) sender.world);

            PlayerVaultStats stats = statsData.getVaultStats(sender);

            if (stats.getUnspentSkillPts() >= research.getCost()) {
                researchesData.research(sender, research);
                statsData.spendSkillPts(sender, research.getCost());
            }
        });
        context.setPacketHandled(true);
    }

}