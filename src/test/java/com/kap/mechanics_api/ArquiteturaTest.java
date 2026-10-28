package com.kap.mechanics_api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

// Regra de Dependência da Clean Architecture: o core não pode conhecer as camadas externas.
// Todos os contextos estão no core (Cliente, Veiculo, Servico, ItemEstoque, MovimentacaoEstoque, Orcamento,
// OrdemServico e Usuario). O login é o caso de uso AutenticarUsuarioUseCase; a porta é implementada em infra/security.
@AnalyzeClasses(packages = "com.kap.mechanics_api", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule coreNaoDependeDeFrameworks = noClasses()
            .that().resideInAPackage("..core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..", "org.mapstruct..");

    @ArchTest
    static final ArchRule coreNaoDependeDasCamadasExternas = noClasses()
            .that().resideInAPackage("..core..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    // prefixo completo: "..domain.." casaria também com core.cliente.domain
                    "com.kap.mechanics_api.adapter..", "com.kap.mechanics_api.infra..",
                    "com.kap.mechanics_api.controller..", "com.kap.mechanics_api.service..",
                    "com.kap.mechanics_api.repository..", "com.kap.mechanics_api.mapper..",
                    "com.kap.mechanics_api.dto..", "com.kap.mechanics_api.domain..",
                    "com.kap.mechanics_api.exception..", "com.kap.mechanics_api.config..",
                    "com.kap.mechanics_api.security..");

    @ArchTest
    static final ArchRule webNaoDependeDaPersistencia = noClasses()
            .that().resideInAPackage("com.kap.mechanics_api.adapter.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.kap.mechanics_api.adapter.persistence..", "org.springframework.data..");

    @ArchTest
    static final ArchRule persistenciaNaoDependeDaWeb = noClasses()
            .that().resideInAPackage("com.kap.mechanics_api.adapter.persistence..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("com.kap.mechanics_api.adapter.web..", "org.springframework.web..");

    @ArchTest
    static final ArchRule adaptersNaoDependemDeInfra = noClasses()
            .that().resideInAPackage("com.kap.mechanics_api.adapter..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.kap.mechanics_api.infra..");

    @ArchTest
    static final ArchRule semPacotesLegados = noClasses()
            .should().resideInAnyPackage(
                    "com.kap.mechanics_api.controller..", "com.kap.mechanics_api.service..",
                    "com.kap.mechanics_api.repository..", "com.kap.mechanics_api.mapper..",
                    "com.kap.mechanics_api.dto..", "com.kap.mechanics_api.domain..",
                    "com.kap.mechanics_api.exception..", "com.kap.mechanics_api.config..",
                    "com.kap.mechanics_api.security..", "com.kap.mechanics_api.documentation..");
}
