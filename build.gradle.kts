plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.11.0")
}


tasks.shadowJar {
    archiveBaseName.set("testplugin")
    destinationDirectory.set(file("C:\\Users\\USER\\Documents\\ServerEngine\\servers\\server_441974653\\plugins"))
    manifest {
        attributes["Main-Class"] = "org.zepelown.kotlintestplugin.KotlinTestPlugin"
    }
}
