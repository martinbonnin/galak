package galak.gradle

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

abstract class GalakService {
  abstract val packageName: Property<String>
  abstract val schemaFile: RegularFileProperty
}
