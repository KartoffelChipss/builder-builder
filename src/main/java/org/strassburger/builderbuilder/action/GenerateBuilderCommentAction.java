package org.strassburger.builderbuilder.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public class GenerateBuilderCommentAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || editor == null || psiFile == null) return;

        PsiClass psiClass = enclosingClassAt(editor, psiFile);
        if (psiClass == null) return;

        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        String comment = "// " + psiClass.getName();
        WriteCommandAction.runWriteCommandAction(project, () -> document.insertString(offset, comment));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        boolean enabled = e.getProject() != null
                && editor != null
                && psiFile != null
                && enclosingClassAt(editor, psiFile) != null;
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
