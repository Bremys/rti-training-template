package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.publication.DataWriterQos;
import com.rti.dds.topic.Topic;
import iface.topics.Publisher;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

@Getter
public class PublisherImpl<T extends Serializable> implements Publisher<T> {

    private static final Logger logger = LogManager.getLogger(PublisherImpl.class);
    @NonNull
    private TopicData topicData;
    private com.rti.dds.publication.Publisher publisher;
    private DataWriter dataWriter;

    public PublisherImpl(DomainParticipant participant, TopicData topicData, Topic topic) {

        this.publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT,
                null,           // listener
                StatusKind.STATUS_MASK_NONE);
        this.topicData = topicData;

        DataWriterQos qos = new DataWriterQos();
        DomainParticipantFactory.TheParticipantFactory.get_datawriter_qos_from_profile(qos, "DefaultLibrary", "default");
        qos.user_data.value.addAllByte(topicData.getId().getBytes());

        this.dataWriter =
                publisher.create_datawriter(topic,
                        qos,
                        null,           // listener
                        StatusKind.STATUS_MASK_NONE);
        if (this.dataWriter == null) {
            logger.error("Unable to create DDS data writer\n");
        }
        topic.enable();
        publisher.enable();
    }

    @Override
    public void send(T entity) {
        logger.info("Sending entity: {}", entity);
        dataWriter.write_untyped(entity, InstanceHandle_t.HANDLE_NIL);
    }

    @Override
    public void close() throws Exception {
        this.publisher.delete_contained_entities();
    }
}
