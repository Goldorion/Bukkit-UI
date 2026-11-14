package ca.goldorion.bukkitui.utils;

import net.mcreator.element.ModElementType;
import net.mcreator.element.ModElementTypeLoader;
import net.mcreator.minecraft.RegistryNameFixer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public class BukkitModElement {

    private final String name;
    private String type;
    private boolean compiles = true;
    private boolean locked_code = false;
    @Nullable
    private String registry_name;
    @Nullable
    private LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
    @Nullable
    private String path;


    public BukkitModElement(@Nonnull String name, ModElementType<?> type, String path) {
        this.name = name;
        this.type = type.getRegistryName();
        this.registry_name = RegistryNameFixer.fromCamelCase(name);
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public ModElementType<?> getType() {
        return ModElementTypeLoader.getModElementType(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCompiles() {
        return compiles;
    }

    public boolean isLocked_code() {
        return locked_code;
    }

    @Nullable
    public String getRegistry_name() {
        return registry_name;
    }

    @Nullable
    public LinkedHashMap<String, Object> getMetadata() {
        return metadata;
    }

    @Nullable
    public String getPath() {
        return path;
    }
}
