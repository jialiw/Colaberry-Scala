import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ ConsumerMessage, ConsumerSettings, Subscriptions }
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ ByteArrayDeserializer, StringDeserializer }

/**
  * Created by Dan on 5/1/2017.
  */
object MessageProcessorSource {
  def create(groupId: String)(implicit system: ActorSystem):Source[ConsumerMessage.CommittableMessage[Array[Byte],String], Consumer.Control] = {
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer) // tell Kafka that I want to read from you about specific topic
      .withBootstrapServers("192.168.99.100:9092")
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(MessageTopic.Topic))
  }
}
