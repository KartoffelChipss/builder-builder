package org.strassburger.builderbuilder.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.util.Set;

public class GenerateBuilderDialog extends DialogWrapper {

    private final JTextField methodPrefixField = new JTextField("with", 15);
    private final JCheckBox generateButMethodCheckBox = new JCheckBox("Generate but() method for creating modified copies");
    private final JCheckBox generateBuilderMethodCheckBox = new JCheckBox("Generate static builder() factory method", true);

    public GenerateBuilderDialog(@Nullable Project project) {
        super(project);
        setTitle("Generate Builder");
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
}
