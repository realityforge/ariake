package org.ariake.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.Test;

public final class ArchitectureTest {
    private final JavaClasses ariakeClasses = new ClassFileImporter().importPackages("org.ariake");

    @Test
    public void prometheusDependenciesStayInPrometheusMetrics() {
        noClasses()
                .that()
                .resideOutsideOfPackage("org.ariake.metrics.prometheus..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("io.prometheus..")
                .check(ariakeClasses);
    }

    @Test
    public void narayanaDependenciesStayInNarayanaTransactions() {
        noClasses()
                .that()
                .resideOutsideOfPackage("org.ariake.tx.narayana..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("com.arjuna..")
                .check(ariakeClasses);
    }

    @Test
    public void eclipseLinkDependenciesStayInEclipseLinkJpa() {
        noClasses()
                .that()
                .resideOutsideOfPackage("org.ariake.jpa.eclipselink..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.eclipse.persistence..")
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
