package org.strassburger.builderbuilder.generator;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiJavaFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BuilderGeneratorTest extends BasePlatformTestCase {

    public void testGeneratesBuilderForSimpleFields() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private int age;
                }
                """);

        generate("with");

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private int age;

                    public static class Builder {
                        private String name;
                        private int age;

                        private Builder() {
                        }

                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Builder withAge(int age) {
                            this.age = age;
                            return this;
                        }

                        public Person build() {
                            Person result = new Person();
                            result.name = this.name;
                            result.age = this.age;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testMarksSetterAsDeprecatedWhenFieldIsDeprecated() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    @Deprecated
                    private int age;
                }
                """);

        generate("with");

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    @Deprecated
                    private int age;

                    public static class Builder {
                        private String name;
                        private int age;

                        private Builder() {
                        }
                
                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        @Deprecated
                        public Builder withAge(int age) {
                            this.age = age;
                            return this;
                        }

                        public Person build() {
                            Person result = new Person();
                            result.name = this.name;
                            result.age = this.age;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testEmptyPrefixUsesFieldNameDirectly() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                }
                """);

        generate("");

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static class Builder {
                        private String name;

                        private Builder() {
                        }
                
                        public Builder name(String name) {
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

    public void testBlankPrefixUsesFieldNameDirectly() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                }
                """);

        generate("   ");

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static class Builder {
                        private String name;
                
                        private Builder() {
                        }

                        public Builder name(String name) {
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

    public void testSkipsStaticAndFinalFields() {
        myFixture.configureByText("Config.java", """
                public class Config {
                    private static final String CONSTANT = "x";
                    private final int fixedValue = 1;
                    private String name;
                }
                """);

        generate("with");

        myFixture.checkResult("""
                public class Config {
                    private static final String CONSTANT = "x";
                    private final int fixedValue = 1;
                    private String name;

                    public static class Builder {
                        private String name;
                
                        private Builder() {
                        }

                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Config build() {
                            Config result = new Config();
                            result.name = this.name;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testUsesAllArgsConstructorForImmutableClass() {
        myFixture.configureByText("Point.java", """
                public class Point {
                    private final int x;
                    private final int y;

                    private Point(int x, int y) {
                        this.x = x;
                        this.y = y;
                    }
                }
                """);

        generate("with");

        myFixture.checkResult("""
                public class Point {
                    private final int x;
                    private final int y;

                    private Point(int x, int y) {
                        this.x = x;
                        this.y = y;
                    }

                    public static class Builder {
                        private int x;
                        private int y;
                
                        private Builder() {
                        }

                        public Builder withX(int x) {
                            this.x = x;
                            return this;
                        }

                        public Builder withY(int y) {
                            this.y = y;
                            return this;
                        }

                        public Point build() {
                            return new Point(x, y);
                        }
                    }
                }
                """, true);
    }

    public void testGeneratesButMethodWhenRequested() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private int age;
                }
                """);

        generate("with", true);

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private int age;

                    public static class Builder {
                        private String name;
                        private int age;

                        private Builder() {
                        }
                
                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Builder withAge(int age) {
                            this.age = age;
                            return this;
                        }

                        public Builder but() {
                            return new Builder().withName(name).withAge(age);
                        }

                        public Person build() {
                            Person result = new Person();
                            result.name = this.name;
                            result.age = this.age;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testGeneratesBuilderFactoryMethodWhenRequested() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, true, false, allFieldNames(psiClass));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    public static Builder builder() {
                        return new Builder();
                    }

                    public static class Builder {
                        private String name;

                        private Builder() {
                        }
                
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

    public void testBuilderFactoryMethodStaysImmediatelyAboveBuilderClassAmongOtherMembers() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;

                    @Override
                    public String toString() {
                        return "Person{" + name + "}";
                    }
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, true, false, allFieldNames(psiClass));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Person {
                    private String name;

                    @Override
                    public String toString() {
                        return "Person{" + name + "}";
                    }

                    public static Builder builder() {
                        return new Builder();
                    }

                    public static class Builder {
                        private String name;

                        private Builder() {
                        }
                
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

    public void testOnlySelectedFieldsAreIncludedInBuilder() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private int age;
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, false, false, Set.of("name"));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private int age;

                    public static class Builder {
                        private String name;

                        private Builder() {
                        }
                
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

    public void testConstructorMatchingConsidersOnlySelectedFields() {
        myFixture.configureByText("Point.java", """
                public class Point {
                    private final int x;
                    private final int y;
                    private String label;

                    private Point(int x, int y) {
                        this.x = x;
                        this.y = y;
                    }
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, false, false, Set.of("x", "y"));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Point {
                    private final int x;
                    private final int y;
                    private String label;

                    private Point(int x, int y) {
                        this.x = x;
                        this.y = y;
                    }

                    public static class Builder {
                        private int x;
                        private int y;

                        private Builder() {
                        }
                
                        public Builder withX(int x) {
                            this.x = x;
                            return this;
                        }

                        public Builder withY(int y) {
                            this.y = y;
                            return this;
                        }

                        public Point build() {
                            return new Point(x, y);
                        }
                    }
                }
                """, true);
    }

    public void testNullSafetyAnnotatesNullableFieldsAndChecksRequiredOnes() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private @org.jetbrains.annotations.Nullable String nickname;
                    private int age;
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, false, true, allFieldNames(psiClass));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private @org.jetbrains.annotations.Nullable String nickname;
                    private int age;

                    public static class Builder {
                        private @org.jspecify.annotations.Nullable String name;
                        private @org.jspecify.annotations.Nullable String nickname;
                        private int age;

                        private Builder() {
                        }
                
                        public Builder withName(String name) {
                            this.name = name;
                            return this;
                        }

                        public Builder withNickname(@org.jspecify.annotations.Nullable String nickname) {
                            this.nickname = nickname;
                            return this;
                        }

                        public Builder withAge(int age) {
                            this.age = age;
                            return this;
                        }

                        public Person build() {
                            if (name == null) {
                                throw new IllegalStateException("name must not be null");
                            }
                            Person result = new Person();
                            result.name = this.name;
                            result.nickname = this.nickname;
                            result.age = this.age;
                            return result;
                        }
                    }
                }
                """, true);
    }

    public void testNullSafetyChecksApplyBeforeConstructorBasedBuild() {
        myFixture.configureByText("Point.java", """
                public class Point {
                    private final String label;
                    @org.jetbrains.annotations.Nullable
                    private final String note;

                    private Point(String label, String note) {
                        this.label = label;
                        this.note = note;
                    }
                }
                """);

        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions("with", false, false, true, allFieldNames(psiClass));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));

        myFixture.checkResult("""
                public class Point {
                    private final String label;
                    @org.jetbrains.annotations.Nullable
                    private final String note;

                    private Point(String label, String note) {
                        this.label = label;
                        this.note = note;
                    }

                    public static class Builder {
                        private @org.jspecify.annotations.Nullable String label;
                        private @org.jspecify.annotations.Nullable String note;

                        private Builder() {
                        }
                
                        public Builder withLabel(String label) {
                            this.label = label;
                            return this;
                        }

                        public Builder withNote(@org.jspecify.annotations.Nullable String note) {
                            this.note = note;
                            return this;
                        }

                        public Point build() {
                            if (label == null) {
                                throw new IllegalStateException("label must not be null");
                            }
                            return new Point(label, note);
                        }
                    }
                }
                """, true);
    }

    public void testCanGenerateIsFalseWhenBuilderAlreadyExists() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;

                    public static class Builder {
                    }
                }
                """);

        assertFalse(BuilderGenerator.canGenerate(soleClass()));
    }

    private void generate(String prefix) {
        generate(prefix, false);
    }

    private void generate(String prefix, boolean generateButMethod) {
        PsiClass psiClass = soleClass();
        BuilderGenerationOptions options = new BuilderGenerationOptions(prefix, generateButMethod, false, false, allFieldNames(psiClass));
        WriteCommandAction.runWriteCommandAction(getProject(), () -> BuilderGenerator.generate(getProject(), psiClass, options));
    }

    private Set<String> allFieldNames(PsiClass psiClass) {
        return Arrays.stream(BuilderGenerator.candidateFields(psiClass))
                .map(PsiField::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private PsiClass soleClass() {
        return ((PsiJavaFile) myFixture.getFile()).getClasses()[0];
    }
}
