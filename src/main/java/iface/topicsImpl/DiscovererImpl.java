package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicData;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataDataReader;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataSeq;
import com.rti.dds.publication.builtin.PublicationBuiltinTopicDataTypeSupport;
import com.rti.dds.subscription.*;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicData;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataDataReader;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataSeq;
import com.rti.dds.subscription.builtin.SubscriptionBuiltinTopicDataTypeSupport;
import iface.Constants;
import iface.topics.Discoverer;
import iface.topics.ETopicMode;
import iface.topics.TopicData;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;


@SuppressWarnings("FieldMayBeFinal")
@Getter
public class DiscovererImpl implements Discoverer {

    private static final Logger logger = LogManager.getLogger(DiscovererImpl.class);
    private String id;
    private DomainParticipant participant;
    private Collection<String> allowedTopics;
    private Collection<String> deniedTopics;
    private Consumer<TopicData> eventHandler;
    private Set<TopicData> discoveredTopics;
    private final Object lock;

    public DiscovererImpl(String id, Collection<String> allowedTopics, Collection<String> deniedTopics, Consumer<TopicData> eventHandler) {
        this.participant = DomainParticipantFactory.TheParticipantFactory.create_participant(
                Constants.DOMAIN_ID,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);

        this.id = id;
        this.allowedTopics = allowedTopics;
        this.deniedTopics = deniedTopics;
        this.eventHandler = eventHandler;
        this.participant
                .get_builtin_subscriber()
                .lookup_datareader(SubscriptionBuiltinTopicDataTypeSupport.SUBSCRIPTION_TOPIC_NAME)
                .set_listener(new BuiltinSubscriberListener(), StatusKind.DATA_AVAILABLE_STATUS);
        this.participant
                .get_builtin_subscriber()
                .lookup_datareader(PublicationBuiltinTopicDataTypeSupport.PUBLICATION_TOPIC_NAME)
                .set_listener(new BuiltinPublisherListener(), StatusKind.DATA_AVAILABLE_STATUS);

        this.discoveredTopics = new HashSet<>();

        this.lock = new Object();

        this.participant.enable();
    }

    @Override
    public Set<TopicData> getDiscoveredTopics() {
        return new HashSet<>(discoveredTopics);
    }

    @Override
    public void changeHandler(Consumer<TopicData> newHandler) {
        this.eventHandler = newHandler;
    }

    @Override
    public void close() throws Exception {
        this.participant.delete_contained_entities();
        DomainParticipantFactory.TheParticipantFactory.delete_participant(this.participant);
    }

    private class BuiltinSubscriberListener extends DataReaderAdapter {
        SubscriptionBuiltinTopicDataSeq _dataSeq =
                new SubscriptionBuiltinTopicDataSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        // This gets called when a new subscriber has been discovered
        public void on_data_available(DataReader reader) {
            SubscriptionBuiltinTopicDataDataReader builtin_reader =
                    (SubscriptionBuiltinTopicDataDataReader) reader;

            try {
                // We only process newly seen subscribers
                builtin_reader.take(
                        _dataSeq, _infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.NEW_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE);

                for (int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = _infoSeq.get(i);

                    if (info.valid_data) {
                        SubscriptionBuiltinTopicData subscriptionBuiltinTopicData =
                                (SubscriptionBuiltinTopicData) _dataSeq.get(i);

                        String topicName = subscriptionBuiltinTopicData.topic_name;
                        if (allowedTopics != null && !allowedTopics.contains(topicName)) {
                            logger.info("Ignoring subcription because it's not inside allowed topics: {}", info.instance_handle);
                            participant.ignore_subscription(info.instance_handle);
                            continue;
                        }

                        if (deniedTopics != null && deniedTopics.contains(topicName)) {
                            logger.info("Ignoring subcription because it's inside denied topics: {}", info.instance_handle);
                            participant.ignore_subscription(info.instance_handle);
                            continue;
                        }

                        String id = new String(subscriptionBuiltinTopicData.user_data.value.toArrayByte(null));

                        TopicDataImpl topicData = new TopicDataImpl(topicName, id, ETopicMode.READ);
                        synchronized (lock) {
                            discoveredTopics.add(topicData);
                        }

                        eventHandler.accept(topicData);
                    }
                }

            } catch (RETCODE_NO_DATA ignored) {
            } finally {
                builtin_reader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }

    private class BuiltinPublisherListener extends DataReaderAdapter {
        PublicationBuiltinTopicDataSeq _dataSeq =
                new PublicationBuiltinTopicDataSeq();
        SampleInfoSeq _infoSeq = new SampleInfoSeq();

        // This gets called when a new subscriber has been discovered
        public void on_data_available(DataReader reader) {
            PublicationBuiltinTopicDataDataReader builtin_reader =
                    (PublicationBuiltinTopicDataDataReader) reader;

            try {
                // We only process newly seen subscribers
                builtin_reader.take(
                        _dataSeq, _infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.NEW_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE);

                for (int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = _infoSeq.get(i);

                    if (info.valid_data) {
                        PublicationBuiltinTopicData publicationBuiltinTopicData =
                                (PublicationBuiltinTopicData) _dataSeq.get(i);

                        String topicName = publicationBuiltinTopicData.topic_name;
                        if (allowedTopics != null && !allowedTopics.contains(topicName)) {
                            logger.info("Ignoring publication because it's not inside allowed topics: {}", info.instance_handle);
                            participant.ignore_publication(info.instance_handle);
                            continue;
                        }

                        if (deniedTopics != null && deniedTopics.contains(topicName)) {
                            logger.info("Ignoring publication because it's inside denied topics: {}", info.instance_handle);
                            participant.ignore_publication(info.instance_handle);
                            continue;
                        }

                        String id = new String(publicationBuiltinTopicData.user_data.value.toArrayByte(null));

                        TopicDataImpl topicData = new TopicDataImpl(topicName, id, ETopicMode.WRITE);
                        synchronized (lock) {
                            discoveredTopics.add(topicData);
                        }

                        eventHandler.accept(topicData);
                    }
                }

            } catch (RETCODE_NO_DATA ignored) {
            } finally {
                builtin_reader.return_loan(_dataSeq, _infoSeq);
            }
        }
    }
}
