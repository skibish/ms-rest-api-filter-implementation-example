package com.github.skibish.filterexample

import com.github.skibish.filterexample.antlr4.FilterBaseListener
import com.github.skibish.filterexample.antlr4.FilterLexer
import com.github.skibish.filterexample.antlr4.FilterParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

class FilterListener : FilterBaseListener() {

  private var result: MutableList<String> = mutableListOf()

  private var enteredNot: Boolean = false

  fun generateQueryString(filter: String): String {
    // lexer
    val chars = CharStreams.fromString(filter)
    val lexer = FilterLexer(chars)
    lexer.removeErrorListeners()

    // parser
    val tokens = CommonTokenStream(lexer)
    val parser = FilterParser(tokens)
    parser.removeErrorListeners()
    parser.addErrorListener(ErrorListener())

    // execute
    ParseTreeWalker().walk(this, parser.filter())
    return result.last()
  }

  override fun enterExpr(ctx: FilterParser.ExprContext) {
    if (ctx.NOT() != null) enteredNot = true
  }

  override fun exitExpr(ctx: FilterParser.ExprContext) {
    when {
      ctx.COMPARISON() != null -> buildComparison(
        property = ctx.PROPERTY().text,
        operator = ctx.COMPARISON().text,
        value = ctx.VALUE().text
      )
      ctx.AND() != null -> buildAnd()
      ctx.OR() != null -> buildOr()
    }
  }

  private fun comparisonQueryOperator(property: String, operator: String, value: String): String {
    if (enteredNot) {
      enteredNot = false
      return "{ \"$property\": { \$not: { $operator: $value } } }"
    }

    return "{ \"$property\": { $operator: $value } }"
  }

  private fun buildComparison(property: String, operator: String, value: String) {
    val parsed = when {
      value == "true" || value == "false" -> value
      value.toDoubleOrNull() != null -> "${value.toDouble()}"
      else ->
        "\"${
        value
          .drop(1)
          .dropLast(1)
          .split("''")
          .joinToString("'")
        }\""
    }

    val q = when (operator) {
      "gt", "lt", "eq", "ne" -> comparisonQueryOperator(property, "\$$operator", parsed)
      "ge" -> comparisonQueryOperator(property, "\$gte", parsed)
      "le" -> comparisonQueryOperator(property, "\$lte", parsed)
      else -> throw Exception("operator $operator not implemented")
    }

    result.add(q)
  }

  private fun buildAnd() {
    val (left, right) = result
    result = mutableListOf()

    result.add("{ \$and: [ $left, $right ] }")
  }

  private fun buildOr() {
    val (left, right) = result
    result = mutableListOf()

    result.add("{ \$or: [ $left, $right ] }")
  }
}
