package org.ariake.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

public final class ArchitectureTest {
    private final JavaClasses ariakeClasses = new ClassFileImporter().importPackages("org.ariake");

    @Test
    public void publicApisDoNotDependOnRuntimeAdapters() {
        noClasses()
                .that()
                .resideInAnyPackage(
                        "org.ariake.config",
                        "org.ariake.http",
                        "org.ariake.websocket",
                        "org.ariake.metrics",
                        "org.ariake.server",
                        "org.ariake.tx")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("io.helidon..", "io.prometheus..", "com.arjuna..", "org.eclipse.persistence..")
                .check(ariakeClasses);
    }

    @Test
    public void helidonDependenciesStayInHelidonAdapter() {
        noClasses()
                .that()
                .resideOutsideOfPackage("org.ariake.server.helidon..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("io.helidon..")
                .check(ariakeClasses);
    }

    @Test
    public void frameworkDoesNotDependOnExamples() {
        noClasses()
                .that()
                .resideInAPackage("org.ariake..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.ariake.examples..")
                .check(ariakeClasses);
    }
}
