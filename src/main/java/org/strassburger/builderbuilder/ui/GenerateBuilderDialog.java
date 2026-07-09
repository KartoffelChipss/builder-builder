package org.strassburger.builderbuilder.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;
import org.strassburger.builderbuilder.settings.BuilderBuilderSettings;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.Set;

public class GenerateBuilderDialog extends DialogWrapper {

    private final JTextField methodPrefixField = new JTextField(15);
    private final JCheckBox generateButMethodCheckBox = new JCheckBox("Generate but() method for creating modified copies");
    private final JCheckBox generateBuilderMethodCheckBox = new JCheckBox("Generate static builder() factory method");
    private final JCheckBox generateNullSafetyCheckBox = new JCheckBox("Add JSpecify @Nullable annotations and null-check required fields");

    public GenerateBuilderDialog(@Nullable Project project) {
        super(project);
        setTitle("Generate Builder");

        BuilderBuilderSettings settings = BuilderBuilderSettings.getInstance();
        methodPrefixField.setText(settings.methodPrefix);
        generateButMethodCheckBox.setSelected(settings.generateButMethod);
        generateBuilderMethodCheckBox.setSelected(settings.generateBuilderMethod);
        generateNullSafetyCheckBox.setSelected(settings.generateNullSafety);

        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel prefixRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        prefixRow.add(new JLabel("Method prefix:"));
        prefixRow.add(methodPrefixField);
        panel.add(prefixRow);

        JPanel builderMethodRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        builderMethodRow.add(generateBuilderMethodCheckBox);
        panel.add(builderMethodRow);

        JPanel checkboxRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkboxRow.add(generateButMethodCheckBox);
        panel.add(checkboxRow);

        JPanel nullSafetyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        nullSafetyRow.add(generateNullSafetyCheckBox);
        panel.add(nullSafetyRow);

        return panel;
    }

    @Override
    public @NotNull JComponent getPreferredFocusedComponent() {
        return methodPrefixField;
    }

    public BuilderGenerationOptions getOptions(Set<String> selectedFieldNames) {
        return new BuilderGenerationOptions(
                methodPrefixField.getText(),
                generateButMethodCheckBox.isSelected(),
                generateBuilderMethodCheckBox.isSelected(),
                generateNullSafetyCheckBox.isSelected(),
                selectedFieldNames);
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
