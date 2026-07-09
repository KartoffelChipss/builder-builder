package org.strassburger.builderbuilder.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public final class BuilderBuilderConfigurable implements Configurable {

    private JTextField methodPrefixField;
    private JCheckBox generateButMethodCheckBox;
    private JCheckBox generateBuilderMethodCheckBox;
    private JCheckBox generateNullSafetyCheckBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Builder Builder";
    }

    @Override
    public @Nullable JComponent createComponent() {
        methodPrefixField = new JTextField(15);
        generateButMethodCheckBox = new JCheckBox("Generate but() method for creating modified copies");
        generateBuilderMethodCheckBox = new JCheckBox("Generate static builder() factory method");
        generateNullSafetyCheckBox = new JCheckBox("Add JSpecify @Nullable annotations and null-check required fields");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 0, 0, 0);

        JPanel prefixRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        prefixRow.add(new JLabel("Default method prefix:"));
        prefixRow.add(Box.createHorizontalStrut(8));
        prefixRow.add(methodPrefixField);
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 0);
        panel.add(prefixRow, c);

        c.gridy = 1;
        c.insets = new Insets(4, 0, 0, 0);
        panel.add(generateBuilderMethodCheckBox, c);

        c.gridy = 2;
        panel.add(generateButMethodCheckBox, c);

        c.gridy = 3;
        panel.add(generateNullSafetyCheckBox, c);

        c.gridy = 4;
        c.weighty = 1;
        c.fill = GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), c);

        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        BuilderBuilderSettings settings = BuilderBuilderSettings.getInstance();
        return !methodPrefixField.getText().equals(settings.methodPrefix)
                || generateButMethodCheckBox.isSelected() != settings.generateButMethod
                || generateBuilderMethodCheckBox.isSelected() != settings.generateBuilderMethod
                || generateNullSafetyCheckBox.isSelected() != settings.generateNullSafety;
    }

    @Override
    public void apply() {
        BuilderBuilderSettings settings = BuilderBuilderSettings.getInstance();
        settings.methodPrefix = methodPrefixField.getText();
        settings.generateButMethod = generateButMethodCheckBox.isSelected();
        settings.generateBuilderMethod = generateBuilderMethodCheckBox.isSelected();
        settings.generateNullSafety = generateNullSafetyCheckBox.isSelected();
    }

    @Override
    public void reset() {
        BuilderBuilderSettings settings = BuilderBuilderSettings.getInstance();
        methodPrefixField.setText(settings.methodPrefix);
        generateButMethodCheckBox.setSelected(settings.generateButMethod);
        generateBuilderMethodCheckBox.setSelected(settings.generateBuilderMethod);
        generateNullSafetyCheckBox.setSelected(settings.generateNullSafety);
    }

    JTextField getMethodPrefixField() {
        return methodPrefixField;
    }

    JCheckBox getGenerateButMethodCheckBox() {
        return generateButMethodCheckBox;
    }

    JCheckBox getGenerateBuilderMethodCheckBox() {
        return generateBuilderMethodCheckBox;
    }

    JCheckBox getGenerateNullSafetyCheckBox() {
        return generateNullSafetyCheckBox;
    }
}
