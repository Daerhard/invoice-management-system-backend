import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.yaml:snakeyaml:2.3")
	}
}

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

	// ================= EMAIL =================
	implementation("org.springframework.boot:spring-boot-starter-mail")

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

// ============================= OPENAPI MERGE =============================

val openApiStaticDir = "$rootDir/src/main/resources/static"

tasks.register("mergeOpenApiSpecs") {
	group = "openapi tools"
	description = "Merges service specs referenced in openapi_master.yml into the combined openapi.yml"

	val masterFile = file("$openApiStaticDir/openapi_master.yml")
	val outputFile = file("$openApiStaticDir/openapi.yml")

	inputs.file(masterFile)
	inputs.files(
		file("$openApiStaticDir/openapi_invoice_management_system.yml"),
		file("$openApiStaticDir/openapi_e-mail_service.yml")
	)
	outputs.file(outputFile)

	doLast {
		val loaderOptions = org.yaml.snakeyaml.LoaderOptions()
		val yaml = org.yaml.snakeyaml.Yaml(
			org.yaml.snakeyaml.constructor.SafeConstructor(loaderOptions)
		)

		@Suppress("UNCHECKED_CAST")
		val masterSpec = yaml.load<Map<String, Any>>(masterFile.readText()) as Map<String, Any>

		@Suppress("UNCHECKED_CAST")
		val sources = (masterSpec["x-merge-sources"] as? List<*>)
			?.filterIsInstance<String>()
			?: error("openapi_master.yml must contain a non-empty 'x-merge-sources' list")
		val baseDir = masterFile.parentFile

		val mergedPaths = linkedMapOf<String, Any>()
		val mergedSchemas = linkedMapOf<String, Any>()

		sources.forEach { sourcePath ->
			val sourceFile = baseDir.resolve(sourcePath)
			require(sourceFile.exists()) {
				"Source spec file referenced in openapi_master.yml not found: ${sourceFile.absolutePath}"
			}
			@Suppress("UNCHECKED_CAST")
			val sourceSpec = yaml.load<Map<String, Any>>(sourceFile.readText()) as Map<String, Any>

			@Suppress("UNCHECKED_CAST")
			val paths = sourceSpec["paths"] as? Map<String, Any> ?: emptyMap()
			mergedPaths.putAll(paths)

			@Suppress("UNCHECKED_CAST")
			val components = sourceSpec["components"] as? Map<String, Any> ?: emptyMap()
			@Suppress("UNCHECKED_CAST")
			val schemas = components["schemas"] as? Map<String, Any> ?: emptyMap()
			mergedSchemas.putAll(schemas)
		}

		val merged = linkedMapOf<String, Any>(
			"openapi" to (masterSpec["openapi"] as String),
			"info" to (masterSpec["info"] as Map<*, *>),
			"servers" to (masterSpec["servers"] as List<*>),
			"paths" to mergedPaths,
			"components" to linkedMapOf("schemas" to mergedSchemas)
		)

		val dumperOptions = org.yaml.snakeyaml.DumperOptions().apply {
			defaultFlowStyle = org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK
			isPrettyFlow = true
			indent = 2
			indicatorIndent = 2
			indentWithIndicator = true
		}
		val dumper = org.yaml.snakeyaml.Yaml(dumperOptions)
		outputFile.writeText(dumper.dump(merged))

		println("✓ Merged ${sources.size} OpenAPI spec(s) into ${outputFile.name}")
	}
}

// ============================= OPENAPI GENERATION =============================

val openApiOutputDirPath =
	project.layout.buildDirectory.dir("generated_sources/openAPI")

tasks.register<Delete>("cleanOpenApiOutputDir") {
	group = "build"
	description = "Delete generated OpenAPI sources"
	delete(openApiOutputDirPath.get().asFile)
}

tasks.openApiGenerate {

	dependsOn(tasks.named("mergeOpenApiSpecs"), tasks.named("cleanOpenApiOutputDir"))

	inputSpec.set("$openApiStaticDir/openapi.yml")
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

// ============================= COMPILE =============================

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

sourceSets {
	val main by getting
	main.java.srcDir("${openApiOutputDirPath.get()}/src/main/kotlin")
}