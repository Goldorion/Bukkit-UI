package ca.goldorion.bukkitui;

import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BukkitUI extends JavaPlugin {

    public static final Logger LOG = LogManager.getLogger("Bukkit-UI");

    public BukkitUI(Plugin plugin) {
        super(plugin);

        addListener(PreGeneratorsLoadingEvent.class, _ -> BukkitModElements.registerModElements());

//        addListener(WorkspaceSelectorLoadedEvent.class, event -> {
//            event.getWorkspaceSelector().getRecentWorkspaces().getList().getFirst()
//            Workspace workspace = event.getMCreator().getWorkspace();
//
//            if (event.getMCreator().getGeneratorConfiguration().getGeneratorFlavor() == GeneratorFlavor.PAPER) {
//                Converters.commands2paperCommands(workspace);
//            }
//        });

        LOG.info("Bukkit-UI plugin was loaded");
    }
}