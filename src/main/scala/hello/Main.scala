package hello

import cats.effect.IOApp
import cats.effect.{ExitCode, IO}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    import cats.effect._
    import cats.implicits._
    import fs2.kafka._
    import scala.concurrent.duration._

    val consumerSettings =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    val stream = KafkaConsumer
      .stream(consumerSettings)
      .evalTap(_.subscribeTo("test-topic"))
      .flatMap(_.stream)
      .debug(x =>
        s"Offset before: ${x.offset.offsetAndMetadata.offset()} with key ${x.record.key}"
      )
      .chunkN(1)
      .map(fs2.Stream.chunk(_).covary[IO].evalMap { x =>
        val sleepF =
          if (x.offset.offsetAndMetadata.offset() % 2 == 0) IO.sleep(7.seconds)
          else IO.unit
        sleepF.as(x.offset)
      })
      .parJoinUnbounded
      .debug(x => s"Offset after: ${x.offsetAndMetadata.offset()}")
      .through(commitBatchWithin[IO](2, 5.seconds))
      .debug(_ => s"Committing")

    stream.compile.drain.as(ExitCode.Success)
  }
}
