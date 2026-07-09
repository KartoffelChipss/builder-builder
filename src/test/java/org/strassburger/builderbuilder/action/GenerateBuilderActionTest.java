package org.strassburger.builderbuilder.action;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class GenerateBuilderActionTest extends BasePlatformTestCase {

    public void testGeneratesBuilderForSimpleFields() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    private int age;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction());

        myFixture.checkResult("""
                public class Person {
                    private String name;
                    private int age;

                    public static class Builder {
                        private String name;
                        private int age;

                        public Builder name(String name) {
                            this.name = name;
                            return this;
                        }

                        public Builder age(int age) {
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

    public void testSkipsStaticAndFinalFields() {
        myFixture.configureByText("Config.java", """
                public class Config {
                    private static final String CONSTANT = "x";
                    private final int fixedValue = 1;
                    private String name;
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction());

        myFixture.checkResult("""
                public class Config {
                    private static final String CONSTANT = "x";
                    private final int fixedValue = 1;
                    private String name;

                    public static class Builder {
                        private String name;

                        public Builder name(String name) {
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
                    <caret>
                }
                """);

        myFixture.testAction(new GenerateBuilderAction());

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

                        public Builder x(int x) {
                            this.x = x;
                            return this;
                        }

                        public Builder y(int y) {
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

    public void testDoesNothingWhenBuilderAlreadyExists() {
        myFixture.configureByText("Person.java", """
                public class Person {
                    private String name;
                    <caret>

                    public static class Builder {
                    }
                }
                """);

        myFixture.testAction(new GenerateBuilderAction());

        myFixture.checkResult("""
                public class Person {
                    private String name;


                    public static class Builder {
                    }
                }
                """, true);
    }
}
