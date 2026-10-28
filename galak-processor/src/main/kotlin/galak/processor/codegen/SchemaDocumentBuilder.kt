package galak.processor.codegen

import com.apollographql.apollo.ast.*
import galak.processor.codegen.KotlinSymbols.AstArgument
import galak.processor.codegen.KotlinSymbols.AstBooleanValue
import galak.processor.codegen.KotlinSymbols.AstDirective
import galak.processor.codegen.KotlinSymbols.AstDirectiveDefinition
import galak.processor.codegen.KotlinSymbols.AstDocument
import galak.processor.codegen.KotlinSymbols.AstEnumValue
import galak.processor.codegen.KotlinSymbols.AstFieldDefinition
import galak.processor.codegen.KotlinSymbols.AstFloatValue
import galak.processor.codegen.KotlinSymbols.AstInputObjectTypeDefinition
import galak.processor.codegen.KotlinSymbols.AstInputValueDefinition
import galak.processor.codegen.KotlinSymbols.AstIntValue
import galak.processor.codegen.KotlinSymbols.AstInterfaceTypeDefinition
import galak.processor.codegen.KotlinSymbols.AstListValue
import galak.processor.codegen.KotlinSymbols.AstNamedType
import galak.processor.codegen.KotlinSymbols.AstNullValue
import galak.processor.codegen.KotlinSymbols.AstObjectField
import galak.processor.codegen.KotlinSymbols.AstObjectTypeDefinition
import galak.processor.codegen.KotlinSymbols.AstObjectValue
import galak.processor.codegen.KotlinSymbols.AstOperationTypeDefinition
import galak.processor.codegen.KotlinSymbols.AstSchemaDefinition
import galak.processor.codegen.KotlinSymbols.AstStringValue
import galak.processor.codegen.KotlinSymbols.AstUnionTypeDefinition
import galak.processor.sir.*
import galak.processor.sir.SirDefinition
import galak.processor.sir.SirEnumDefinition
import galak.processor.sir.SirEnumValueDefinition
import galak.processor.sir.SirErrorType
import galak.processor.sir.SirExecutionContextArgumentDefinition
import galak.processor.sir.SirFieldDefinition
import galak.processor.sir.SirInputValueDefinition
import galak.processor.sir.SirInputObjectDefinition
import galak.processor.sir.SirInterfaceDefinition
import galak.processor.sir.SirListType
import galak.processor.sir.SirNamedType
import galak.processor.sir.SirNonNullType
import galak.processor.sir.SirObjectDefinition
import galak.processor.sir.SirScalarDefinition
import galak.processor.sir.SirType
import galak.processor.sir.SirUnionDefinition
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.joinToCode
import com.squareup.kotlinpoet.withIndent

internal class SchemaDocumentBuilder(
  val context: KotlinExecutableSchemaContext,
  serviceName: String,
  val sirDefinitions: List<SirDefinition>,
) : CgFileBuilder {
  private val simpleName = "${serviceName.decapitalizeFirstLetter()}SchemaDocument"

  val schemaDocument: MemberName
    get() = MemberName(packageName = context.packageName, simpleName)

  override fun prepare() {}

  private fun propertySpec(): PropertySpec {

    return PropertySpec.builder(simpleName, AstDocument)
        .addModifiers(KModifier.INTERNAL)
        .initializer(
            buildCodeBlock {
              add("%T(\n", AstDocument)
              withIndent {
                add("")
                add("definitions = listOf(\n")
                withIndent {
                  add("%L,\n", sirDefinitions.schemaDefinitionCodeBlock())
                  sirDefinitions.forEach {
                    when (it) {
                      is SirScalarDefinition -> add("%L,\n", it.codeBlock())
                      is SirEnumDefinition -> add("%L,\n", it.codeBlock())
                      is SirInputObjectDefinition -> add("%L,\n", it.codeBlock())
                      is SirInterfaceDefinition -> add("%L,\n", it.codeBlock())
                      is SirObjectDefinition -> add("%L,\n", it.codeBlock())
                      is SirUnionDefinition -> add("%L,\n", it.codeBlock())
                      is SirDirectiveDefinition -> add("%L,\n", it.codeBlock())
                    }
                  }
                }
                add("),\n")
                add("sourceLocation = null,\n")
              }
              add(")\n")
            }
        )
        .build()
  }

  override fun build(): FileSpec {
    return FileSpec.builder(context.packageName, simpleName)
        .addProperty(propertySpec())
        .build()
  }
}

