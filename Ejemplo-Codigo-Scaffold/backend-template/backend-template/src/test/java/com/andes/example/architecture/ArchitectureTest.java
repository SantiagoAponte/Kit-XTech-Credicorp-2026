package com.andes.example.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/** ADR-002/008 y A2 §7: regla de dependencia verificada automáticamente. */
@AnalyzeClasses(packages = "com.andes.example", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.andes.example";

    @ArchTest
    static final ArchRule domain_does_not_depend_on_outer_layers = noClasses().that()
            .resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".application..", BASE + ".adapter..", BASE + ".infrastructure..");

    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses().that()
            .resideInAPackage(BASE + ".domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta..", "javax.persistence..", "com.fasterxml..",
                    "org.hibernate..");

    @ArchTest
    static final ArchRule application_does_not_depend_on_adapters = noClasses().that()
            .resideInAPackage(BASE + ".application..")
            .should().dependOnClassesThat().resideInAnyPackage(BASE + ".adapter..", BASE + ".infrastructure..");

    @ArchTest
    static final ArchRule web_adapter_does_not_reference_persistence_adapter = noClasses().that()
            .resideInAPackage(BASE + ".adapter.in..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.out..");

    @ArchTest
    static final ArchRule persistence_adapter_does_not_reference_web_adapter = noClasses().that()
            .resideInAPackage(BASE + ".adapter.out..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".adapter.in..");

    @ArchTest
    static final ArchRule controllers_only_depend_on_input_ports = noClasses().that()
            .haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().resideInAnyPackage(
                    BASE + ".domain..", BASE + ".application.usecase..", BASE + ".application.port.out..",
                    BASE + ".adapter.out..", BASE + ".infrastructure..");

    @ArchTest
    static final ArchRule usecases_implement_input_ports = classes().that()
            .resideInAPackage(BASE + ".application.usecase..")
            .should().haveSimpleNameEndingWith("UseCase");
}
