package org.jd.gui.service.preferencespanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.jd.gui.spi.PreferencesPanel;
import org.jd.gui.util.exception.ExceptionUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class ViewerPreferencesProvider extends JPanel implements PreferencesPanel, DocumentListener {
    protected static final int MIN_VALUE = 2;
    protected static final int MAX_VALUE = 40;
    protected static final String FONT_SIZE_KEY = "ViewerPreferences.fontSize";
    protected static final String LOOK_AND_FEEL_KEY = "ViewerPreferences.lookAndFeel";

    protected PreferencesPanel.PreferencesPanelChangeListener listener = null;
    protected JTextField fontSizeTextField;
    protected JComboBox<String> themeComboBox;
    protected Color errorBackgroundColor = Color.RED;
    protected Color defaultBackgroundColor;

    public ViewerPreferencesProvider() {
        super(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Font size
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(new JLabel("Font size (" + MIN_VALUE + ".." + MAX_VALUE + "): "), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        fontSizeTextField = new JTextField();
        fontSizeTextField.getDocument().addDocumentListener(this);
        formPanel.add(fontSizeTextField, gbc);

        // Theme
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formPanel.add(new JLabel("UI Theme: "), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        themeComboBox = new JComboBox<>(new String[] {
            "FlatLaf Light",
            "FlatLaf Dark",
            "FlatLaf IntelliJ",
            "FlatLaf Darcula",
            "System Default"
        });
        formPanel.add(themeComboBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        defaultBackgroundColor = fontSizeTextField.getBackground();
    }

    // --- PreferencesPanel --- //
    @Override public String getPreferencesGroupTitle() { return "Viewer"; }
    @Override public String getPreferencesPanelTitle() { return "Appearance"; }
    @Override public JComponent getPanel() { return this; }

    @Override public void init(Color errorBackgroundColor) {
        this.errorBackgroundColor = errorBackgroundColor;
    }

    @Override public boolean isActivated() { return true; }

    @Override
    public void loadPreferences(Map<String, String> preferences) {
        String fontSize = preferences.get(FONT_SIZE_KEY);

        if (fontSize == null) {
            // Search default value for the current platform
            RSyntaxTextArea textArea = new RSyntaxTextArea();

            try {
                String themePath = "rsyntaxtextarea/themes/eclipse.xml";
                String currentLaf = UIManager.getLookAndFeel().getClass().getName();
                if (currentLaf.contains("FlatDarkLaf") || currentLaf.contains("FlatDarculaLaf") || currentLaf.contains("Dark")) {
                    themePath = "rsyntaxtextarea/themes/dark.xml";
                }
                Theme theme = Theme.load(getClass().getClassLoader().getResourceAsStream(themePath));
                theme.apply(textArea);
            } catch (IOException e) {
                assert ExceptionUtil.printStackTrace(e);
            }

            fontSize = String.valueOf(textArea.getFont().getSize());
        }

        fontSizeTextField.setText(fontSize);
        fontSizeTextField.setCaretPosition(fontSizeTextField.getText().length());

        String laf = preferences.get(LOOK_AND_FEEL_KEY);
        if (laf == null) {
            laf = "com.formdev.flatlaf.FlatLightLaf";
        }
        if (laf.equals("com.formdev.flatlaf.FlatDarkLaf")) {
            themeComboBox.setSelectedIndex(1);
        } else if (laf.equals("com.formdev.flatlaf.FlatIntelliJLaf")) {
            themeComboBox.setSelectedIndex(2);
        } else if (laf.equals("com.formdev.flatlaf.FlatDarculaLaf")) {
            themeComboBox.setSelectedIndex(3);
        } else if (laf.equals(UIManager.getSystemLookAndFeelClassName())) {
            themeComboBox.setSelectedIndex(4);
        } else {
            themeComboBox.setSelectedIndex(0);
        }
    }

    @Override
    public void savePreferences(Map<String, String> preferences) {
        preferences.put(FONT_SIZE_KEY, fontSizeTextField.getText());

        String selectedLaf;
        switch (themeComboBox.getSelectedIndex()) {
            case 1: selectedLaf = "com.formdev.flatlaf.FlatDarkLaf"; break;
            case 2: selectedLaf = "com.formdev.flatlaf.FlatIntelliJLaf"; break;
            case 3: selectedLaf = "com.formdev.flatlaf.FlatDarculaLaf"; break;
            case 4: selectedLaf = UIManager.getSystemLookAndFeelClassName(); break;
            default: selectedLaf = "com.formdev.flatlaf.FlatLightLaf"; break;
        }
        preferences.put(LOOK_AND_FEEL_KEY, selectedLaf);

        String currentLaf = UIManager.getLookAndFeel().getClass().getName();
        if (!currentLaf.equals(selectedLaf)) {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (selectedLaf.equals("com.formdev.flatlaf.FlatLightLaf")) {
                        com.formdev.flatlaf.FlatLightLaf.setup();
                    } else if (selectedLaf.equals("com.formdev.flatlaf.FlatDarkLaf")) {
                        com.formdev.flatlaf.FlatDarkLaf.setup();
                    } else if (selectedLaf.equals("com.formdev.flatlaf.FlatIntelliJLaf")) {
                        com.formdev.flatlaf.FlatIntelliJLaf.setup();
                    } else if (selectedLaf.equals("com.formdev.flatlaf.FlatDarculaLaf")) {
                        com.formdev.flatlaf.FlatDarculaLaf.setup();
                    } else {
                        UIManager.setLookAndFeel(selectedLaf);
                    }
                    // Update all windows
                    for (Window window : Window.getWindows()) {
                        SwingUtilities.updateComponentTreeUI(window);
                    }
                } catch (Exception ex) {
                    assert ExceptionUtil.printStackTrace(ex);
                }
            });
        }
    }

    @Override
    public boolean arePreferencesValid() {
        try {
            int i = Integer.valueOf(fontSizeTextField.getText());
            return (i >= MIN_VALUE) && (i <= MAX_VALUE);
        } catch (NumberFormatException e) {
            assert ExceptionUtil.printStackTrace(e);
            return false;
        }
    }

    @Override
    public void addPreferencesChangeListener(PreferencesPanel.PreferencesPanelChangeListener listener) {
        this.listener = listener;
    }

    // --- DocumentListener --- //
    @Override public void insertUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void removeUpdate(DocumentEvent e) { onTextChange(); }
    @Override public void changedUpdate(DocumentEvent e) { onTextChange(); }

    public void onTextChange() {
        fontSizeTextField.setBackground(arePreferencesValid() ? defaultBackgroundColor : errorBackgroundColor);

        if (listener != null) {
            listener.preferencesPanelChanged(this);
        }
    }
}