/**
 * ```
 * fun foo() {
 *   GQLSchemaDefinition(
 *       rootOperationTypeDefinitions = listOf(
 *           GQLOperationTypeDefinition(
 *               operationType = "",
 *               namedType = ""
 *           )
 *       )
 *   )
 * }
 * ``
 */
private fun List<SirDefinition>.schemaDefinitionCodeBlock(): CodeBlock {
  return buildCodeBlock {
    add("%T(\n", AstSchemaDefinition)
    withIndent {
      add("sourceLocation = null,\n")
      add("description = null,\n")
      add("directives = emptyList(),\n")
      add("rootOperationTypeDefinitions = listOf(\n")
      withIndent {
        filterIsInstance<SirObjectDefinition>().filter { it.operationType != null }.forEach {
          add("%T(\n", AstOperationTypeDefinition)
          withIndent {
            add("operationType = %S,\n", it.operationType)
            add("namedType = %S,\n", it.name)
          }
          add("),\n")
        }
      }
      add(")\n")
    }
    add(")")
  }
}

private fun SirInputObjectDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstInputObjectTypeDefinition, name = name, description = description, directives = directives) {
    add("inputFields = listOf(\n")
    indent()
    inputFields.forEach {
      add("%L,\n", it.codeBlock())
    }
    unindent()
    add(")")
  }
}

private fun SirUnionDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstUnionTypeDefinition, name = name, description = description, directives = directives) {
    add("memberTypes = listOf(")
    memberTypes.forEach {
      add("%T(null,·%S),·", AstNamedType, it)
    }
    add(")")
  }
}


private fun SirDirectiveDefinition.codeBlock(): CodeBlock {
  return buildCodeBlock {
    add("%T(\n", AstDirectiveDefinition)
    withIndent {
      add("sourceLocation·=·null,\n")
      add("description·=·%S,\n", description)
      add("name·=·%S,\n", name)
      add("arguments·=·listOf(\n")
      withIndent {
        argumentDefinitions.forEach {
          add("%L,\n", it.codeBlock())
        }
      }
      add("),\n")
      add("repeatable·=·%L,\n", repeatable.toString())
      add("locations·=·%L\n", locations.map { it.codeBlock() }.joinToCode(prefix = "listOf(", suffix = ")"))
    }
    add(")")
  }
}

private fun GQLDirectiveLocation.codeBlock(): CodeBlock {
  return CodeBlock.of("%T", ClassName("com.apollographql.apollo.ast", "GQLDirectiveLocation", name))
}

private fun SirObjectDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstObjectTypeDefinition, name = name, description = description, directives = directives) {
    add("implementsInterfaces = listOf(%L),\n", interfaces.map { CodeBlock.builder().add("%S", it).build() }.joinToCode(",·"))
    add("fields = listOf(\n")
    indent()
    fields.forEach {
      add("%L,\n", it.codeBlock())
    }
    unindent()
    add("),\n")
  }
}

private fun SirInterfaceDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstInterfaceTypeDefinition, name = name, description = description, directives = directives) {
    add("implementsInterfaces = listOf(%L),\n", interfaces.map { CodeBlock.builder().add("%S", it).build() }.joinToCode(",·"))
    add("fields = listOf(\n")
    indent()
    fields.forEach {
      add("%L,\n", it.codeBlock())
    }
    unindent()
    add("),\n")
  }
}

private fun SirFieldDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstFieldDefinition, name = name, description = description, directives = directives) {
    add("arguments = listOf(\n")
    indent()
    arguments.forEach {
      when (it) {
        is SirExecutionContextArgumentDefinition -> Unit
        is SirInputValueDefinition -> {
          add("%L,\n", it.codeBlock())
        }
      }
    }
    unindent()
    add("),\n")
    add("type = %L,\n", type.codeBlock())
  }
}

