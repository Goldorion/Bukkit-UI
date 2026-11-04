package ca.goldorion.bukkitui.ui.modguis;

import ca.goldorion.bukkitui.elements.PaperCommand;
import ca.goldorion.bukkitui.utils.PermissionValidator;
import net.mcreator.blockly.BlocklyCompileNote;
import net.mcreator.blockly.InternalBlocksLoader;
import net.mcreator.blockly.data.BlocklyLoader;
import net.mcreator.blockly.data.DynamicBlockLoader;
import net.mcreator.blockly.data.ToolboxBlock;
import net.mcreator.blockly.data.ToolboxType;
import net.mcreator.blockly.java.BlocklyToJava;
import net.mcreator.element.types.Command;
import net.mcreator.element.util.AnnotationUtils;
import net.mcreator.generator.blockly.BlocklyBlockCodeGenerator;
import net.mcreator.generator.blockly.ProceduralBlockCodeGenerator;
import net.mcreator.generator.template.TemplateGeneratorException;
import net.mcreator.ui.MCreator;
import net.mcreator.ui.blockly.*;
import net.mcreator.ui.component.JStringListField;
import net.mcreator.ui.component.util.ComponentUtils;
import net.mcreator.ui.component.util.PanelUtils;
import net.mcreator.ui.help.HelpUtils;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.modgui.IBlocklyPanelHolder;
import net.mcreator.ui.modgui.ModElementGUI;
import net.mcreator.ui.validation.ValidationGroup;
import net.mcreator.ui.validation.component.VTextField;
import net.mcreator.util.TestUtil;
import net.mcreator.workspace.elements.ModElement;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class PaperCommandGUI extends ModElementGUI<PaperCommand> implements IBlocklyPanelHolder {

    private final VTextField commandName = new VTextField(25).requireValue("elementgui.command.warning.empty_string")
            .enableRealtimeValidation();
    private final VTextField description = new VTextField(25);
    private final VTextField usage = new VTextField(25);
    private final JStringListField aliases = new JStringListField(mcreator, null);
    private final VTextField permission = new VTextField(25).requireValue("elementgui.command.warning.empty_string")
            .enableRealtimeValidation();
    private final VTextField permissionMessage = new VTextField(25);

    private final ValidationGroup page1group = new ValidationGroup();

    private BlocklyPanel blocklyPanel;
    private Map<String, ToolboxBlock> externalBlocks;
    private final CompileNotesPanel compileNotesPanel = new CompileNotesPanel();
    private final List<BlocklyChangedListener> blocklyChangedListeners = new ArrayList<>();

    public PaperCommandGUI(MCreator mcreator, ModElement modElement, boolean editingMode) {
        super(mcreator, modElement, editingMode);
        this.initGUI();
        super.finalizeGUI();
    }

    @Override
    public void addBlocklyChangedListener(BlocklyChangedListener listener) {
        blocklyChangedListeners.add(listener);
    }

    @Override
    protected void initGUI() {
        ComponentUtils.deriveFont(commandName, 16);

        JPanel properties = new JPanel(new GridLayout(3, 4, 4, 2));
        properties.setOpaque(false);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("command/name"), L10N.label("elementgui.command.name")));
        properties.add(commandName);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("bukkit_command/description"), L10N.label("elementgui.bukkit_command.description")));
        properties.add(description);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("bukkit_command/usage"), L10N.label("elementgui.bukkit_command.usage")));
        properties.add(usage);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("bukkit_command/aliases"), L10N.label("elementgui.bukkit_command.aliases")));
        aliases.setUniqueEntries(true);
        properties.add(aliases);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("bukkit_command/permission"), L10N.label("elementgui.bukkit_command.permission")));
        permission.setValidator(new PermissionValidator(permission));
        properties.add(permission);

        properties.add(
                HelpUtils.wrapWithHelpButton(this.withEntry("bukkit_command/permission_message"), L10N.label("elementgui.bukkit_command.permission_message")));
        properties.add(permissionMessage);

        page1group.addValidationElement(commandName);
        page1group.addValidationElement(permission);

        externalBlocks = BlocklyLoader.INSTANCE.getBlockLoader(BlocklyEditorType.COMMAND_ARG).getDefinedBlocks();

        blocklyPanel = new BlocklyPanel(mcreator, BlocklyEditorType.COMMAND_ARG);
        blocklyPanel.addTaskToRunAfterLoaded(() -> {
            InternalBlocksLoader.loadBlocksAndCategoriesInPanel(blocklyPanel);
            DynamicBlockLoader.loadBlocksAndCategoriesInPanel(blocklyPanel);
            BlocklyLoader.INSTANCE.getBlockLoader(BlocklyEditorType.COMMAND_ARG)
                    .loadBlocksAndCategoriesInPanel(blocklyPanel, ToolboxType.COMMAND);
            blocklyPanel.addChangeListener(
                    _ -> new Thread(() -> regenerateBlockAssemblies(true), "CommandRegenerate").start());
        });
        if (!isEditingMode()) {
            blocklyPanel.setInitialXML(AnnotationUtils.getBlocklyXMLDefaultValue(Command.class, "argsxml"));
        }

        JPanel blocklyAndToolbarPanel = new JPanel(new GridLayout());
        blocklyAndToolbarPanel.setOpaque(false);
        blocklyAndToolbarPanel.add(PanelUtils.northAndCenterElement(
                new BlocklyEditorToolbar(mcreator, BlocklyEditorType.COMMAND_ARG, blocklyPanel, false), blocklyPanel));

        compileNotesPanel.setPreferredSize(new Dimension(0, 70));

        JComponent args = PanelUtils.centerAndSouthElement(blocklyAndToolbarPanel, compileNotesPanel);
        ComponentUtils.makeSection(args, L10N.t("elementgui.command.arguments"));
        args.setPreferredSize(new Dimension(0, 460));


        JPanel page1 = new JPanel(new BorderLayout(10, 10));
        page1.add("Center", PanelUtils.northAndCenterElement(PanelUtils.join(FlowLayout.LEFT, properties), args));

        page1.setOpaque(false);

        addPage(page1).validate(page1group).lazyValidate(BlocklyAggregatedValidationResult.blocklyValidator(this,
                message -> message.replace("Command", "Command arguments")));

        if (!isEditingMode()) {
            commandName.setText(modElement.getName().toLowerCase(Locale.ENGLISH));
            permission.setText(mcreator.getWorkspaceSettings().getModID() + "." + commandName.getText() + ".use");
        }
    }

    @Override
    public synchronized List<BlocklyCompileNote> regenerateBlockAssemblies(boolean jsEventTriggeredChange) {
        BlocklyToJava blocklyToJava;
        try {
            blocklyToJava = new BlocklyToJava(mcreator.getWorkspace(), this.modElement, BlocklyEditorType.COMMAND_ARG,
                    blocklyPanel.getXML(), null, new ProceduralBlockCodeGenerator(
                    new BlocklyBlockCodeGenerator(externalBlocks,
                            mcreator.getGeneratorStats().getBlocklyBlocks(BlocklyEditorType.COMMAND_ARG))));
        } catch (TemplateGeneratorException e) {
            TestUtil.failIfTestingEnvironment();
            return List.of(); // should not be possible to happen here
        }

        List<BlocklyCompileNote> compileNotesArrayList = blocklyToJava.getCompileNotes();

        SwingUtilities.invokeLater(() -> compileNotesPanel.updateCompileNotes(compileNotesArrayList));

        blocklyChangedListeners.forEach(l -> l.blocklyChanged(blocklyPanel, jsEventTriggeredChange));

        return compileNotesArrayList;
    }

    @Override
    public void openInEditingMode(PaperCommand command) {
        commandName.setText(command.commandName);
        description.setText(command.description);
        permission.setText(command.permission);
        permissionMessage.setText(command.permissionMessage);
        aliases.setTextList(command.aliases);
        usage.setText(command.usage);
        blocklyPanel.setInitialXML(command.argsxml);
    }

    @Override
    public PaperCommand getElementFromGUI() {
        PaperCommand command = new PaperCommand(modElement);
        command.commandName = commandName.getText();
        command.description = description.getText();
        command.usage = usage.getText();
        command.aliases = aliases.getTextList();
        command.permission = permission.getText();
        command.permissionMessage = permissionMessage.getText();
        command.argsxml = blocklyPanel.getXML();

        return command;
    }

    @Override
    public @Nullable URI contextURL() {
        return null;
    }

    @Override
    public Set<BlocklyPanel> getBlocklyPanels() {
        return Set.of(blocklyPanel);
    }

}
