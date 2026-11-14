package ca.goldorion.bukkitui.utils;

import net.mcreator.workspace.elements.*;
import net.mcreator.workspace.misc.CreativeTabsOrder;
import net.mcreator.workspace.settings.WorkspaceSettings;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

public class BukkitWorkspace {

    public LinkedHashSet<BukkitModElement> mod_elements = new LinkedHashSet<>();
    public LinkedHashSet<VariableElement> variable_elements = new LinkedHashSet<>(0);
    public LinkedHashSet<SoundElement> sound_elements = new LinkedHashSet<>(0);
    public LinkedHashMap<TagElement, ArrayList<TagElement.Entry>> tag_elements = new LinkedHashMap<>();
    public CreativeTabsOrder tab_element_order = new CreativeTabsOrder();
    public LinkedHashMap<String, LinkedHashMap<String, String>> language_map = new LinkedHashMap<>() {{
        put("en_us", new LinkedHashMap<>());
    }};

    @Nullable
    public LinkedHashMap<String, Object> metadata = null;

    public FolderElement foldersRoot = FolderElement.ROOT;

    public WorkspaceSettings workspaceSettings;
    public long mcreatorVersion;
    
    public transient File workspaceFile;
}
