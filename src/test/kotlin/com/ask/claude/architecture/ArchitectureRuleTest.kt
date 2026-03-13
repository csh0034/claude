package com.ask.claude.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Onion Architecture 의존성 방향 및 네이밍 컨벤션 검증 (Konsist)
 *
 * 의존성 방향: ui → application → domain ← infra, 모든 레이어 → common
 * Output Port: domain에 interface 정의(`MemberPort`), infra Adapter가 implements(`MemberAdapter`)
 */
class ArchitectureRuleTest {

    companion object {
        private val scope = Konsist.scopeFromProduction()

        private const val DOMAIN = "com.ask.claude.domain.."
        private const val APPLICATION = "com.ask.claude.application.."
        private const val INFRA = "com.ask.claude.infra.."
        private const val UI = "com.ask.claude.ui.."
        private const val COMMON = "com.ask.claude.common.."

        private const val DOMAIN_PREFIX = "com.ask.claude.domain"
        private const val APPLICATION_PREFIX = "com.ask.claude.application"
        private const val UI_PREFIX = "com.ask.claude.ui"
    }

    @Nested
    inner class LayerDependency {

        @Test
        fun `레이어 간 의존 방향이 올바르다`() {
            scope.assertArchitecture {
                val domain = Layer("Domain", DOMAIN)
                val application = Layer("Application", APPLICATION)
                val infra = Layer("Infra", INFRA)
                val ui = Layer("UI", UI)
                val common = Layer("Common", COMMON)

                domain.dependsOnNothing()
                application.dependsOn(domain)
                infra.dependsOn(domain, application)
                ui.dependsOn(application)
                common.dependsOnNothing()
            }
        }
    }

    @Nested
    inner class DomainLayer {

        private val domainFiles = scope.files.filter {
            it.packagee?.name?.startsWith(DOMAIN_PREFIX) == true
        }

        @Test
        fun `domain은 Spring 프레임워크를 참조할 수 없다`() {
            domainFiles.assertFalse {
                it.hasImport { import -> import.name.startsWith("org.springframework") }
            }
        }

        @Test
        fun `domain은 JPA를 참조할 수 없다`() {
            domainFiles.assertFalse {
                it.hasImport { import -> import.name.startsWith("jakarta.persistence") }
            }
        }

        @Test
        fun `domain은 Jackson을 참조할 수 없다`() {
            domainFiles.assertFalse {
                it.hasImport { import -> import.name.startsWith("com.fasterxml.jackson") }
            }
        }

        @Test
        fun `domain 클래스는 @Entity 어노테이션을 가질 수 없다`() {
            scope.classes()
                .filter { it.resideInPackage(DOMAIN) }
                .assertFalse { it.hasAnnotationWithName("Entity") }
        }

        @Test
        fun `domain 프로퍼티는 @Id 어노테이션을 가질 수 없다`() {
            scope.classes()
                .filter { it.resideInPackage(DOMAIN) }
                .flatMap { it.properties() }
                .assertFalse { it.hasAnnotationWithName("Id") }
        }

        @Test
        fun `domain 프로퍼티는 @Column 어노테이션을 가질 수 없다`() {
            scope.classes()
                .filter { it.resideInPackage(DOMAIN) }
                .flatMap { it.properties() }
                .assertFalse { it.hasAnnotationWithName("Column") }
        }

        @Test
        fun `Output Port 인터페이스는 domain 패키지에 있어야 한다`() {
            scope.interfaces()
                .withNameEndingWith("Port")
                .assertTrue { it.resideInPackage(DOMAIN) }
        }

        @Test
        fun `DomainEvent는 domain 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Event")
                .assertTrue { it.resideInPackage(DOMAIN) }
        }

        @Test
        fun `DomainService는 domain 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("DomainService")
                .assertTrue { it.resideInPackage(DOMAIN) }
        }
    }

    @Nested
    inner class ApplicationLayer {

        @Test
        fun `application Service는 Adapter 구현체를 직접 참조할 수 없다`() {
            scope.files
                .filter { it.packagee?.name?.startsWith(APPLICATION_PREFIX) == true }
                .assertFalse { it.hasImport { import -> import.name.contains("Adapter") } }
        }

        @Test
        fun `@Service 어노테이션 클래스는 application 패키지에 있어야 한다`() {
            scope.classes()
                .withAnnotationNamed("Service")
                .assertTrue { it.resideInPackage(APPLICATION) }
        }

        @Test
        fun `UseCase 인터페이스는 application 패키지에 있어야 한다`() {
            scope.interfaces()
                .withNameEndingWith("UseCase")
                .assertTrue { it.resideInPackage(APPLICATION) }
        }

        @Test
        fun `Command DTO는 application 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Command")
                .assertTrue { it.resideInPackage(APPLICATION) }
        }

        @Test
        fun `Result DTO는 application 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Result")
                .assertTrue { it.resideInPackage(APPLICATION) }
        }
    }

    @Nested
    inner class InfraLayer {

        @Test
        fun `Adapter는 infra 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Adapter")
                .assertTrue { it.resideInPackage(INFRA) }
        }

        @Test
        fun `JpaEntity는 infra 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("JpaEntity")
                .assertTrue { it.resideInPackage(INFRA) }
        }

        @Test
        fun `JpaRepository는 infra 패키지에 있어야 한다`() {
            scope.interfaces()
                .withNameEndingWith("JpaRepository")
                .assertTrue { it.resideInPackage(INFRA) }
        }

        @Test
        fun `Mapper는 infra 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Mapper")
                .assertTrue { it.resideInPackage(INFRA) }
        }
    }

    @Nested
    inner class UiLayer {

        @Test
        fun `Controller는 Service 구현체를 직접 참조할 수 없다 - UseCase 인터페이스만 사용`() {
            scope.files
                .filter { it.packagee?.name?.startsWith(UI_PREFIX) == true }
                .assertFalse { it.hasImport { import -> import.name.contains("Service") } }
        }

        @Test
        fun `Controller는 @RestController 어노테이션을 가져야 한다`() {
            scope.classes()
                .withNameEndingWith("Controller")
                .assertTrue { it.hasAnnotationWithName("RestController") }
        }

        @Test
        fun `Controller는 ui 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Controller")
                .assertTrue { it.resideInPackage(UI) }
        }

        @Test
        fun `Request DTO는 ui 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Request")
                .assertTrue { it.resideInPackage(UI) }
        }

        @Test
        fun `Response DTO는 ui 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Response")
                .assertTrue { it.resideInPackage(UI) }
        }

        @Test
        fun `ui는 domain의 Port를 직접 참조할 수 없다`() {
            scope.files
                .filter { it.packagee?.name?.startsWith(UI_PREFIX) == true }
                .assertFalse { it.hasImport { import -> import.name.contains("Port") } }
        }
    }

    @Nested
    inner class CommonLayer {

        @Test
        fun `Extension은 common 패키지에 있어야 한다`() {
            scope.classes()
                .withNameEndingWith("Extension")
                .assertTrue { it.resideInPackage(COMMON) }
        }

        @Test
        fun `@Configuration 어노테이션 클래스는 common 패키지에 있어야 한다`() {
            scope.classes()
                .withAnnotationNamed("Configuration")
                .assertTrue { it.resideInPackage(COMMON) }
        }
    }

    @Nested
    inner class GeneralRules {

        @Test
        fun `@Autowired 필드 주입을 사용할 수 없다 - 생성자 주입만 허용`() {
            scope.classes()
                .flatMap { it.properties() }
                .assertFalse { it.hasAnnotationWithName("Autowired") }
        }
    }
}
