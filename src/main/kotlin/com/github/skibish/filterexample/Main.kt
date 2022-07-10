package com.github.skibish.filterexample

fun main() {
  try {
    print("input filter: ")
    val filter = readln()
    val filterListener = FilterListener()
    println("output:")
    println(filterListener.generateQueryString(filter))
  } catch (e: Exception) {
    println("ERROR: ${e.message}")
  }
}
