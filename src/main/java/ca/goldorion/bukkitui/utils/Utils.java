package ca.goldorion.bukkitui.utils;

import net.mcreator.element.GeneratableElement;
import net.mcreator.generator.io.GradleTrackingFileIO;
import net.mcreator.workspace.elements.FolderElement;
import net.mcreator.workspace.elements.ModElement;

import static ca.goldorion.bukkitui.BukkitUI.LOG;

public class Utils {

    public static void replaceMEByNewME(ModElement source, GeneratableElement result) {
        // Delete old files of the source ME as it will be converted to a different ME that will have different/own files
        source.getAssociatedFiles().forEach(f -> GradleTrackingFileIO.deleteFile(source.getWorkspace(), f));
        source.getWorkspace().removeModElement(source);

        result.getModElement()
                .setParentFolder(FolderElement.findFolderByPath(source.getWorkspace(), source.getFolderPath()));
        source.getWorkspace().getModElementManager().storeModElementPicture(result);
        source.getWorkspace().addModElement(result.getModElement());
        source.getWorkspace().getGenerator().generateElement(result);
        source.getWorkspace().getModElementManager().storeModElement(result);

        LOG.debug("Converted mod element {} ({}) to {} using a custom converter", source.getName(), source.getTypeString(),
                result.getModElement().getType().getRegistryName());
    }
}
