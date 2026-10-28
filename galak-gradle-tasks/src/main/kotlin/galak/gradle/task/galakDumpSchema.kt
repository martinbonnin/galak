package galak.gradle.task

import gratatouille.tasks.GInputFile
import gratatouille.tasks.GManuallyWired
import gratatouille.tasks.GOutputFile
import gratatouille.tasks.GTask

internal const val galakDumpSchema = "galakDumpSchema"

@GTask
fun galakDumpSchema(
  from: GInputFile,
  @GManuallyWired
  to: GOutputFile
) {
  from.copyTo(to, overwrite = true)
}