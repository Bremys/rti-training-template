package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.DataWriter;
import com.rti.dds.topic.Topic;
import iface.topics.Publisher;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;

@Getter
public class PublisherImpl<T extends Serializable> implements Publisher<T> {

    @NonNull
    private TopicData topicData;
    private com.rti.dds.publication.Publisher publisher;
    private DataWriter dataWriter;

    public PublisherImpl(DomainParticipant participant, TopicData topicData, Topic topic) {
        publisher = participant.create_publisher(
                DomainParticipant.PUBLISHER_QOS_DEFAULT,
                null,           // listener
                StatusKind.STATUS_MASK_NONE);
        this.topicData = topicData;
        dataWriter =
                publisher.create_datawriter(topic,
                        com.rti.dds.publication.Publisher.DATAWRITER_QOS_DEFAULT,
                        null,           // listener
                        StatusKind.STATUS_MASK_NONE);
        if (dataWriter == null) {
            System.err.println("Unable to create DDS data writer\n");
        }
    }

    @Override
    public void send(T entity) {
        dataWriter.write_untyped(entity, InstanceHandle_t.HANDLE_NIL);
    }

    @Override
    public void close() throws Exception {

    }
}
