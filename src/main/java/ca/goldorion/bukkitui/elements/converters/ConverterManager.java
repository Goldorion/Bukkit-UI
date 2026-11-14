package ca.goldorion.bukkitui.elements.converters;

import ca.goldorion.bukkitui.utils.BukkitModElement;
import ca.goldorion.bukkitui.utils.BukkitUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.mcreator.io.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConverterManager {

    private static final Map<File, Map<String, Integer>> CONVERTERS = new HashMap<>();

    private static final String FILE_NAME = "bukkit_ui_converters";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveFile(File workspaceFile) throws IOException {
        if (doesFileExist(workspaceFile))
            converterFile(workspaceFile).createNewFile();

        List<MEConverterData> list = new ArrayList<>();
        CONVERTERS.get(workspaceFile).forEach((meName, versionNumber) -> list.add(new MEConverterData(meName, versionNumber)));

        FileIO.writeStringToFile(GSON.toJson(list, List.class), converterFile(workspaceFile));
    }

    public static void loadFile(File workspaceFile) throws IOException {
        if (!CONVERTERS.containsKey(workspaceFile)) {
            CONVERTERS.put(workspaceFile, new HashMap<>());
        }

        File converterFile = converterFile(workspaceFile);

        if (!doesFileExist(converterFile)) {
            converterFile.createNewFile();
            return;
        }

        String str = FileIO.readFileToString(converterFile);

        if (str.isBlank())
            return;

        try {
            List<?> appliedConverters = GSON.fromJson(str, List.class);
            if (appliedConverters != null && !appliedConverters.isEmpty()) {
                appliedConverters.forEach(obj -> {
                    if (obj instanceof MEConverterData(String modElementName, int versionNumber)) {
                        CONVERTERS.getOrDefault(workspaceFile, new HashMap<>()).put(modElementName, versionNumber);
                    }
                });
            }
        } catch (JsonSyntaxException _) {
        }
    }

    public static void updateMEVersion(File workspaceFile, String meName, int versionNumber) {
        CONVERTERS.getOrDefault(workspaceFile, new HashMap<>()).put(meName, versionNumber);
    }

    public static void updateMEVersion(File workspaceFile, BukkitModElement me, int versionNumber) {
        updateMEVersion(workspaceFile, me.getName(), versionNumber);
    }

    public static int getMEVersion(File workspaceFile, BukkitModElement me) {
        return CONVERTERS.get(workspaceFile).getOrDefault(me.getName(), 0);
    }

    private static boolean doesFileExist(File workspaceFile) {
        return converterFile(workspaceFile).exists();
    }

    private static File converterFile(File workspaceFile) {
        return new File(BukkitUtils.cacheFolder(workspaceFile), FILE_NAME);
    }
}
