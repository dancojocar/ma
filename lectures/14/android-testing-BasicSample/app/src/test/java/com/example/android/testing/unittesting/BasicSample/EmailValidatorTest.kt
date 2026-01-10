package com.example.android.testing.unittesting.BasicSample

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for the EmailValidator logic.
 *
 * This test class demonstrates:
 * - Pure unit testing without Android dependencies
 * - Testing edge cases (null, empty, invalid formats)
 * - Using Google Truth assertions for readable tests
 */
class EmailValidatorTest {

  @Test
  fun `valid email with simple format returns true`() {
    assertThat(EmailValidator.isValidEmail("name@email.com")).isTrue()
  }

  @Test
  fun `valid email with subdomain returns true`() {
    assertThat(EmailValidator.isValidEmail("name@email.co.uk")).isTrue()
  }

  @Test
  fun `valid email with plus sign returns true`() {
    assertThat(EmailValidator.isValidEmail("name+tag@email.com")).isTrue()
  }

  @Test
  fun `valid email with dots in username returns true`() {
    assertThat(EmailValidator.isValidEmail("first.last@email.com")).isTrue()
  }

  @Test
  fun `invalid email without TLD returns false`() {
    assertThat(EmailValidator.isValidEmail("name@email")).isFalse()
  }

  @Test
  fun `invalid email with double dot returns false`() {
    assertThat(EmailValidator.isValidEmail("name@email..com")).isFalse()
  }

  @Test
  fun `invalid email without username returns false`() {
    assertThat(EmailValidator.isValidEmail("@email.com")).isFalse()
  }

  @Test
  fun `invalid email without at symbol returns false`() {
    assertThat(EmailValidator.isValidEmail("nameemail.com")).isFalse()
  }

  @Test
  fun `empty string returns false`() {
    assertThat(EmailValidator.isValidEmail("")).isFalse()
  }

  @Test
  fun `null email returns false`() {
    assertThat(EmailValidator.isValidEmail(null)).isFalse()
  }

  @Test
  fun `email with spaces returns false`() {
    assertThat(EmailValidator.isValidEmail("name @email.com")).isFalse()
  }

  @Test
  fun `email with special characters returns false`() {
    assertThat(EmailValidator.isValidEmail("name!@email.com")).isFalse()
  }
}
