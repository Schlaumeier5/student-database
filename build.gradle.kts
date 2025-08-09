plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "igs-landstuhl"
version = "1.0-SNAPSHOT"
application {
    mainClass.set("de.igslandstuhl.database.server.Server") // update to your actual main class if different
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("commons-codec:commons-codec:1.19.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // using JUnit 5 (latest)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("test.environment", "true")
}
tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}
tasks.shadowJar {
    archiveBaseName.set("student-database")
    archiveClassifier.set("") // no "-all" suffix, makes it the default jar
    archiveVersion.set(project.version.toString())    // omit version in filename if you want
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // or another version you prefer
    }
}