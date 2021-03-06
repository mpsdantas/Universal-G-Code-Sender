/**
 * Configure the controller settings.
 */
package com.willwinder.universalgcodesender.uielements;

import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfigGroups;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils.ConfigTuple;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class ControllerProcessorSettingsPanel extends AbstractUGSSettings {
    private static Logger logger = Logger.getLogger(ControllerProcessorSettingsPanel.class.getName());

    private final Map<String,ConfigTuple> configFiles;
    JComboBox controllerConfigs;
    final JTable customRemoverTable;
    final JButton add = new JButton(Localization.getString("settings.processors.add"));
    final JButton remove = new JButton(Localization.getString("settings.processors.remove"));
    private boolean updatingCombo = false;

    public ControllerProcessorSettingsPanel(Settings settings, IChanged changer, Map<String,ConfigTuple> configFiles) {
        super(settings, changer);
        this.configFiles = configFiles;
        this.controllerConfigs = new JComboBox(configFiles.keySet().toArray());
        this.customRemoverTable = initCustomRemoverTable(new JTable());
        super.updateComponents();

        controllerConfigs.addActionListener(e -> { if (!updatingCombo) super.updateComponents();});
        add.addActionListener(e -> this.addNewPatternRemover());
        remove.addActionListener(e -> this.removeSelectedPatternRemover());
    }

    public ControllerProcessorSettingsPanel(Settings settings, Map<String,ConfigTuple> configFiles) {
        this(settings, null, configFiles);
    }

    private void addNewPatternRemover() {
        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        model.addRow(new Object[]{true, ""});
    }

    private void removeSelectedPatternRemover() {
        int[] rows = customRemoverTable.getSelectedRows();
        Arrays.sort(rows);

        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        for (int i = rows.length; i > 0; i--) {
            int row = rows[i-1];
            model.removeRow(row);
        }
    }

    @Override
    public void save() {
        // In case there are in-progress changes.
        TableCellEditor editor = customRemoverTable.getCellEditor();
        if (editor != null) {
            editor.stopCellEditing();
        }

        ConfigTuple ct = configFiles.get(controllerConfigs.getSelectedItem());
        ct.loader.getProcessorConfigs().Custom.clear();

        // Roll up the pattern processors.
        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        for (int i = 0; i < customRemoverTable.getRowCount(); i++) {
            JsonObject args = new JsonObject();
            String pattern = model.getValueAt(i, 1).toString();
            if (!StringUtils.isEmpty(pattern)) {
                args.addProperty("pattern", pattern);
                ProcessorConfig pc = new ProcessorConfig(
                        "PatternRemover",
                        (Boolean) model.getValueAt(i, 0),
                        true,
                        args);
                ct.loader.getProcessorConfigs().Custom.add(pc);
            }
        }

        try {
            FirmwareUtils.save(ct.file, ct.loader);
        } catch (IOException ex) {
            GUIHelpers.displayErrorDialog("Problem saving controller config: " + ex.getMessage());
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    synchronized public void restoreDefaults() throws Exception {
        FirmwareUtils.restoreDefaults((String)controllerConfigs.getSelectedItem());

        updatingCombo = true;
        String selected = (String) controllerConfigs.getSelectedItem();
        this.controllerConfigs.removeAllItems();
        for (String item : configFiles.keySet()) {
            this.controllerConfigs.addItem(item);
        }
        controllerConfigs.setSelectedItem(selected);
        updatingCombo = false;
        updateComponentsInternal(settings);
    }

    @Override
    public String getHelpMessage() {
        return Localization.getString("settings.processors.help");
    }

    /**
     *  ------------------------------
     *  |  [      controller      ]  |
     *  | [ ] front processor 1      |
     *  | [ ] front processor 2      |
     *  | [ ] end processor 1        |
     *  | [ ] end processor 2        |
     * 
     *  | [+]                   [-]  |
     *  |  ________________________  |
     *  | | Enabled | Pattern      | |
     *  | |  [y]    | T\d+         | |
     *  | |  [n]    | M30          | |
     *  |  ------------------------  |
     *  |____________________________|
     */
    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();
        initCustomRemoverTable(customRemoverTable);
        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        super.addIgnoreChanges(controllerConfigs);

        ConfigTuple ct = configFiles.get(controllerConfigs.getSelectedItem());
        ProcessorConfigGroups pcg = ct.loader.getProcessorConfigs();
        System.out.println(ct.file);

        for (ProcessorConfig pc : pcg.Front) {
            add(new ProcessorConfigCheckbox(pc));
        }

        for (ProcessorConfig pc : pcg.End) {
            add(new ProcessorConfigCheckbox(pc));
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("wrap 3", "grow, fill", "grow, fill"));
        add(buttonPanel, add);
        add(buttonPanel, new JLabel());
        add(buttonPanel, remove);
        addIgnoreChanges(buttonPanel);

        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        for (ProcessorConfig pc : pcg.Custom) {
            Boolean enabled = pc.enabled;
            String pattern = "";
            if (pc.args != null && !pc.args.get("pattern").isJsonNull()) {
                pattern = pc.args.get("pattern").getAsString();
            }
            model.addRow(new Object[]{enabled, pattern});
        }
        addIgnoreChanges(new JScrollPane(customRemoverTable));

        SwingUtilities.updateComponentTreeUI(this);
    }

    private JTable initCustomRemoverTable(JTable table) {
        final String[] columnNames = {
            Localization.getString("settings.processors.enabled"),
            Localization.getString("PatternRemover")
        };

        final Class[] columnTypes =  {
            Boolean.class,
            String.class
        };

        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int idx) {
                return columnTypes[idx];
            }
        };

        table.setModel(model);
        table.getTableHeader().setReorderingAllowed(false);
        table.getModel().addTableModelListener((TableModelEvent e) -> change());

        return table;
    }
}
