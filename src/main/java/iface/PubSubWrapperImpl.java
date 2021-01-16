package iface;

import com.google.common.collect.ImmutableMap;
import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.topic.Topic;
import iface.objects.Plot;
import iface.objects.PlotTypeSupport;
import iface.objects.Utils;
import iface.topics.*;
import iface.topicsImpl.PublisherImpl;
import iface.topicsImpl.SubscriberImpl;
import iface.topicsImpl.TopicDataImpl;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PubSubWrapperImpl implements PubSubWrapper {

    private DomainParticipant participant;
    private Map<String, Publisher<? extends Serializable>> publishers;
    private Map<String, Subscriber<? extends Serializable>> subscribers;

    public PubSubWrapperImpl() {
        participant = DomainParticipantFactory.get_instance().create_participant(
                1,
                DomainParticipantFactory.PARTICIPANT_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);

        publishers = new HashMap<>();
        subscribers = new HashMap<>();

        //register plot type
        PlotTypeSupport.register_type(participant,
                PlotTypeSupport.get_type_name());
    }

    @Override
    public <T extends Serializable> Publisher<T> getOrCreateWriter(String id, String topicName) {
        if (publishers.containsKey(id)) {
            return (Publisher<T>) publishers.get(id);
        } else {
            TopicData data = new TopicDataImpl(topicName, id, ETopicMode.WRITE);
            Publisher<T> publisher = new PublisherImpl<>(participant, data, createTopic(topicName));
            publishers.put(id, publisher);
            return publisher;
        }
    }

    /**
     * creates a topic
     *
     * @return rti topic instance
     */
    private Topic createTopic(String topicName) {
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
        if (subscribers.containsKey(id)) {
            return (Subscriber<T>) subscribers.get(id);
        } else {
            TopicData data = new TopicDataImpl(topicName, id, ETopicMode.READ);
            Subscriber<T> subscriber = new SubscriberImpl<>(participant, data, createTopic(topicName));
            subscriber.changeHandler(eventHandler);
            subscribers.put(id, subscriber);
            return subscriber;
        }
    }

    @Override
    public Discoverer openDiscoverer(String id, Collection<String> allowedTopics, Collection<String> deniedTopics, Consumer<TopicData> eventHandler) {
        return null;
    }

    @Override
    public void closeDiscoverer(String id) {

    }

    @Override
    public void closeTopic(String id) {

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
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    public static void main(String[] args) throws InterruptedException {
        PubSubWrapperImpl impl = new PubSubWrapperImpl();
        Publisher<Plot> publisher = impl.getOrCreateWriter("tomer", "Plot");
        impl.getOrCreateReader("tomer", "Plot", System.out::println);
        Plot p = new Plot();
        p.azimuth = 2;
        Thread.sleep(1000);
        publisher.send(p);
        while (true) {
            Thread.sleep(1000);
        }
    }
}
