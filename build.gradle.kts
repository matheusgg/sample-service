import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.math.BigDecimal.valueOf

plugins {
    jacoco
    kotlin("jvm") version "1.3.31"
    kotlin("kapt") version "1.3.31"
    kotlin("plugin.noarg") version "1.3.31"
    kotlin("plugin.spring") version "1.3.31"
    id("org.jetbrains.dokka") version "0.9.18"
    id("org.asciidoctor.convert") version "2.2.0"
    id("org.springframework.boot") version "2.1.5.RELEASE"
    id("com.gorylenko.gradle-git-properties") version "2.0.0"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
}

group = "br.com.sample"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    compile {
        exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    }
}

val kotlinLoggingVersion = "1.6.22"
val springAutoRestDocsVersion = "2.0.5"
val springRestDocsVersion = "2.0.3.RELEASE"
val mapStructVersion = "1.3.0.Final"
val springCloudVersion = "Greenwich.SR1"
val feignHttpClientVersion = "10.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-undertow")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.mapstruct:mapstruct:$mapStructVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("io.github.openfeign:feign-httpclient:${feignHttpClientVersion}")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.mapstruct:mapstruct-processor:$mapStructVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc:$springRestDocsVersion")
    testImplementation("capital.scalable:spring-auto-restdocs-core:$springAutoRestDocsVersion")
    testImplementation("capital.scalable:spring-auto-restdocs-json-doclet-jdk9:$springAutoRestDocsVersion")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

kapt {
    arguments {
        arg("mapstruct.defaultComponentModel", "spring")
    }
}

noArg {
    annotation("br.com.sample.mapper.base.NoArgConstructor")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<ProcessResources> {
    filesMatching("application.yml") {
        expand(project.properties)
    }
}

tasks.withType<Test> {
    addTestListener(object : TestListener {
        override fun beforeTest(testDescriptor: TestDescriptor?) {}

        override fun afterSuite(suite: TestDescriptor?, result: TestResult?) {}

        override fun beforeSuite(suite: TestDescriptor?) {}

        override fun afterTest(testDescriptor: TestDescriptor?, result: TestResult?) {
            logger.lifecycle("$testDescriptor")
            logger.lifecycle("Result: $result \n")
        }
    })
}

/*****************************************************************************
 * Auto Rest Docs Configuration
 *****************************************************************************/
val snippetsDir = file("$buildDir/generated-snippets")
val javadocJsonDir = file("$buildDir/generated-javadoc-json")

tasks {
    dokka {
        noJdkLink = true
        noStdlibLink = true
        includeNonPublic = true
        outputFormat = "auto-restdocs-json"
        outputDirectory = javadocJsonDir.absolutePath
        dokkaFatJar = "capital.scalable:spring-auto-restdocs-dokka-json:$springAutoRestDocsVersion"
    }

    test {
        outputs.dir(snippetsDir)
        systemProperty("org.springframework.restdocs.outputDir", snippetsDir)
        systemProperty("org.springframework.restdocs.javadocJsonDir", javadocJsonDir)
        doLast {
            copy {
                from(file("src/test/asciidoc/api.adoc"))
                into(file("$buildDir/tmp/asciidoc"))
            }
            val tmp = file("$buildDir/tmp/asciidoc/api.adoc")
            snippetsDir.walk()
                .filter { it.isFile && it.absolutePath.endsWith("auto-section.adoc") }
                .forEach {
                    tmp.appendText("\ninclude::${it.absolutePath}[]")
                }
        }
        dependsOn(dokka)
    }

    asciidoctor {
        options["backend"] = "html"
        options["doctype"] = "book"
        attributes["snippets"] = snippetsDir
        attributes["source-highlighter"] = "highlightjs"
        sourceDir = file("$buildDir/tmp/asciidoc")
        outputDir = file("$buildDir/generated-docs")
        dependsOn(test)
    }

    bootJar {
        from("$buildDir/generated-docs/html5") {
            into("BOOT-INF/classes/static/docs")
        }
        dependsOn(asciidoctor)
    }
}

/*****************************************************************************
 * JaCoCo Configuration
 *****************************************************************************/
fun applyExclusions(collection: ConfigurableFileCollection) {
    collection.setFrom(collection.files.flatMap {
        fileTree(it) {
            exclude(
                "**/*SampleServiceApplication*",
                "br/com/sample/model/**"
            )
        }
    })
}

afterEvaluate {
    tasks {
        jacoco {
            toolVersion = "0.8.2"
        }

        jacocoTestReport {
            reports {
                html.isEnabled = true
            }
            applyExclusions(classDirectories)
        }

        jacocoTestCoverageVerification {
            violationRules {
                rule {
                    element = "BUNDLE"
                    limit {
                        counter = "INSTRUCTION"
                        value = "COVEREDRATIO"
                        minimum = valueOf(0.1)
                    }
                }
            }
            applyExclusions(classDirectories)
            dependsOn(jacocoTestReport)
        }

        test {
            finalizedBy(jacocoTestCoverageVerification)
        }
    }
}
