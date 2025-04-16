import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	val openApiGeneratorVersion = "7.6.0"
	val swaggerVersion = "2.2.22"
	val kotlinVersion = "2.0.0"
	val flywayVersion = "10.15.0"

	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.openapi.generator") version openApiGeneratorVersion
	id("io.swagger.core.v3.swagger-gradle-plugin") version swaggerVersion
	kotlin("plugin.serialization") version kotlinVersion
	kotlin("plugin.jpa") version "1.9.25"
	id("org.flywaydb.flyway") version flywayVersion
	id("java")
}

group = "invoice.management.system"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	val springDocVersion = "2.5.0"
	val jUnitJupiterVersion = "5.10.2"
	val testcontainersVersion = "1.19.8"

	implementation ("io.github.microutils:kotlin-logging-jvm:2.0.11")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocVersion")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.flywaydb:flyway-core")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.opencsv:opencsv:5.8")
	implementation("com.h2database:h2:2.2.220")

	implementation ("com.itextpdf:itext7-core:7.1.19")
	implementation ("io.konik:konik:1.1.0")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.mysql:mysql-connector-j")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-mysql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.testcontainers:mysql:${testcontainersVersion}")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
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
	kotlinOptions {
		freeCompilerArgs = listOf(
			"-Xjsr305=strict",
			"-Xjvm-default=all"
		)
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	minHeapSize = "512m"
	maxHeapSize = "2048m"
	jvmArgs = listOf("-XX:MaxMetaspaceSize=512m")
	useJUnitPlatform()
	testLogging {
		events("skipped", "passed", "failed")
	}
}

val openApiOutputDirPath = project.layout.buildDirectory.dir("generated_sources/openAPI")

tasks.register<Delete>("cleanOpenApiOutputDir") {
	description = "Delete generated code from OpenAPI files."
	group = tasks.openApiGenerate.get().group
	delete(openApiOutputDirPath.get().toString())
}

tasks.openApiGenerate {
	dependsOn(tasks.named("cleanOpenApiOutputDir"))
	inputSpec.set("$rootDir/src/main/resources/static/openapi.yml")
	outputDir.set(openApiOutputDirPath.get().toString())
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