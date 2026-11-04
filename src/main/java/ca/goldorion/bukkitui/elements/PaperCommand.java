package ca.goldorion.bukkitui.elements;

import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.data.BlocklyXML;
import net.mcreator.blockly.java.BlocklyToJava;
import net.mcreator.element.GeneratableElement;
import net.mcreator.generator.blockly.BlocklyBlockCodeGenerator;
import net.mcreator.generator.blockly.ProceduralBlockCodeGenerator;
import net.mcreator.generator.template.IAdditionalTemplateDataProvider;
import net.mcreator.minecraft.MinecraftImageGenerator;
import net.mcreator.ui.blockly.BlocklyEditorType;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PaperCommand extends GeneratableElement {

    private static final String XML_BASE = "<xml xmlns=\"https://developers.google.com/blockly/xml\"><block type=\"args_start\" deletable=\"false\" x=\"40\" y=\"40\"><next><block type=\"call_procedure\"><field name=\"procedure\"></field></block></next></block></xml>";

    public String commandName;
    public String description;
    public String usage;
    public List<String> aliases;
    public String permission;
    public String permissionMessage;

    @BlocklyXML(name = "cmdargs", defaultXML = XML_BASE) public String argsxml;

    private PaperCommand() {
        this(null);
    }

    public PaperCommand(ModElement element) {
        super(element);

        this.aliases = new ArrayList<>();
    }

    @Override public BufferedImage generateModElementPicture() {
        return MinecraftImageGenerator.Preview.generateCommandPreviewPicture(commandName, argsxml);
    }

    @Override public @Nullable IAdditionalTemplateDataProvider getAdditionalTemplateData() {
        return additionalData -> {
            BlocklyBlockCodeGenerator blocklyBlockCodeGenerator = new BlocklyBlockCodeGenerator(
                    BlocklyLoader.INSTANCE.getBlockLoader(BlocklyEditorType.COMMAND_ARG).getDefinedBlocks(),
                    getModElement().getGenerator().getGeneratorStats().getBlocklyBlocks(BlocklyEditorType.COMMAND_ARG),
                    this.getModElement().getGenerator()
                            .getTemplateGeneratorFromName(BlocklyEditorType.COMMAND_ARG.registryName()),
                    additionalData).setTemplateExtension(
                    this.getModElement().getGeneratorConfiguration().getGeneratorFlavor().getBaseLanguage().name()
                            .toLowerCase(Locale.ENGLISH));
            BlocklyToJava blocklyToJava = new BlocklyToJava(this.getModElement().getWorkspace(), this.getModElement(),
                    BlocklyEditorType.COMMAND_ARG, this.argsxml, this.getModElement().getGenerator()
                    .getTemplateGeneratorFromName(BlocklyEditorType.COMMAND_ARG.registryName()),
                    new ProceduralBlockCodeGenerator(blocklyBlockCodeGenerator));

            additionalData.put("argscode", blocklyToJava.getGeneratedCode());
            additionalData.put("argsblocks", blocklyToJava.getUsedBlocks());
            additionalData.put("extra_templates_code", blocklyToJava.getExtraTemplatesCode());
        };
    }
}
