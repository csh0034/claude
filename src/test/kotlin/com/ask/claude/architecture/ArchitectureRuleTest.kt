package com.ask.claude.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields
import org.junit.jupiter.api.Test

/**
 * Onion Architecture 의존성 방향 및 네이밍 컨벤션 검증
 *
 * 의존성 방향: ui → application → domain ← infra, 모든 레이어 → common
 * Output Port: domain에 interface 정의, infra Adapter가 implements
 */
class ArchitectureRuleTest {

    companion object {
        private val importedClasses = ClassFileImporter()
            .withImportOption(ImportOption.DoNotIncludeTests())
            .importPackages("com.ask.claude")

        private const val DOMAIN = "com.ask.claude.domain.."
        private const val APPLICATION = "com.ask.claude.application.."
        private const val INFRA = "com.ask.claude.infra.."
        private const val UI = "com.ask.claude.ui.."
        private const val COMMON = "com.ask.claude.common.."
    }

    // =========================================================================
    // 레이어 의존성 방향 규칙
    // =========================================================================

    @Test
    fun `domain은 application을 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    @Test
    fun `domain은 infra를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `domain은 ui를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage(UI)
            .check(importedClasses)
    }

    @Test
    fun `application은 infra를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `application은 ui를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().resideInAPackage(UI)
            .check(importedClasses)
    }

    @Test
    fun `ui는 infra를 직접 참조할 수 없다`() {
        noClasses().that().resideInAPackage(UI)
            .should().dependOnClassesThat().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    // =========================================================================
    // common 레이어 의존성 규칙 — common은 어떤 레이어도 참조 금지
    // =========================================================================

    @Test
    fun `common은 domain을 참조할 수 없다`() {
        noClasses().that().resideInAPackage(COMMON)
            .should().dependOnClassesThat().resideInAPackage(DOMAIN)
            .check(importedClasses)
    }

    @Test
    fun `common은 application을 참조할 수 없다`() {
        noClasses().that().resideInAPackage(COMMON)
            .should().dependOnClassesThat().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    @Test
    fun `common은 infra를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(COMMON)
            .should().dependOnClassesThat().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `common은 ui를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(COMMON)
            .should().dependOnClassesThat().resideInAPackage(UI)
            .check(importedClasses)
    }

    // =========================================================================
    // domain 프레임워크 의존성 금지
    // =========================================================================

    @Test
    fun `domain은 Spring 프레임워크를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .check(importedClasses)
    }

    @Test
    fun `domain은 JPA를 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .check(importedClasses)
    }

    @Test
    fun `domain은 Jackson을 참조할 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
            .check(importedClasses)
    }

    // =========================================================================
    // JPA 어노테이션 금지 (domain)
    // =========================================================================

    @Test
    fun `domain 클래스는 @Entity 어노테이션을 가질 수 없다`() {
        noClasses().that().resideInAPackage(DOMAIN)
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .check(importedClasses)
    }

    @Test
    fun `domain 필드는 @Id 어노테이션을 가질 수 없다`() {
        noFields().that().areDeclaredInClassesThat().resideInAPackage(DOMAIN)
            .should().beAnnotatedWith("jakarta.persistence.Id")
            .check(importedClasses)
    }

    @Test
    fun `domain 필드는 @Column 어노테이션을 가질 수 없다`() {
        noFields().that().areDeclaredInClassesThat().resideInAPackage(DOMAIN)
            .should().beAnnotatedWith("jakarta.persistence.Column")
            .check(importedClasses)
    }

    // =========================================================================
    // Controller 의존성 규칙
    // =========================================================================

    @Test
    fun `Controller는 Service 구현체를 직접 참조할 수 없다 - UseCase 인터페이스만 사용`() {
        noClasses().that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Service")
            .check(importedClasses)
    }

    @Test
    fun `Controller는 @RestController 어노테이션을 가져야 한다`() {
        classes().that().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .check(importedClasses)
    }

    @Test
    fun `Controller는 ui 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage(UI)
            .check(importedClasses)
    }

    // =========================================================================
    // application Service 의존성 규칙
    // =========================================================================

    @Test
    fun `application Service는 Adapter 구현체를 직접 참조할 수 없다 - Output Port 인터페이스만 사용`() {
        noClasses().that().haveSimpleNameEndingWith("Service")
            .and().resideInAPackage(APPLICATION)
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Adapter")
            .check(importedClasses)
    }

    @Test
    fun `@Service 어노테이션 클래스는 application 패키지에 있어야 한다`() {
        classes().that().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    // =========================================================================
    // @Autowired 필드 주입 금지 — 생성자 주입만 허용
    // =========================================================================

    @Test
    fun `@Autowired 필드 주입을 사용할 수 없다 - 생성자 주입만 허용`() {
        noFields().that().areDeclaredInClassesThat().resideInAPackage("com.ask.claude..")
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .check(importedClasses)
    }

    // =========================================================================
    // 네이밍 컨벤션 — 위치 검증
    // =========================================================================

    @Test
    fun `UseCase 인터페이스는 application 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("UseCase")
            .should().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    @Test
    fun `Adapter는 infra 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Adapter")
            .should().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `JpaEntity는 infra 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("JpaEntity")
            .should().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `JpaRepository는 infra 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("JpaRepository")
            .should().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `Mapper는 infra 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Mapper")
            .should().resideInAPackage(INFRA)
            .check(importedClasses)
    }

    @Test
    fun `Command DTO는 application 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Command")
            .should().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    @Test
    fun `Result DTO는 application 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Result")
            .should().resideInAPackage(APPLICATION)
            .check(importedClasses)
    }

    @Test
    fun `Request DTO는 ui 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Request")
            .should().resideInAPackage(UI)
            .check(importedClasses)
    }

    @Test
    fun `Response DTO는 ui 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Response")
            .should().resideInAPackage(UI)
            .check(importedClasses)
    }

    @Test
    fun `Extension은 common 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Extension")
            .should().resideInAPackage(COMMON)
            .check(importedClasses)
    }

    // =========================================================================
    // Output Port — domain에 정의, infra Adapter가 구현
    // =========================================================================

    @Test
    fun `domain Output Port Repository 인터페이스는 domain 패키지에 있어야 한다`() {
        // MemberRepository(domain Port) ← 허용, MemberJpaRepository(infra) ← 제외
        classes().that().haveNameMatching(".*(?<!Jpa)Repository")
            .and().areInterfaces()
            .should().resideInAPackage(DOMAIN)
            .check(importedClasses)
    }

    @Test
    fun `DomainEvent는 domain 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("Event")
            .and().resideInAPackage("com.ask.claude..")
            .should().resideInAPackage(DOMAIN)
            .check(importedClasses)
    }

    @Test
    fun `DomainService는 domain 패키지에 있어야 한다`() {
        classes().that().haveSimpleNameEndingWith("DomainService")
            .should().resideInAPackage(DOMAIN)
            .check(importedClasses)
    }
}
