plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "1.0.0"
}

group = "com.monitor"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // H2 in-memory database
    runtimeOnly("com.h2database:h2")
    
    // PostgreSQL for production
    runtimeOnly("org.postgresql:postgresql")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
}

graalvmNative {
    binaries {
        named("main") {
            buildArgs.add("--enable-preview")
        }
        named("test") {
            buildArgs.add("--enable-preview")
        }
    }
}
