package io.roflsoft.validation

import cats.data._

trait FormValidator[Request] {
  type ValidationResult[A] = ValidatedNec[ValidationError, A]

  def validateForm(request: Request): ValidationResult[Request]
}
