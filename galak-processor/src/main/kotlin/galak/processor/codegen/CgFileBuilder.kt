package galak.processor.codegen

import com.squareup.kotlinpoet.FileSpec

internal interface CgFileBuilder {
  fun prepare()
  fun build(): FileSpec
}