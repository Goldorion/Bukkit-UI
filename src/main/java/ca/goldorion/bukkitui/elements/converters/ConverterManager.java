package ca.goldorion.bukkitui.elements.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.mcreator.io.FileIO;
import net.mcreator.workspace.Workspace;
import net.mcreator.workspace.elements.ModElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ConverterFileIO {

    private static final Map<Workspace, Map<String, Integer>> CONVERTERS = new HashMap<>();

    private static final String FILE_NAME = "bukkit_ui_converters";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveFile(Workspace workspace) throws IOException {
        if (doesFileExist(workspace))
            converterFile(workspace).createNewFile();

        List<MEConverterData> list = new ArrayList<>();
        CONVERTERS.get(workspace).forEach((meName, versionNumber) -> list.add(new MEConverterData(meName, versionNumber)));

        FileIO.writeStringToFile(GSON.toJson(list, List.class), converterFile(workspace));
    }

    public static void loadFile(Workspace workspace) {
        String str = FileIO.readFileToString(converterFile(workspace));

        List<?> appliedConverters = GSON.fromJson(str, List.class);
        if (appliedConverters != null && !appliedConverters.isEmpty()) {
            appliedConverters.forEach(obj -> {
                if (obj instanceof MEConverterData(String modElementName, int versionNumber)) {
                    CONVERTERS.getOrDefault(workspace, new HashMap<>()).put(modElementName, versionNumber);
                }
            });
        }
    }

    public static void updateMEVersion(Workspace workspace, String meName, int versionNumber) {
        CONVERTERS.getOrDefault(workspace, new HashMap<>()).put(meName, versionNumber);
    }

    public static void updateMEVersion(Workspace workspace, MEConverterData data) {
        updateMEVersion(workspace, data.modElementName(), data.versionNumber());
    }

    public static int getMEVersion(Workspace workspace, ModElement me) {
        return CONVERTERS.get(workspace).get(me.getName());
    }

    public static boolean doesFileExist(Workspace workspace) {
        return converterFile(workspace).exists();
    }

    private static File folder(Workspace workspace) {
        return workspace.getFolderManager().getWorkspaceCacheDir();
    }

    private static File converterFile(Workspace workspace) {
        return new File(folder(workspace), FILE_NAME);
    }
}
