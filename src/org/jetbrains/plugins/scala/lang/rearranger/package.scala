package org.jetbrains.plugins.scala
package lang

import scala.collection.immutable
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokenType
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens
import com.intellij.psi.codeStyle.arrangement.std.ArrangementSettingsToken

import com.intellij.psi.codeStyle.arrangement.std.StdArrangementSettingsToken
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.EntryType._
import com.intellij.psi.codeStyle.arrangement.std.StdArrangementTokens.Modifier._

/**
 * @author Roman Shein
 * Date: 09.07.13
 */
package object rearranger {

  val SCALA_GETTERS_AND_SETTERS_ID = "SCALA_KEEP_SCALA_GETTERS_SETTERS_TOGETHER"
  val SCALA_GETTERS_AND_SETTERS_UI = "Keep_scala-style_getters_and_setters_together"
  val SCALA_GETTERS_AND_SETTERS: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(SCALA_GETTERS_AND_SETTERS_ID, SCALA_GETTERS_AND_SETTERS_UI, StdArrangementTokenType.GROUPING)
  val JAVA_GETTERS_AND_SETTERS_ID = "SCALA_KEEP_JAVA_GETTERS_SETTERS_TOGETHER"
  val JAVA_GETTERS_AND_SETTERS_UI = "Keep_java-style_getters_and_setters_together"
  val JAVA_GETTERS_AND_SETTERS: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(JAVA_GETTERS_AND_SETTERS_ID, JAVA_GETTERS_AND_SETTERS_UI, StdArrangementTokenType.GROUPING)
  val SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS_ID = "SCALA_SPLIT_BY_EXPRESSIONS"
  val SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS_UI = "Split into unarrangeable blocks by expressions"
  val SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS_ID, SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS_UI, StdArrangementTokenType.GROUPING) //TODO: use name from bundle

  val scalaGroupingRules = immutable.HashMap(SCALA_GETTERS_AND_SETTERS.getId -> SCALA_GETTERS_AND_SETTERS,
    JAVA_GETTERS_AND_SETTERS.getId -> JAVA_GETTERS_AND_SETTERS,
    SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS.getId -> SPLIT_INTO_UNARRANGEABLE_BLOCKS_BY_EXPRESSIONS)

  //access modifiers
  val scalaAccessModifiers = immutable.HashMap(PRIVATE.getId -> PRIVATE, PUBLIC.getId -> PUBLIC,
    PROTECTED.getId -> PROTECTED)

  //other modifiers
  val SEALED_ID = "SCALA_SEALED"
  val SEALED_UI = "sealed"
  val SEALED: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(SEALED_ID, SEALED_UI, StdArrangementTokenType.MODIFIER)
  val IMPLICIT_ID = "SCALA_IMPLICIT"
  val IMPLICIT_UI = "implicit"
  val IMPLICIT: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(IMPLICIT_ID, IMPLICIT_UI, StdArrangementTokenType.MODIFIER)
  val CASE_ID = "SCALA_CASE"
  val CASE_UI = "case"
  val CASE: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(CASE_ID, CASE_UI, StdArrangementTokenType.MODIFIER)
  val OVERRIDE_ID = "SCALA_OVERRIDE"
  val OVERRIDE_UI = "override"
  val OVERRIDE: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(OVERRIDE_ID, OVERRIDE_UI, StdArrangementTokenType.MODIFIER)
  val LAZY_ID = "SCALA_LAZY"
  val LAZY_UI = "lazy"
  val LAZY: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(LAZY_ID, LAZY_UI, StdArrangementTokenType.MODIFIER)
  val scalaOtherModifiers = immutable.HashMap(SEALED_ID -> SEALED, IMPLICIT_ID -> IMPLICIT,
    ABSTRACT.getId -> ABSTRACT, CASE_ID -> CASE, FINAL.getId -> FINAL, OVERRIDE_ID -> OVERRIDE, LAZY_ID -> LAZY)

  //types
  val TYPE_ID = "SCALA_TYPE"
  val TYPE_UI = "type"
  val TYPE: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(TYPE_ID, TYPE_UI, StdArrangementTokenType.ENTRY_TYPE)
  val FUNCTION_ID = "SCALA_FUNCTION"
  val FUNCTION_UI = "function"
  val FUNCTION: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(FUNCTION_ID, FUNCTION_UI, StdArrangementTokenType.ENTRY_TYPE)
  val VAL_ID = "SCALA_VAL"
  val VAL_UI = "val"
  val VAL: ArrangementSettingsToken = StdArrangementSettingsToken.token(VAL_ID, VAL_UI, StdArrangementTokenType.ENTRY_TYPE)
  val MACRO_ID = "SCALA_MACRO"
  val MACRO_UI = "macro"
  val MACRO: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(MACRO_ID, MACRO_UI, StdArrangementTokenType.ENTRY_TYPE)
  val OBJECT_ID = "SCALA_OBJECT"
  val OBJECT_UI = "object"
  val OBJECT: ArrangementSettingsToken =
    StdArrangementSettingsToken.token(OBJECT_ID, OBJECT_UI, StdArrangementTokenType.ENTRY_TYPE)
  //this is a special token that is not used in arrangement GUI and always has canBeMatched = false
  val UNSEPARABLE_RANGE_ID = "SCALA_UNSEPARABLE_RANGE"
  val UNSEPARABLE_RANGE: ArrangementSettingsToken = StdArrangementSettingsToken.tokenById(UNSEPARABLE_RANGE_ID, StdArrangementTokenType.ENTRY_TYPE)

  //maps and sets of tokens
  val scalaTypes = immutable.HashMap(TYPE_ID -> TYPE, FUNCTION_ID -> FUNCTION, CLASS.getId -> CLASS,
    VAL_ID -> VAL, VAR.getId -> VAR, TRAIT.getId -> TRAIT, MACRO_ID -> MACRO, CONSTRUCTOR.getId -> CONSTRUCTOR,
    OBJECT_ID -> OBJECT)
  val scalaTypesValues = scalaTypes.toSet.map((x: tokensType) => x._2)

  val scalaAccessModifiersValues = scalaAccessModifiers.toSet.map((x: tokensType) => x._2)

  val scalaModifiers = scalaAccessModifiersValues ++ scalaOtherModifiers.toSet.map((x: tokensType) => x._2)

  val scalaArrangementTokensByName = scalaAccessModifiers ++ scalaOtherModifiers ++ scalaTypes ++ scalaGroupingRules

  private type tokensType = Pair[String, ArrangementSettingsToken]

  val scalaArrangementSettingsTokens = scalaModifiers ++ scalaTypesValues

  val supportedOrders = immutable.HashSet(StdArrangementTokens.Order.BY_NAME, StdArrangementTokens.Order.KEEP)

  val commonModifiers = scalaAccessModifiersValues + FINAL //TODO: determine if final is common

  val tokensForType = immutable.HashMap(TYPE -> (commonModifiers + OVERRIDE), FUNCTION -> (commonModifiers +
          OVERRIDE + IMPLICIT), CLASS -> (commonModifiers + ABSTRACT + SEALED), TRAIT -> (commonModifiers +
          ABSTRACT + SEALED), VAL -> (commonModifiers + OVERRIDE + LAZY + ABSTRACT),
          VAR -> (commonModifiers + OVERRIDE), MACRO -> (commonModifiers + OVERRIDE),
          CONSTRUCTOR -> scalaAccessModifiersValues, OBJECT -> commonModifiers)

  def getTokenByName(name: String) = {
    scalaArrangementTokensByName.get(name.toUpperCase)
  }

}
