package org.strassburger.builderbuilder.action;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;
import org.strassburger.builderbuilder.generator.BuilderGenerator;
import org.strassburger.builderbuilder.ui.GenerateBuilderDialog;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class GenerateBuilderAction extends AnAction {

    private final BiFunction<Project, PsiClass, BuilderGenerationOptions> optionsProvider;

    public GenerateBuilderAction() {
        this(GenerateBuilderAction::showDialog);
    }

    GenerateBuilderAction(BiFunction<Project, PsiClass, BuilderGenerationOptions> optionsProvider) {
        this.optionsProvider = optionsProvider;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || psiFile == null) return;

        PsiClass psiClass = enclosingClassAt(editor, psiFile);
        if (psiClass == null || !BuilderGenerator.canGenerate(psiClass)) return;

        BuilderGenerationOptions options = optionsProvider.apply(project, psiClass);
        if (options == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> BuilderGenerator.generate(project, psiClass, options));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        PsiClass psiClass = editor != null && psiFile != null ? enclosingClassAt(editor, psiFile) : null;
        boolean enabled = e.getProject() != null
                && psiClass != null
                && BuilderGenerator.canGenerate(psiClass);
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Nullable
    private static BuilderGenerationOptions showDialog(Project project, PsiClass psiClass) {
        GenerateBuilderDialog optionsDialog = new GenerateBuilderDialog(project);
        if (!optionsDialog.showAndGet()) return null;

        Set<String> selectedFieldNames = selectFields(project, BuilderGenerator.candidateFields(psiClass));
        if (selectedFieldNames == null) return null;

        return optionsDialog.getOptions(selectedFieldNames);
    }

    @Nullable
    private static Set<String> selectFields(Project project, PsiField[] candidateFields) {
        PsiFieldMember[] members = Arrays.stream(candidateFields)
                .map(PsiFieldMember::new)
                .toArray(PsiFieldMember[]::new);

        MemberChooser<PsiFieldMember> chooser = new MemberChooser<>(members, true, true, project);
        chooser.setTitle("Select Fields to Be Available in Builder");
        chooser.selectElements(members);

        if (!chooser.showAndGet()) return null;

        return chooser.getSelectedElements().stream()
                .map(member -> member.getElement().getName())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static PsiClass enclosingClassAt(Editor editor, PsiFile psiFile) {
        PsiElement elementAtCaret = psiFile.findElementAt(editor.getCaretModel().getOffset());
        return PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass.class);
    }
}
