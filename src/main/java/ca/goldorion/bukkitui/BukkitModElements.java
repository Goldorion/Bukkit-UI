package ca.goldorion.bukkitui;

import ca.goldorion.bukkitui.elements.PaperCommand;
import ca.goldorion.bukkitui.ui.modguis.PaperCommandGUI;
import net.mcreator.element.ModElementType;
import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.generator.GeneratorFlavor;

public class BukkitModElements {

    public static ModElementType<?> PAPER_COMMAND;

    public static void registerModElements() {
        PAPER_COMMAND = ModElementTypeLoader.register(new ModElementType<>("papercommand", "command", 'c',
                PaperCommandGUI::new, PaperCommand.class).coveredOn(GeneratorFlavor.SPIGOT, GeneratorFlavor.PAPER));
    }
}
