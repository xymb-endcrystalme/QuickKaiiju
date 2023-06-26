pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}

rootProject.name = "quickkaiiju"

include("QuickKaiiju-API", "QuickKaiiju-Server")
