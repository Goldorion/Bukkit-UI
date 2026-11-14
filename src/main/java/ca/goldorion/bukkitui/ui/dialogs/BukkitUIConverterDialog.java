package ca.goldorion.bukkitui.ui.dialogs;

import ca.goldorion.bukkitui.elements.converters.ConverterManager;
import ca.goldorion.bukkitui.elements.converters.Converters;
import ca.goldorion.bukkitui.ui.BukkitRecentWorkspacesRenderer;
import ca.goldorion.bukkitui.utils.BukkitModElement;
import ca.goldorion.bukkitui.utils.BukkitUtils;
import ca.goldorion.bukkitui.utils.BukkitWorkspace;
import net.mcreator.element.ModElementType;
import net.mcreator.ui.dialogs.MCreatorDialog;
import net.mcreator.ui.dialogs.ProgressDialog;
import net.mcreator.ui.init.L10N;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.ui.workspace.selector.RecentWorkspaceEntry;
import net.mcreator.ui.workspace.selector.WorkspaceSelector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import static ca.goldorion.bukkitui.BukkitUI.LOG;

public class BukkitUIConverterDialog extends MCreatorDialog {

    private final JPanel convertWorkspacesPanel = new JPanel();
    private final CardLayout workspacePanes = new CardLayout();
    private final JPanel recentWorkspaceListPanel = new JPanel(workspacePanes);

    private final DefaultListModel<RecentWorkspaceEntry> defaultListModel = new DefaultListModel<>();
    private final JList<RecentWorkspaceEntry> workspacesList = new JList<>(defaultListModel);

    private final WorkspaceSelector parent;

    public BukkitUIConverterDialog(WorkspaceSelector parent) {
        super(parent, L10N.t("bukkitui.dialog.convert_workspace.title"), true);
        this.parent = parent;

        convertWorkspacesPanel.setLayout(new BorderLayout(5, 5));
        convertWorkspacesPanel.setOpaque(false);

        workspacesList.setBackground(Theme.current().getSecondAltBackgroundColor());
        workspacesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workspacesList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    openWorkspaceToConvert(workspacesList.getSelectedValue());
                }
            }
        });
        workspacesList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    openWorkspaceToConvert(workspacesList.getSelectedValue());
                }
            }
        });
        workspacesList.setCellRenderer(new BukkitRecentWorkspacesRenderer());
        JScrollPane recentsScrollPane = new JScrollPane(workspacesList);
        recentsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        recentWorkspaceListPanel.add("workspaces", workspacesList);

        convertWorkspacesPanel.add("Center", recentWorkspaceListPanel);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                super.windowActivated(e);
                loadWorkspacesList(parent);
            }
        });

        add(convertWorkspacesPanel);

        pack();
        setSize(Math.max(220, getBounds().width), 300);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void loadWorkspacesList(WorkspaceSelector selector) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        List<RecentWorkspaceEntry> discoveredWorkspaces = selector.getRecentWorkspaces().getList();

        if (!discoveredWorkspaces.isEmpty()) {
            defaultListModel.removeAllElements();
            defaultListModel.addAll(discoveredWorkspaces);
            workspacePanes.show(recentWorkspaceListPanel, "workspaces");
        } else {
            workspacePanes.show(recentWorkspaceListPanel, "noWorkspaces");
        }

        convertWorkspacesPanel.revalidate();

        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void openWorkspaceToConvert(RecentWorkspaceEntry recentWorkspace) {
        this.setVisible(false);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        File workspaceFile = recentWorkspace.getPath();

        ProgressDialog dial = new ProgressDialog(parent, L10N.t("dialog.workspace.import_from_zip.importing"));

        Thread t = new Thread(() -> {

            ProgressDialog.ProgressUnit p1 = new ProgressDialog.ProgressUnit(
                    L10N.t("dialog.bukkitui.convert_workspace.load"));
            dial.addProgressUnit(p1);

            try {
                BukkitWorkspace workspace = BukkitUtils.fileToWorkspace(workspaceFile);

                if (workspace == null) {
                    p1.markStateError();
                    return;
                }
                ConverterManager.loadFile(workspaceFile);

                p1.markStateOk();

                ProgressDialog.ProgressUnit p2 = new ProgressDialog.ProgressUnit(
                        L10N.t("dialog.bukkitui.convert_workspace.convert"));
                dial.addProgressUnit(p2);

                int modstoload = workspace.mod_elements.size();

                int i = 0;

                for (BukkitModElement me : workspace.mod_elements) {
                    LOG.debug("Checking to convert {}...", me.getName());

                    if (me.getType().equals(ModElementType.COMMAND)) {
                        int currentVersion = ConverterManager.getMEVersion(workspaceFile, me);
                        int newVersion = Converters.checkAndApplyConverters(workspace, me, currentVersion);

                        ConverterManager.updateMEVersion(workspaceFile, me, newVersion);
                        LOG.debug("Successfully converted {}", me.getName());
                    }

                    i++;
                    p2.setPercent((int) (i / (float) modstoload * 100));
                }

                p2.markStateOk();

                ProgressDialog.ProgressUnit p3 = new ProgressDialog.ProgressUnit(
                        L10N.t("dialog.bukkitui.convert_workspace.save"));
                dial.addProgressUnit(p3);

                ConverterManager.saveFile(workspaceFile);
                BukkitUtils.saveWorkspace(workspace);

                p3.markStateOk();

            } catch (Exception e) {
                LOG.error("Failed to import workspace", e);
            }

            dial.hideDialog();
        }, "BukkitUIConverter");
        t.start();
        dial.setVisible(true);

        if (parent.getApplication() != null) {
            parent.getApplication().openWorkspaceInMCreator(workspaceFile, true);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
