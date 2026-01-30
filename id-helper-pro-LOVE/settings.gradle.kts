pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } } 
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } } 
rootProject.name = "id-helper-pro-LOVE" 
include(":app") 
