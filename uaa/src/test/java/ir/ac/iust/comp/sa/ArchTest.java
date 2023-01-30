package ir.ac.iust.comp.sa;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchTest {

    @Test
    void servicesAndRepositoriesShouldNotDependOnWebLayer() {
        JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("ir.ac.iust.comp.sa");

        noClasses()
            .that()
            .resideInAnyPackage("ir.ac.iust.comp.sa.service..")
            .or()
            .resideInAnyPackage("ir.ac.iust.comp.sa.repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..ir.ac.iust.comp.sa.web..")
            .because("Services and repositories should not depend on web layer")
            .check(importedClasses);
    }
}
