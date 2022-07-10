package com.github.skibish.filterexample

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

class FilterListenerTests {
  companion object {
    @JvmStatic
    fun testCasesPositive(): List<Arguments> = listOf(
      Arguments.arguments("price eq 10.00", "{ \"price\": { \$eq: 10.0 } }"),
      Arguments.arguments("price lt .5", "{ \"price\": { \$lt: 0.5 } }"),
      Arguments.arguments("name eq 'Milk'", "{ \"name\": { \$eq: \"Milk\" } }"),
      Arguments.arguments("name ne 'Milk'", "{ \"name\": { \$ne: \"Milk\" } }"),
      Arguments.arguments(
        "name eq 'Milk' and price lt 2.55",
        "{ \$and: [ { \"name\": { \$eq: \"Milk\" } }, { \"price\": { \$lt: 2.55 } } ] }"
      ),
      Arguments.arguments(
        "name eq 'Milk' or price lt 2.55",
        "{ \$or: [ { \"name\": { \$eq: \"Milk\" } }, { \"price\": { \$lt: 2.55 } } ] }"
      ),
      Arguments.arguments(
        "(name eq 'Milk' or name eq 'Eggs') and price lt 2.55",
        "{ \$and: [ { \$or: [ { \"name\": { \$eq: \"Milk\" } }, { \"name\": { \$eq: \"Eggs\" } } ] }, { \"price\": { \$lt: 2.55 } } ] }"
      ),
      Arguments.arguments("some.attribute ne 'let''s rock'", "{ \"some.attribute\": { \$ne: \"let's rock\" } }"),
      Arguments.arguments("some.attribute ne 'let''s rock'", "{ \"some.attribute\": { \$ne: \"let's rock\" } }"),
      Arguments.arguments(
        "price le 3.5 or price gt 200",
        "{ \$or: [ { \"price\": { \$lte: 3.5 } }, { \"price\": { \$gt: 200.0 } } ] }"
      ),
      Arguments.arguments("not price le 3.5", "{ \"price\": { \$not: { \$lte: 3.5 } } }"),
    )

    @JvmStatic
    fun testCasesNegative(): List<Arguments> = listOf(
      Arguments.arguments("price....avs eq 10.00"),
      Arguments.arguments("not (a eq true or b eq true)"),
      Arguments.arguments("something completely irrelevant"),
    )
  }

  @ParameterizedTest(name = "#{index}: {0}")
  @MethodSource("testCasesPositive")
  fun `validate that parser constructs correct queries`(filter: String, expected: String) {
    val filterListener = FilterListener()
    val result = filterListener.generateQueryString(filter)
    assertEquals(expected, result)
  }

  @ParameterizedTest(name = "#{index}: {0}")
  @MethodSource("testCasesNegative")
  fun `validate that parser throws exception on not invalid queries`(filter: String) {
    val filterListener = FilterListener()
    assertThrows<Exception> {
      filterListener.generateQueryString(filter)
    }
  }
}
