/*
 * Copyright (C) 2023 Cash App
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.paraphrase.plugin

import app.cash.paraphrase.plugin.model.MergedResource
import app.cash.paraphrase.plugin.model.MergedResource.Argument
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.time.Duration

/**
 * Writes the given tokenized resources to a Kotlin source file.
 */
internal fun writeResources(
  packageName: String,
  mergedResources: List<MergedResource>,
): FileSpec {
  val packageStringsType = ClassName(packageName = packageName, "R", "string")
  val maxVisibility = mergedResources.maxOf { it.visibility }
  return FileSpec.builder(packageName = packageName, fileName = "FormattedResources")
    .addFileComment(
      """
        This code was generated by the Paraphrase Gradle plugin.
        Do not edit this file directly. Instead, edit the string resources in the source file.
      """.trimIndent(),
    )
    .addImport(packageName = packageName, "R")
    .addType(
      TypeSpec.objectBuilder("FormattedResources")
        .apply {
          mergedResources.forEach { mergedResource ->
            addFunction(mergedResource.toFunSpec(packageStringsType))
          }
        }
        .addModifiers(maxVisibility.toKModifier())
        .build(),
    )
    .build()
}

private fun MergedResource.toFunSpec(packageStringsType: TypeName): FunSpec {
  return FunSpec.builder(name.value)
    .apply { if (description != null) addKdoc(description) }
    .apply { arguments.forEach { addParameter(it.toParameterSpec()) } }
    .returns(Types.FormattedResource)
    .apply {
      if (hasContiguousNumberedTokens) {
        addCode(
          buildCodeBlock {
            add("val arguments = arrayOf(⇥\n")
            for (argument in arguments) {
              addStatement("%L,", argument.toParameterCodeBlock())
            }
            add("⇤)\n")
          },
        )
      } else {
        addStatement("val arguments = %T(%L)", Types.ArrayMap.parameterizedBy(STRING, ANY), arguments.size)
        for (argument in arguments) {
          addCode("arguments.put(\n⇥")
          addCode("%S,\n", argument.key)
          addCode("%L,\n", argument.toParameterCodeBlock())
          addCode("⇤)\n")
        }
      }
    }
    .addCode(
      buildCodeBlock {
        add("return %T(⇥\n", Types.FormattedResource)
        addStatement("id = %T.%L,", packageStringsType, name.value)
        addStatement("arguments = arguments,")
        add("⇤)\n")
      },
    )
    .addModifiers(visibility.toKModifier())
    .build()
}

private fun Argument.toParameterSpec(): ParameterSpec =
  ParameterSpec(
    name = name,
    type = when (type) {
      Nothing::class -> NOTHING.copy(nullable = true)
      else -> type.asClassName()
    },
  )

private fun Argument.toParameterCodeBlock(): CodeBlock =
  when (type) {
    Duration::class -> CodeBlock.of("%L.inWholeSeconds", name)
    LocalDate::class -> buildCodeBlock {
      addCalendarInstance {
        addStatement("set(%1L.year, %1L.monthValue·-·1, %1L.dayOfMonth)", name)
      }
    }

    LocalTime::class -> buildCodeBlock {
      addCalendarInstance {
        addStatement("set(%T.HOUR_OF_DAY, %L.hour)", Types.Calendar, name)
        addStatement("set(%T.MINUTE, %L.minute)", Types.Calendar, name)
        addStatement("set(%T.SECOND, %L.second)", Types.Calendar, name)
        addStatement("set(%T.MILLISECOND, %L.nano·/·1_000_000)", Types.Calendar, name)
      }
    }

    LocalDateTime::class -> buildCodeBlock {
      addCalendarInstance {
        addDateTimeSetStatements(name)
      }
    }

    // `Nothing` arg must be null, but passing null to the formatter replaces the whole format with
    //  "null". Passing an `Int` allows the formatter to function as expected.
    Nothing::class -> CodeBlock.of("-1")

    OffsetTime::class -> buildCodeBlock {
      addCalendarInstance(timeZoneId = "\"GMT\${%L.offset.id}\"", name) {
        addStatement("set(%T.HOUR_OF_DAY, %L.hour)", Types.Calendar, name)
        addStatement("set(%T.MINUTE, %L.minute)", Types.Calendar, name)
        addStatement("set(%T.SECOND, %L.second)", Types.Calendar, name)
        addStatement("set(%T.MILLISECOND, %L.nano·/·1_000_000)", Types.Calendar, name)
      }
    }

    OffsetDateTime::class -> buildCodeBlock {
      addCalendarInstance(timeZoneId = "\"GMT\${%L.offset.id}\"", name) {
        addDateTimeSetStatements(name)
      }
    }

    ZonedDateTime::class -> buildCodeBlock {
      addCalendarInstance(timeZoneId = "%L.zone.id", name) {
        addDateTimeSetStatements(name)
      }
    }

    ZoneOffset::class -> buildCodeBlock {
      addCalendarInstance(timeZoneId = "\"GMT\${%L.id}\"", name)
    }

    else -> CodeBlock.of("%L", name)
  }

private fun CodeBlock.Builder.addCalendarInstance(
  timeZoneId: String? = null,
  vararg timeZoneIdArgs: Any? = emptyArray(),
  applyBlock: (() -> Unit)? = null,
) {
  val timeZoneReference = if (timeZoneId == null) "GMT_ZONE" else "getTimeZone($timeZoneId)"
  add("%T.getInstance(\n⇥", Types.Calendar)
  addStatement("%T.$timeZoneReference,", Types.TimeZone, *timeZoneIdArgs)
  addStatement("%T.Builder().setExtension('u', \"ca-iso8601\").build(),", Types.ULocale)
  add("⇤)")

  if (applyBlock != null) {
    add(".apply·{\n⇥")
    applyBlock.invoke()
    add("⇤}")
  }
}

private fun CodeBlock.Builder.addDateTimeSetStatements(dateTimeArgName: String) {
  add("set(\n⇥")
  addStatement("%L.year,", dateTimeArgName)
  addStatement("%L.monthValue·-·1,", dateTimeArgName)
  addStatement("%L.dayOfMonth,", dateTimeArgName)
  addStatement("%L.hour,", dateTimeArgName)
  addStatement("%L.minute,", dateTimeArgName)
  addStatement("%L.second,", dateTimeArgName)
  add("⇤)\n")
  addStatement("set(%T.MILLISECOND, %L.nano·/·1_000_000)", Types.Calendar, dateTimeArgName)
}

private fun MergedResource.Visibility.toKModifier(): KModifier {
  return when (this) {
    MergedResource.Visibility.Public -> KModifier.PUBLIC
    MergedResource.Visibility.Private -> KModifier.INTERNAL
  }
}

private object Types {
  val ArrayMap = ClassName("androidx.collection", "ArrayMap")
  val Calendar = ClassName("android.icu.util", "Calendar")
  val FormattedResource = ClassName("app.cash.paraphrase", "FormattedResource")
  val TimeZone = ClassName("android.icu.util", "TimeZone")
  val ULocale = ClassName("android.icu.util", "ULocale")
}
