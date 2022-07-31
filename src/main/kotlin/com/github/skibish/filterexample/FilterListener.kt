package com.github.skibish.filterexample

import com.github.skibish.filterexample.antlr4.FilterBaseListener
import com.github.skibish.filterexample.antlr4.FilterLexer
import com.github.skibish.filterexample.antlr4.FilterParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

class FilterListener : FilterBaseListener() {

  private var result: List<String> = listOf()

  private enum class EnteredNode {
    NOT,
    AND,
    OR
  }

  private var enteredNodes: List<EnteredNode> = listOf()

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
    if (ctx.NOT() != null) enteredNodes = enteredNodes.plus(EnteredNode.NOT)
    if (ctx.AND() != null) enteredNodes = enteredNodes.plus(EnteredNode.AND)
    if (ctx.OR() != null) enteredNodes = enteredNodes.plus(EnteredNode.OR)
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

    val not = isNot()

    val q = when (operator) {
      "gt" -> if (not) comparisonQueryOperator(property, "\$lte", parsed) else comparisonQueryOperator(property, "\$$operator", parsed)
      "lt" -> if (not) comparisonQueryOperator(property, "\$gte", parsed) else comparisonQueryOperator(property, "\$$operator", parsed)
      "eq" -> if (not) comparisonQueryOperator(property, "\$ne", parsed) else comparisonQueryOperator(property, "\$$operator", parsed)
      "ne" -> if (not) comparisonQueryOperator(property, "\$eq", parsed) else comparisonQueryOperator(property, "\$$operator", parsed)
      "ge" -> if (not) comparisonQueryOperator(property, "\$lt", parsed) else comparisonQueryOperator(property, "\$gte", parsed)
      "le" -> if (not) comparisonQueryOperator(property, "\$gt", parsed) else comparisonQueryOperator(property, "\$lte", parsed)
      else -> throw Exception("operator $operator not implemented")
    }

    result = result.plus(q)
  }

  private fun isNot(checkAgainst: EnteredNode = EnteredNode.NOT): Boolean {
    if (enteredNodes.isEmpty()) {
      return false
    }

    if (checkAgainst == EnteredNode.AND && enteredNodes.last() == EnteredNode.AND) {
      enteredNodes = enteredNodes.dropLast(1)
    }

    if (checkAgainst == EnteredNode.OR && enteredNodes.last() == EnteredNode.OR) {
      enteredNodes = enteredNodes.dropLast(1)
    }

    if (enteredNodes.isEmpty()) {
      return false
    }

    if (enteredNodes.last() == EnteredNode.NOT) {
      enteredNodes = enteredNodes.dropLast(1)
      return true
    }

    return false
  }

  private fun leftAndRight(): Pair<String, String> {
    val right = result.last()
    result = result.dropLast(1)

    val left = result.last()
    result = result.dropLast(1)

    return left to right
  }

  private fun buildAnd() {
    val (left, right) = leftAndRight()

    if (isNot(EnteredNode.AND)) {
      result = result.plus("{ \$nor: [ { \$and: [ $left, $right ] } ] }")
      return
    }

    result = result.plus("{ \$and: [ $left, $right ] }")
  }

  private fun buildOr() {
    val (left, right) = leftAndRight()

    if (isNot(EnteredNode.OR)) {
      result = result.plus("{ \$nor: [ $left, $right ] }")
      return
    }

    result = result.plus("{ \$or: [ $left, $right ] }")
  }
}
