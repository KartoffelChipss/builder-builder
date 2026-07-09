package org.strassburger.builderbuilder.action;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.strassburger.builderbuilder.generator.BuilderGenerationOptions;

import java.util.Set;

public class GenerateBuilderActionTest extends BasePlatformTestCase {

    public void testGeneratesBuilderUsingOptionsFromDialog() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction(
                (project, psiClass) -> new BuilderGenerationOptions("set", false, false, Set.of("name"))));

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

    public void testGeneratesButMethodWhenRequestedInDialog() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction(
                (project, psiClass) -> new BuilderGenerationOptions("with", true, false, Set.of("name"))));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static class Builder {
                        private String name;

                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Builder but() {
                            return new Builder().withName(name);
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

    public void testGeneratesBuilderFactoryMethodWhenRequestedInDialog() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction(
                (project, psiClass) -> new BuilderGenerationOptions("with", false, true, Set.of("name"))));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static Builder builder() {
                        return new Builder();
                    }

                    public static class Builder {
                        private String name;

                        public Builder withName(String name) {
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

    public void testOnlySelectedFieldsAreIncluded() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private int age;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction(
                (project, psiClass) -> new BuilderGenerationOptions("with", false, false, Set.of("name"))));

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private int age;

                    public static class Builder {
                        private String name;

                        public Builder withName(String name) {
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

        myFixture.testAction(new GenerateBuilderAction((project, psiClass) -> null));

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

        myFixture.testAction(new GenerateBuilderAction((project, psiClass) -> {
            throw new AssertionError("dialog should not be shown when a Builder already exists");
        }));

        myFixture.checkResult("""
                public class Person {
                    private String name;


                    public static class Builder {
                    }
                }
                """, true);
    }
}
