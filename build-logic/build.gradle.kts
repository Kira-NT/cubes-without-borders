plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("omni-loom") {
            id = "dev.kirant.omni-loom"
            implementationClass = "dev.kirant.omniloom.OmniLoomPlugin"
        }
    }
}
