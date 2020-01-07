package io.roflsoft.http

import akka.http.scaladsl.model.StatusCodes

case class ErrorPayload(status: StatusCodes.ClientError, message: String)
