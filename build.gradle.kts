plugins {
    java
    application
    // ELIMINADO: id("org.javamodularity.moduleplugin") -> Causa conflictos si no hay module-info.java
    id("org.openjfx.javafxplugin") version "0.0.13"
    // NOTA: Se eliminó temporalmente 'jlink' porque esa herramienta exige estrictamente
    // el uso de módulos (JPMS), lo cual rompe la compatibilidad con Firebase.
}

group = "dev.alan20111"
version = "1.0.0"

repositories {
    mavenCentral()
}

val junitVersion = "5.12.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

application {
    // ELIMINADO: mainModule.set(...) -> Porque borramos el module-info.java
    mainClass.set("dev.alan20111.todolist.Launcher")
}

javafx {
    version = "21.0.6"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")

    implementation("com.google.firebase:firebase-admin:9.2.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation("com.itextpdf:kernel:7.2.5")
    implementation("com.itextpdf:layout:7.2.5")
    implementation("com.itextpdf:io:7.2.5")

    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    implementation("com.itextpdf:itextpdf:5.5.13.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// --- LA ORDEN ESTRICTA: APAGAR EL SISTEMA DE MÓDULOS EN LA EJECUCIÓN ---
// Esto fuerza a JavaFX, Firebase y tus Reportes a convivir pacíficamente en el Classpath
tasks.withType<JavaExec> {
    modularity.inferModulePath.set(false)
}