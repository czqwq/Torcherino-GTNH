plugins {
    id("com.github.ElytraServers.elytra-conventions") version "v1.1.1"
    id("com.gtnewhorizons.gtnhconvention")
}

// gtnhConvention does not configure a test sourceSet by default because
// GTNH tests are normally placed in a separate Gradle submodule.
// Explicitly wire src/test/java so the IDE and Gradle can compile
// Horizon-QA tests that live inside this module.
sourceSets {
    named("test") {
        java.srcDir("src/test/java")
    }
}

