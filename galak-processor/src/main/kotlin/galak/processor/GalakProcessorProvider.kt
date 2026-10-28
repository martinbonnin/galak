package galak.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class GalakProcessorProvider : SymbolProcessorProvider {
  override fun create(
      environment: SymbolProcessorEnvironment,
  ): SymbolProcessor {
    return GalakProcessor(
        environment.codeGenerator,
        environment.logger,
        serviceName = environment.options.get("galakService")
            ?: error("Galak: serviceName is required:\nksp {\n  arg(\"galakService\", \"service\")\n}"),
        packageName = environment.options.get("galakPackageName")
            ?: error("Galak: packageName is required:\nksp {\n  arg(\"galakPackageName\", \"package\")\n}"),
    )
  }
}

