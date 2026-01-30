plugins {
    // 不在根專案應用 AGP/Kotlin plugin，僅用來定義版本與共用設定
    id("org.jetbrains.kotlin.jvm") version "1.9.10" apply false
    id("com.android.application") version "8.4.0" apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}