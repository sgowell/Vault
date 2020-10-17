package iskallia.vault.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.world.data.PlayerAbilitiesData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class VaultLevelCommand extends Command {

    @Override
    public String getName() {
        return "vault_level";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
                Commands.literal("add_exp")
                        .then(Commands.argument("exp", IntegerArgumentType.integer())
                                .executes(this::addExp))
        );

        builder.then(
                Commands.literal("set_level")
                        .then(Commands.argument("level", IntegerArgumentType.integer())
                                .executes(this::setLevel))
        );
    }

    private int setLevel(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int level = IntegerArgumentType.getInteger(context, "level");
        CommandSource source = context.getSource();
        PlayerAbilitiesData.get(source.getWorld()).setLevel(source.asPlayer(), level);
        return 0;
    }

    private int addExp(CommandContext<CommandSource> context) throws CommandSyntaxException {
        int exp = IntegerArgumentType.getInteger(context, "exp");
        CommandSource source = context.getSource();
        PlayerAbilitiesData.get(source.getWorld()).addExp(source.asPlayer(), exp);
        return 0;
    }

}
