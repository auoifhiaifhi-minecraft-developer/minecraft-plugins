// Configuração compartilhada pelos três plugins.
subprojects {
    apply(plugin = "java")

    group = "dev.auoifhiaifhi"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        "compileOnly"("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    // Troca ${version} no plugin.yml pela versão do projeto.
    tasks.withType<ProcessResources> {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
