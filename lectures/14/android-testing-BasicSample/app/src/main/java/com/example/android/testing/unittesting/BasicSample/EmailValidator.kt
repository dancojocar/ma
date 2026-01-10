package com.example.android.testing.unittesting.BasicSample

import java.util.regex.Pattern

/**
 * An Email format validator.
 *
 * This is a simple utility class that demonstrates:
 * - Pure Kotlin/Java unit testing (no Android dependencies)
 * - Static validation methods that are easy to test
 */
object EmailValidator {

  /**
   * Email validation pattern.
   * Matches standard email format: user@domain.tld
   */
  private val EMAIL_PATTERN: Pattern = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
        "@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
  )

  /**
   * Validates if the given input is a valid email address.
   *
   * @param email The email to validate.
   * @return `true` if the input is a valid email. `false` otherwise.
   */
  fun isValidEmail(email: CharSequence?): Boolean {
    return email != null && EMAIL_PATTERN.matcher(email).matches()
  }
}
