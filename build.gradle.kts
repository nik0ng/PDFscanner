plugins {
    kotlin("jvm") version "1.5.31" // Замените "1.5.31" на актуальную версию Kotlin
}

// Настройки компиляции и исполнения Kotlin кода
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"

}

// Настройки репозиториев для зависимостей
repositories {
    mavenCentral()
}

// Зависимости проекта
dependencies {
    implementation(kotlin("stdlib-jdk8")) // Зависимость на стандартную библиотеку Kotlin
    implementation("org.apache.pdfbox:pdfbox:2.0.16") // Зависимость на Apache PDFBox
    implementation("org.apache.pdfbox:fontbox:2.0.16") // Зависимость на Apache FontBox
}

// Настройки JAR файла
tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.pdfparse.PDFscanner" // Указываем главный класс для JAR файла (PDFscannerKt для Kotlin)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Добавляем стратегию обработки дубликатов
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    archiveFileName.set("PDFscanner-all.jar")
}