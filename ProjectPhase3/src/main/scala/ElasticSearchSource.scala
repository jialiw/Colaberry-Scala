import akka.actor.ActorSystem
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import com.typesafe.config.ConfigFactory

/**
  * Created by Dan on 5/3/2017.
  */
object ElasticSearchSource {
  def create(groupId: String)(implicit system: ActorSystem):Source[ConsumerMessage.CommittableMessage[Array[Byte],String], Consumer.Control] = {
    val config = ConfigFactory.load()
    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer) // tell Kafka that I want to read from you about specific topic
      .withBootstrapServers(config.getString("kafka.broker-list"))
      .withGroupId(groupId)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(consumerSettings, Subscriptions.topics(MessageTopic2.Topic))
  }
}
