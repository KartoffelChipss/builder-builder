package org.strassburger.builderbuilder.action;

import com.intellij.openapi.ui.TestDialogManager;
import com.intellij.openapi.ui.TestInputDialog;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class GenerateBuilderActionTest extends BasePlatformTestCase {

    public void testGeneratesBuilderUsingPrefixEnteredInDialog() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>
                }
                """);

        withTestInputDialog(message -> "set", () -> myFixture.testAction(new GenerateBuilderAction()));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static class Builder {
                        private String name;

                        public Builder setName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Person build() {
                            Person result = new Person();
                            result.name = this.name;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testCancellingDialogMakesNoChanges() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>
                }
                """);

        withTestInputDialog(message -> null, () -> myFixture.testAction(new GenerateBuilderAction()));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                }
                """, true);
    }

    public void testDoesNothingWhenBuilderAlreadyExists() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>

                    public static class Builder {
                    }
                }
                """);

        withTestInputDialog(message -> {
            throw new AssertionError("dialog should not be shown when a Builder already exists");
        }, () -> myFixture.testAction(new GenerateBuilderAction()));

        myFixture.checkResult("""
                public class Person {
                    private String name;


                    public static class Builder {
                    }
                }
                """, true);
    }

    private void withTestInputDialog(TestInputDialog dialog, Runnable action) {
        TestInputDialog previous = TestDialogManager.setTestInputDialog(dialog);
        try {
            action.run();
        } finally {
            TestDialogManager.setTestInputDialog(previous);
        }
    }
}
