import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	//id("org.springframework.boot") version "2.1.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.7.RELEASE"

	kotlin("jvm") version "1.3.40"
	kotlin("plugin.spring") version "1.3.40"
}

group = "com.auritylab.spring.gcp.tasks"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	jcenter()
}

extra["springCloudVersion"] = "Greenwich.SR1"

dependencies {
	// kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.7")

	// spring
	implementation("org.springframework:spring-web:5.1.8.RELEASE")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter")

	// gcp standalone
	implementation("com.google.api-client:google-api-client:1.30.0")
	implementation("com.google.apis:google-api-services-cloudtasks:v2beta3-rev19-1.25.0")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
