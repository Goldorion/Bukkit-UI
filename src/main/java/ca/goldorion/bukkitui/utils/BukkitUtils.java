package ca.goldorion.bukkitui.utils;

import net.mcreator.element.GeneratableElement;
import net.mcreator.generator.GeneratorFlavor;
import net.mcreator.generator.io.GradleTrackingFileIO;
import net.mcreator.io.FileIO;
import net.mcreator.workspace.CorruptedWorkspaceFileException;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.WorkspaceFileManager;
import net.mcreator.workspace.elements.FolderElement;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;

import static ca.goldorion.bukkitui.BukkitUI.LOG;

public class WorkspaceUtils {

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

    @Nullable
    public static Workspace fileToWorkspace(File workspaceFile) throws CorruptedWorkspaceFileException, FileNotFoundException {
        if (workspaceFile.isFile()) {
            String workspace_string = FileIO.readFileToString(workspaceFile);
            Workspace retval;
            try {
                retval = WorkspaceFileManager.gson.fromJson(workspace_string, Workspace.class);
            } catch (Exception e) {
                throw new CorruptedWorkspaceFileException(e);
            }

            return retval;
        } else {
            throw new FileNotFoundException();
        }
    }

    public static boolean isBukkitWorkspace(Workspace workspace) {
        return workspace != null && (workspace.getGeneratorConfiguration().getGeneratorFlavor() == GeneratorFlavor.PAPER ||
                workspace.getGeneratorConfiguration().getGeneratorFlavor() == GeneratorFlavor.SPIGOT);
    }
}
