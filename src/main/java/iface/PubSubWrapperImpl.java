package iface;

import com.google.common.collect.ImmutableMap;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.topic.Topic;
import iface.objects.*;
import iface.topics.*;
import iface.topicsImpl.DiscovererImpl;
import iface.topicsImpl.PublisherImpl;
import iface.topicsImpl.SubscriberImpl;
import iface.topicsImpl.TopicDataImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "FieldMayBeFinal"})
public class PubSubWrapperImpl implements PubSubWrapper {

    private static final Logger logger = LogManager.getLogger(PubSubWrapperImpl.class);
    private DomainParticipant participant;
    private Map<String, PublisherImpl<? extends Serializable>> publishers;
    private Map<String, SubscriberImpl<? extends Serializable>> subscribers;
    private Map<String, DiscovererImpl> discoverers;

    public PubSubWrapperImpl() {
        participant = DomainParticipantFactory.TheParticipantFactory.create_participant(
                Constants.DOMAIN_ID,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);

        publishers = new HashMap<>();
        subscribers = new HashMap<>();
        discoverers = new HashMap<>();

        //register types
        PlotTypeSupport.register_type(participant,
                PlotTypeSupport.get_type_name());

        MessageTypeSupport.register_type(participant,
                MessageTypeSupport.get_type_name());

        AmitayTypeSupport.register_type(participant,
                AmitayTypeSupport.get_type_name());

        CoronaTypeSupport.register_type(participant,
                CoronaTypeSupport.get_type_name());

        participant.enable();
    }

    @Override
    public <T extends Serializable> Publisher<T> getOrCreateWriter(String id, String topicName) {
        logger.info("creating publisher: {} in domain {}", topicName, id);
        return (Publisher<T>) publishers.computeIfAbsent(id, key -> {
            TopicData data = new TopicDataImpl(topicName, id, ETopicMode.WRITE);
            return new PublisherImpl<T>(participant, data, getOrCreateTopic(topicName));
        });
    }

    /**
     * creates a topic
     *
     * @return rti topic instance
     */
    private Topic getOrCreateTopic(String topicName) {
        String typeName = Utils.getTypeName(topicName);
        Topic topic = participant.find_topic(topicName, Duration_t.DURATION_AUTO);
        if (topic == null) {
            topic = participant.create_topic(
                    topicName,
                    typeName,
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,   // listener
                    StatusKind.STATUS_MASK_NONE);
        }
        return topic;
    }


    @Override
    public <T extends Serializable> Subscriber<T> getOrCreateReader(String id, String topicName, Consumer<T> eventHandler) {
        logger.info("creating subscriber: {} in domain {}", topicName, id);
        SubscriberImpl<T> subscriber = (SubscriberImpl<T>) subscribers.computeIfAbsent(id, key -> {
            TopicData data = new TopicDataImpl(topicName, id, ETopicMode.READ);
            return new SubscriberImpl<>(participant, data, getOrCreateTopic(topicName));
        });
        subscriber.changeHandler(eventHandler);
        return subscriber;
    }

    @Override
    public Discoverer openDiscoverer(String id, Collection<String> allowedTopics, Collection<String> deniedTopics, Consumer<TopicData> eventHandler) {
        logger.info("creating discoverer in domain {}", id);
        DiscovererImpl discoverer = discoverers.computeIfAbsent(id, key -> new DiscovererImpl(id, allowedTopics, deniedTopics, eventHandler));
        discoverer.changeHandler(eventHandler);
        return discoverer;
    }

    @Override
    public void closeDiscoverer(String id) {
        try {
            discoverers.remove(id).close();
        } catch (Exception e) {
            logger.error("Could not close discoverer", e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void closeTopic(String topicName) {
        logger.info("closing topic {}", topicName);

        subscribers
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getTopicData().getTopicName().equals(topicName))
                .forEach(entry -> {
                    participant.delete_subscriber(subscribers.remove(entry.getKey()).getSubscriber());
                });

        publishers
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getTopicData().getTopicName().equals(topicName))
                .forEach(entry -> {
                    participant.delete_publisher(publishers.remove(entry.getKey()).getPublisher());
                });

        participant.delete_topic(getOrCreateTopic(topicName));
    }

    @Override
    public ImmutableMap<String, Publisher<?>> getPublishers() {
        return ImmutableMap.copyOf(publishers);
    }

    @Override
    public ImmutableMap<String, Subscriber<?>> getSubscribers() {
        return ImmutableMap.copyOf(subscribers);
    }

    @Override
    public ImmutableMap<String, Discoverer> getRunningDiscoverers() {
        return ImmutableMap.copyOf(discoverers);
    }

    @Override
    public void close() throws Exception {
        this.participant.delete_contained_entities();
        for (Discoverer discoverer : discoverers.values()) {
            discoverer.close();
        }
        discoverers = null;
        subscribers = null;
        publishers = null;
    }

    public static void main(String[] args) throws InterruptedException {
        PubSubWrapperImpl impl = new PubSubWrapperImpl();
        Discoverer discoverer = impl.openDiscoverer("discoverer", null, null, System.out::println);
        Publisher<Plot> publisher = impl.getOrCreateWriter("tomer", "Plot");
        impl.getOrCreateReader("tomer", "Plot", System.out::println);
        Plot p = new Plot();
        p.azimuth = 2;
        while (true) {
            Thread.sleep(1000);
            publisher.send(p);
        }
    }
}
