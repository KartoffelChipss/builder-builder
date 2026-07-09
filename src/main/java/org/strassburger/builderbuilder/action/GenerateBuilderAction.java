package org.strassburger.builderbuilder.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.strassburger.builderbuilder.generator.BuilderGenerator;

public class GenerateBuilderAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || psiFile == null) return;

        PsiClass psiClass = enclosingClassAt(editor, psiFile);
        if (psiClass == null || !BuilderGenerator.canGenerate(psiClass)) return;

        String prefix = Messages.showInputDialog(project, "Method prefix:", "Generate Builder", null, "with", null);
        if (prefix == null) return;

        WriteCommandAction.runWriteCommandAction(project, () -> BuilderGenerator.generate(project, psiClass, prefix));
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

    private static PsiClass enclosingClassAt(Editor editor, PsiFile psiFile) {
        PsiElement elementAtCaret = psiFile.findElementAt(editor.getCaretModel().getOffset());
        return PsiTreeUtil.getParentOfType(elementAtCaret, PsiClass.class);
    }
}
