package ca.goldorion.bukkitui.elements.converters;

import ca.goldorion.bukkitui.BukkitModElements;
import ca.goldorion.bukkitui.elements.PaperCommand;
import ca.goldorion.bukkitui.utils.Utils;
import net.mcreator.element.ModElementType;
import net.mcreator.element.types.Command;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import java.util.ArrayList;

public class Converters {

    public static void commands2paperCommands(Workspace workspace) {
        workspace.getModElementsByType(ModElementType.COMMAND).forEach(me -> {
            Command command = (Command) me.getGeneratableElement();
            if (command != null) {
                PaperCommand paperCommand = new PaperCommand(new ModElement(workspace, me.getName(), BukkitModElements.PAPER_COMMAND));
                paperCommand.commandName = command.commandName;
                paperCommand.description = "";
                paperCommand.usage = "";
                paperCommand.aliases = new ArrayList<>();
                paperCommand.permission = workspace.getWorkspaceSettings().getModID() + "." + command.commandName + ".use";
                paperCommand.permissionMessage = "";
                paperCommand.argsxml = command.argsxml;

                Utils.replaceMEByNewME(me, paperCommand);
            }

        });
    }
}