private fun SirInputValueDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = AstInputValueDefinition, name = name, directives = directives, description = description) {
    add("type = %L,\n", type.codeBlock())
    if (defaultValue != null) {
      add("defaultValue = %S.%M().getOrThrow(),\n", defaultValue, KotlinSymbols.AstParseAsGQLValue)
    } else {
      add("defaultValue = null,\n")
    }
  }
}

private fun SirType.codeBlock(): CodeBlock {
  return when (this) {
    SirErrorType -> CodeBlock.of("%T", ClassName("kotlin", "Nothing"))
    is SirListType -> CodeBlock.of("%T(type·=·%L)", KotlinSymbols.AstListType, type.codeBlock())
    is SirNamedType -> CodeBlock.of("%T(name·=·%S)", KotlinSymbols.AstNamedType, name)
    is SirNonNullType -> CodeBlock.of("%T(type·=·%L)", KotlinSymbols.AstNonNullType, type.codeBlock())
  }
}

private fun buildCommon(
    className: ClassName,
    name: String,
    description: String?,
    directives: List<SirDirective>,
    block: CodeBlock.Builder.() -> Unit = {},
): CodeBlock {
  return buildCodeBlock {
    add("%T(\n", className)
    indent()
    add("sourceLocation·=·null,\n")
    add("description·=·%S,\n", description)
    add("name·=·%S,\n", name)
    add("directives·=·%L,\n", directives.map { it.codeBlock() }.joinToCode(prefix = "listOf(", suffix = ")"))
    block()
    unindent()
    add(")")
  }
}

private fun SirDirective.codeBlock(): CodeBlock {
  return buildCodeBlock {
    add("%T(\n", AstDirective)
    withIndent {
      add("null,\n")
      add("%S,\n", name)
      add("listOf(\n")
      withIndent {
        arguments.forEach {
          add("%L,\n", it.codeBlock())
        }
      }
      add(")\n")
    }
    add(")\n")
  }
}

private fun SirArgument.codeBlock(): CodeBlock {
  return buildCodeBlock {
    add("%T(\n", AstArgument)
    withIndent {
      add("null,\n")
      add("%S,\n", name)
      add("%L,\n", value.codeBlock())
    }
    add(")\n")
  }
}

private fun GQLValue.codeBlock(): CodeBlock {
  return when (this) {
    is GQLBooleanValue -> CodeBlock.of("%T(null, %L)", AstBooleanValue, value.toString())
    is GQLEnumValue -> CodeBlock.of("%T(null, %S)", AstEnumValue, value)
    is GQLFloatValue -> CodeBlock.of("%T(null, %S)", AstFloatValue, value)
    is GQLIntValue -> CodeBlock.of("%T(null, %S)", AstIntValue, value)
    is GQLListValue -> CodeBlock.of("%T(null, %L)", AstListValue, values.map { it.codeBlock() }.joinToCode(prefix = "listOf(", suffix = ")"))
    is GQLNullValue -> CodeBlock.of("%T(null)", AstNullValue)
    is GQLObjectValue -> CodeBlock.of("%T(null, %L)", AstObjectValue, fields.map { it.codeBlock() }.joinToCode(prefix = "listOf(", suffix = ")"))
    is GQLStringValue -> CodeBlock.of("%T(null, %S)", AstStringValue, value)
    is GQLVariableValue -> error("Variable cannot be used in const position")
  }
}

private fun GQLObjectField.codeBlock(): CodeBlock {
  return CodeBlock.of("%T(null, %S, %L)", AstObjectField, name, value.codeBlock())
}

private fun SirEnumDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = KotlinSymbols.AstEnumTypeDefinition, name = name, description = description, directives = directives) {
    add("enumValues = listOf(\n")
    indent()
    add("%L", values.map { it.codeBlock() }.joinToCode(",\n", suffix = ",\n"))
    unindent()
    add("),\n")
  }
}

internal fun SirEnumValueDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = KotlinSymbols.AstEnumValueDefinition, name = name, description = description, directives = directives)
}

private fun SirScalarDefinition.codeBlock(): CodeBlock {
  return buildCommon(className = KotlinSymbols.AstScalarTypeDefinition, name = name, description = description, directives = directives)
}