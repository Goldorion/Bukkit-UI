package ca.goldorion.bukkitui.elements.converters;

import ca.goldorion.bukkitui.BukkitModElements;
import ca.goldorion.bukkitui.elements.PaperCommand;
import ca.goldorion.bukkitui.utils.BukkitModElement;
import ca.goldorion.bukkitui.utils.BukkitUtils;
import ca.goldorion.bukkitui.utils.BukkitWorkspace;
import com.formdev.flatlaf.json.Json;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mcreator.element.ModElementType;
import net.mcreator.element.types.Command;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;
import org.apache.logging.log4j.util.TriConsumer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static ca.goldorion.bukkitui.BukkitUI.LOG;

public class Converters {

    private static final int CURRENT_VERSION = 1;

    private static final Map<Integer,BiConsumer<BukkitWorkspace, BukkitModElement>> CONVERTERS = new HashMap<>() {{
        put(1, ( workspace, me) -> {
            if (me.getType() == ModElementType.COMMAND) {
                if (BukkitUtils.isPaperWorkspace(workspace)) {
                    command2paperCommand(workspace, me);
                } else if (BukkitUtils.isSpigotWorkspace(workspace)) {

                }
            }
        });
    }};

    public static int checkAndApplyConverters(BukkitWorkspace workspace, BukkitModElement me, int meVersion) {
        int newVersion = meVersion;
        while (newVersion != CURRENT_VERSION) {
            newVersion++;

            LOG.debug("Converting {} to {}", me.getName(), newVersion);

            CONVERTERS.get(newVersion).accept(workspace, me);
        }

        return newVersion;
    }

    private static void command2paperCommand(BukkitWorkspace workspace, BukkitModElement meToConvert) {
        JsonObject command = BukkitUtils.readMEAsJsonObject(workspace.workspaceFile, meToConvert);

        JsonObject definition = new JsonObject();
        definition.addProperty("commandName", command.get("commandName").getAsString());
        definition.addProperty("description", "");
        definition.addProperty("usage", "");
        definition.addProperty("permission", workspace.workspaceSettings.getModID() + "." + command.get("commandName").getAsString() + ".use");
        definition.addProperty("permissionMessage", "");
        definition.addProperty("argsxml", command.get("argsxml").getAsString());

        JsonObject element = new JsonObject();
        element.addProperty("_fv", 85);
        element.addProperty("_type", "papercommand");
        element.add("definition", definition);

        meToConvert.setType(BukkitModElements.PAPER_COMMAND.getRegistryName());
        BukkitUtils.saveModElement(workspace, element, meToConvert.getName());
    }
}
