package iskallia.vault.network.message;

import iskallia.vault.skill.PlayerVaultStats;
import iskallia.vault.skill.talent.TalentGroup;
import iskallia.vault.skill.talent.TalentNode;
import iskallia.vault.skill.talent.TalentTree;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.world.data.PlayerTalentsData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

// From Client to Server
// "Hey dude, I want to upgrade dis ability o' mine. May I?"
public class AbilityUpgradeMessage {

    public String abilityName;

    public AbilityUpgradeMessage() { }

    public AbilityUpgradeMessage(String abilityName) {
        this.abilityName = abilityName;
    }

    public static void encode(AbilityUpgradeMessage message, PacketBuffer buffer) {
        buffer.writeString(message.abilityName);
    }

    public static AbilityUpgradeMessage decode(PacketBuffer buffer) {
        AbilityUpgradeMessage message = new AbilityUpgradeMessage();
        message.abilityName = buffer.readString();
        return message;
    }

    public static void handle(AbilityUpgradeMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayerEntity sender = context.getSender();

            if (sender == null) return;

            TalentGroup<?> talentGroup = ModConfigs.TALENTS.getByName(message.abilityName);

            PlayerVaultStatsData statsData = PlayerVaultStatsData.get((ServerWorld) sender.world);
            PlayerTalentsData abilitiesData = PlayerTalentsData.get((ServerWorld) sender.world);
            TalentTree talentTree = abilitiesData.getAbilities(sender);

            TalentNode<?> talentNode = talentTree.getNodeByName(message.abilityName);
            PlayerVaultStats stats = statsData.getVaultStats(sender);

            if (talentNode.getLevel() >= talentGroup.getMaxLevel())
                return; // Already maxed out

            int requiredSkillPts = talentGroup.cost(talentNode.getLevel() + 1);

            if (stats.getUnspentSkillPts() >= requiredSkillPts) {
                abilitiesData.upgradeAbility(sender, talentNode);
                statsData.spendSkillPts(sender, requiredSkillPts);
            }
        });
        context.setPacketHandled(true);
    }

}
