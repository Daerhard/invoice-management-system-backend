import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {

	val kotlinVersion = "2.1.10"
	val springBootVersion = "3.4.2"
	val dependencyMgmtVersion = "1.1.7"
	val flywayVersion = "10.22.0"
	val openApiGeneratorVersion = "7.10.0"
	val swaggerVersion = "2.2.27"

	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	kotlin("plugin.serialization") version kotlinVersion
	kotlin("plugin.jpa") version kotlinVersion

	id("org.springframework.boot") version springBootVersion
	id("io.spring.dependency-management") version dependencyMgmtVersion

	id("org.flywaydb.flyway") version flywayVersion
	id("org.openapi.generator") version openApiGeneratorVersion
	id("io.swagger.core.v3.swagger-gradle-plugin") version swaggerVersion

	id("java")
}

group = "invoice.management.system"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_23
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {

	val springDocVersion = "2.8.4"
	val jUnitJupiterVersion = "5.11.4"
	val testcontainersVersion = "1.20.4"

	// ================= SPRING =================
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

	// ================= OPEN API =================
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")

	// ================= JACKSON =================
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// ================= KOTLIN =================
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

	// ================= FILES / PDF =================
	implementation("com.opencsv:opencsv:5.9")
	implementation("com.itextpdf:itext7-core:8.0.4")
	implementation("io.konik:konik:1.1.0")

	// ================= DATABASE =================
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	runtimeOnly("com.mysql:mysql-connector-j:9.2.0")
	runtimeOnly("com.h2database:h2:2.3.232")

	// ================= DEV =================
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// ================= TEST =================
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.testcontainers:mysql:$testcontainersVersion")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Jar>("jar") {
	enabled = false
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xjvm-default=all"
		)
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {

	dependsOn(tasks.openApiGenerate)
	mustRunAfter(tasks.openApiGenerate)

	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_23)
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "failed", "skipped")
	}
}

val openApiOutputDirPath =
	project.layout.buildDirectory.dir("generated_sources/openAPI")

tasks.register<Delete>("cleanOpenApiOutputDir") {
	group = "build"
	description = "Delete generated OpenAPI sources"
	delete(openApiOutputDirPath.get().asFile)
}

tasks.openApiGenerate {

	dependsOn(tasks.named("cleanOpenApiOutputDir"))

	inputSpec.set("$rootDir/src/main/resources/static/openapi.yml")
	outputDir.set(openApiOutputDirPath.get().asFile.absolutePath)

	packageName.set(rootProject.name)
	invokerPackage.set("${rootProject.name}.security")
	apiPackage.set("${rootProject.name}.api")
	modelPackage.set("${rootProject.name}.model")
	modelNameSuffix.set("Dto")
	generatorName.set("kotlin-spring")

	configOptions.set(
		mapOf(
			"useSpringBoot3" to "true",
			"delegatePattern" to "true",
			"interfaceOnly" to "true",
			"dateLibrary" to "java8",
			"useTags" to "true",
			"enumPropertyNaming" to "UPPERCASE"
		)
	)
}

sourceSets {
	val main by getting
	main.java.srcDir("${openApiOutputDirPath.get()}/src/main/kotlin")
}