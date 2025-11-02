plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
}

version = "1.0.0-SNAPSHOT"
group = "com.bittokazi.oauth2.auth.frontend"

val testSpaFramework: Boolean = false

repositories {
    mavenCentral()
    mavenLocal()
    if(testSpaFramework) {
        flatDir {
            dirs("../../kotlin-kvision-spa-framework/build/libs")
        }
    }
}

// Versions
val kotlinVersion: String by System.getProperties()
val kvisionVersion: String by System.getProperties()

kotlin {
    js(IR) {
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }
    sourceSets["jsMain"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap:$kvisionVersion")
        implementation("io.kvision:kvision-datetime:$kvisionVersion")
        implementation("io.kvision:kvision-richtext:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-upload:$kvisionVersion")
        implementation("io.kvision:kvision-imask:$kvisionVersion")
        implementation("io.kvision:kvision-toastify:$kvisionVersion")
        implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
        implementation("io.kvision:kvision-bootstrap-icons:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-pace:$kvisionVersion")
        implementation("io.kvision:kvision-handlebars:$kvisionVersion")
        implementation("io.kvision:kvision-maps:$kvisionVersion")
        implementation("io.kvision:kvision-rest:$kvisionVersion")
        implementation("io.kvision:kvision-jquery:$kvisionVersion")
        implementation("io.kvision:kvision-routing-navigo-ng:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation("io.kvision:kvision-state-flow:$kvisionVersion")
        implementation("io.kvision:kvision-select-remote:$kvisionVersion")
        if(testSpaFramework) {
            implementation(files("../../kotlin-kvision-spa-framework/build/libs/kotlinKvisionSpaFramework-js-1.0.8.klib"))
            implementation(npm(file("../../kotlin-kvision-spa-framework/build/kotlin-kvision-spa-framework-resources")))
        } else {
            implementation("com.bittokazi.sonartype:kotlinKvisionSpaFramework-js:1.0.8")
            implementation(npm("kotlin-kvision-spa-framework-resources", "1.0.8"))
        }
    }
    sourceSets["jsTest"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
}

tasks.register<Copy>("copy") {
    dependsOn("clean", "zip")
    from("build/dist/libs")
    into("build/dist/js/productionExecutable/static")
}

tasks.register("buildFrontend") {
    dependsOn("copy")
}
