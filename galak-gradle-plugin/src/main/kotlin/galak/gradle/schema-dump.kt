package galak.gradle

import galak.gradle.task.registerGalakCheckSchemaTask
import galak.gradle.task.registerGalakDumpSchemaTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty

private fun Project.defaultSchemaDump(): RegularFile {
  return layout.projectDirectory.file("graphql/schema.graphqls")
}

fun Project.enableSchemaDump(kspSchemaPath: String, kspTaskName: String, schemaDump: RegularFileProperty) {
  val dumpSchema = registerGalakDumpSchemaTask(from = objects.fileProperty().apply { fileValue(file(kspSchemaPath)) }, to = schemaDump.orElse(defaultSchemaDump()))
  dumpSchema.configure {
    it.dependsOn(kspTaskName)
  }
  val checkSchema = registerGalakCheckSchemaTask(
    existing = schemaDump.orElse(defaultSchemaDump()), new = objects.fileProperty().apply { fileValue(file(kspSchemaPath)) })
  checkSchema.configure {
    it.dependsOn(kspTaskName)
  }
  tasks.named("check") {
    it.dependsOn(checkSchema)
  }

  tasks.configureEach { maybeKsp ->
    if (maybeKsp.name == kspTaskName) {
      /**
       * XXX: eager configuration on dumpSchema and checkSchema but
       * without this, configureEach above doesn't work 🤷
       */
      dumpSchema.get().dependsOn(maybeKsp)
      checkSchema.get().dependsOn(maybeKsp)
    }
  }
}