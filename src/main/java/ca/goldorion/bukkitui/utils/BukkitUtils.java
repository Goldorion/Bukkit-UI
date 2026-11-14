package ca.goldorion.bukkitui.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.mcreator.io.FileIO;
import net.mcreator.preferences.PreferencesManager;
import net.mcreator.workspace.CorruptedWorkspaceFileException;
import net.mcreator.workspace.WorkspaceFileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static ca.goldorion.bukkitui.BukkitUI.LOG;

public class BukkitUtils {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static BukkitWorkspace fileToWorkspace(File workspaceFile) throws CorruptedWorkspaceFileException, FileNotFoundException {
        if (workspaceFile.isFile()) {
            String workspace_string = FileIO.readFileToString(workspaceFile);
            BukkitWorkspace retval;
            try {
                retval = WorkspaceFileManager.gson.fromJson(workspace_string, BukkitWorkspace.class);
                retval.workspaceFile = workspaceFile;
            } catch (Exception e) {
                throw new CorruptedWorkspaceFileException(e);
            }

            return retval;
        } else {
            throw new FileNotFoundException();
        }
    }

    public static void saveWorkspace(BukkitWorkspace workspace) {
        String workspacestring = new GsonBuilder().setPrettyPrinting().create().toJson(workspace);

        if (!workspacestring.isEmpty()) {
            // first we back up workspace file
            rotateWorkspaceFileBackup(workspace.workspaceFile);

            // We do an "atomic" write to the FS
            File outFile = workspace.workspaceFile;
            File tmpFile = new File(workspace.workspaceFile.getParent(), workspace.workspaceFile.getName() + ".lock");
            if (!FileIO.writeStringToFile(workspacestring, tmpFile)) {
                LOG.error("Failed to write workspace file to FS! Workspace is not saved!");
                return;
            }
            try {
                Files.move(tmpFile.toPath(), outFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                LOG.info("Failed to do atomic move, trying normal move!", e);
                try {
                    Files.move(tmpFile.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e1) {
                    tmpFile.delete(); // we delete the tmp file if it was not moved
                    LOG.warn("Falling back to normal write (non atomic, without move!)", e1);
                    if (!FileIO.writeStringToFile(workspacestring, outFile)) {
                        LOG.error("Failed to write workspace file directly to FS! Workspace is not saved!");
                        return;
                    }
                }
            }

            LOG.debug("Workspace stored on the FS");
        } else {
            LOG.error("Skipping workspace save. Workspace is defined but we failed to serialize it!");
        }
    }

    public static void saveModElement(BukkitWorkspace workspace, JsonObject element, String name) {
        File meFile = new File(elementsFolder(workspace.workspaceFile), name + ".mod.json");
        FileIO.writeStringToFile(GSON.toJson(element), meFile);
    }

    public static JsonObject readMEAsJsonObject(File workspaceFile, BukkitModElement modElement) {
        JsonObject element = GSON.fromJson(FileIO.readFileToString(new File(elementsFolder(workspaceFile), modElement.getName() + ".mod.json")), JsonObject.class);

        if (element != null && element.has("definition"))
            return element.get("definition").getAsJsonObject();
        else if (element != null)
            return element;
        else
            return JsonNull.INSTANCE.getAsJsonObject();
    }

    // Full copy and paste of MCreator's WorkspaceFileManager#rotateWorkspaceFileBackup method because it's private and I need it
    private static void rotateWorkspaceFileBackup(File workspaceFile) {
        int numberOfBackupsExcludingCurrent = PreferencesManager.PREFERENCES.backups.numberOfBackupsToStore.get() - 1;
        File[] existingBackupsArray = cacheFolder(workspaceFile).listFiles();

        if (existingBackupsArray != null && existingBackupsArray.length > 0) { // we already have some backups
            List<File> existingBackups = new ArrayList<>(Arrays.asList(existingBackupsArray));
            existingBackups.sort(Comparator.comparingLong(File::lastModified));
            Collections.reverse(existingBackups);
            long lastBackupTime = existingBackups.getFirst().lastModified();
            if ((System.currentTimeMillis() - lastBackupTime) / (1000 * 60)
                    > PreferencesManager.PREFERENCES.backups.automatedBackupInterval.get()) {  // check if we have surpassed backup interval
                if (existingBackupsArray.length
                        > numberOfBackupsExcludingCurrent) // only delete old ones if we have more than threshold of backups
                    existingBackups.stream().skip(numberOfBackupsExcludingCurrent).forEach(File::delete);
                createNewWorkspaceFileBackup(workspaceFile);
            }
        } else { // we don't have any backup yet
            createNewWorkspaceFileBackup(workspaceFile);
        }
    }

    // Full copy and paste of MCreator's WorkspaceFileManager#createNewWorkspaceFileBackup method because it's private and I need it
    private static void createNewWorkspaceFileBackup(File workspaceFile) {
        // if workspace file exists, so we can back it up, we back it up
        if (workspaceFile.isFile()) {
            File backupFile = new File(cacheFolder(workspaceFile),
                    workspaceFile.getName() + "-backup_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
            FileIO.copyFile(workspaceFile, backupFile);
        }
    }

    public static boolean isSpigotWorkspace(BukkitWorkspace workspace) {
        return workspace != null && workspace.workspaceSettings.getCurrentGenerator().startsWith("spigot");
    }

    public static boolean isPaperWorkspace(BukkitWorkspace workspace) {
        return workspace != null && workspace.workspaceSettings.getCurrentGenerator().startsWith("paper");
    }

    public static boolean isBukkitWorkspace(BukkitWorkspace workspace) {
        return isSpigotWorkspace(workspace) || isPaperWorkspace(workspace);
    }

    public static File folder(File workspaceFile) {
        return workspaceFile.getParentFile();
    }

    public static File cacheFolder(File workspaceFile) {
        return new File(folder(workspaceFile), ".mcreator/");
    }

    public static File elementsFolder(File workspaceFile) {
        return new File(folder(workspaceFile), "elements/");
    }
}
