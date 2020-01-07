package io.roflsoft.sqs

import akka.Done
import akka.stream.Materializer
import akka.stream.alpakka.sqs.scaladsl.SqsPublishSink
import akka.stream.scaladsl.Source
import monix.eval.Task
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

abstract class SqsTopic(val queueURL: String)(implicit materializer: Materializer, sqsAsyncClient: SqsAsyncClient) {

  /**
  * Sends single message to a queue with a json string as the request body
   */
  def sendSingle(json: String): Task[Done] = {
    Task.deferFuture(
      Source
        .single(SendMessageRequest.builder().messageBody(json).build())
        .runWith(SqsPublishSink.messageSink(queueURL))
    )
  }
}
