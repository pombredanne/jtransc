/*
 * Copyright 2016 Carlos Ballesteros Velasco
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

package com.jtransc.text

import com.jtransc.error.InvalidOperationException
import java.io.Reader


class TokenReader<T>(val list: List<T>) {
	var position = 0
	val size = list.size

	val hasMore: Boolean get() = position < size

	fun peek(): T = list[position]
	fun read(): T {
		val result = peek()
		skip()
		return result
	}

	fun tryRead(vararg expected: T): Boolean {
		val value = peek()
		if (value in expected) {
			skip(1)
			return true
		} else {
			return false
		}
	}

	fun expect(expected: T): T {
		val value = read()
		if (value != expected) {
			throw InvalidOperationException("Expected $expected but found $value")
		}
		return value
	}

	fun expect(expected: Set<T>): T {
		val value = read()
		if (value !in expected) throw InvalidOperationException("Expected $expected but found $value")
		return value
	}

	fun unread() {
		position--
	}

	fun skip(count: Int = 1): TokenReader<T> {
		position += count
		return this
	}
}

fun Char.isLetterOrUnderscore(): Boolean = this.isLetter() || this == '_' || this == '$'
fun Char.isLetterDigitOrUnderscore(): Boolean = this.isLetterOrDigit() || this == '_' || this == '$'
fun Char.isLetterOrDigitOrDollar(): Boolean = this.isLetterOrDigit() || this == '$'

// @TODO: Make a proper table
// 0x20, 0x7e
//return this.isLetterDigitOrUnderscore() || this == '.' || this == '/' || this == '\'' || this == '"' || this == '(' || this == ')' || this == '[' || this == ']' || this == '+' || this == '-' || this == '*' || this == '/'
fun Char.isPrintable(): Boolean = when (this) {
	in '\u0020'..'\u007e', in '\u00a1'..'\u00ff' -> true
	else -> false
}

fun GenericTokenize(sr: Reader): List<String> {
	val symbols = setOf("...")

	val tokens = arrayListOf<String>()
	while (sr.hasMore) {
		val char = sr.peekch()
		if (char.isLetterOrUnderscore()) {
			var token = ""
			while (sr.hasMore) {
				val c = sr.peekch()
				if (!c.isLetterDigitOrUnderscore()) break
				token += c
				sr.skip(1)
			}
			tokens.add(token)
		} else if (char.isWhitespace()) {
			sr.skip(1)
		} else if (char.isDigit()) {
			var token = ""
			while (sr.hasMore) {
				val c = sr.peekch()
				if (!c.isLetterOrDigit() && c != '.') break
				token += c
				sr.skip(1)
			}
			tokens.add(token)
		} else if (char == '"') {
			var token = "\""
			sr.skip(1)
			while (sr.hasMore) {
				val c = sr.peekch()
				token += c
				sr.skip(1)
				if (c == '"') break
			}
			tokens.add(token)
		} else {
			//val peek = sr.peek(3)
			if (sr.peek(3) in symbols) {
				tokens.add(sr.read(3))
			} else if (sr.peek(2) in symbols) {
				tokens.add(sr.read(2))
			} else {
				tokens.add("$char")
				sr.skip(1)
			}
		}
	}
	return tokens.toList()
}