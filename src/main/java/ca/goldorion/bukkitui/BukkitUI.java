package ca.goldorion.bukkitui;

import ca.goldorion.bukkitui.ui.dialogs.BukkitUIConverterDialog;
import net.mcreator.plugin.JavaPlugin;
import net.mcreator.plugin.Plugin;
import net.mcreator.plugin.events.PreGeneratorsLoadingEvent;
import net.mcreator.plugin.events.WorkspaceSelectorLoadedEvent;
import net.mcreator.ui.init.UIRES;
import net.mcreator.ui.workspace.selector.WorkspaceSelector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BukkitUI extends JavaPlugin {

    public static final Logger LOG = LogManager.getLogger("Bukkit-UI");

    public BukkitUI(Plugin plugin) {
        super(plugin);

        addListener(PreGeneratorsLoadingEvent.class, _ -> BukkitModElements.registerModElements());

        addListener(WorkspaceSelectorLoadedEvent.class, event -> {
            WorkspaceSelector selector = event.getWorkspaceSelector();
            selector.addWorkspaceButton("BukkitUI converter", UIRES.get("16px.swap"),_-> {
                // If you want to keep your innocence, don't open this code and everything it executes. Trust me, it's even worse than your worst nightmare.
                new BukkitUIConverterDialog(selector);
            });
        });

        LOG.info("Bukkit-UI plugin was loaded");
    }
}