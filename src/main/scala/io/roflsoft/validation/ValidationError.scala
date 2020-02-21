package io.roflsoft.validation

trait ValidationError extends Exception {
  def errorMessage: String
}
