package iface;

import com.google.common.collect.ImmutableMap;
import com.rti.dds.infrastructure.Copyable;
import iface.topics.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

public interface PubSubWrapper extends AutoCloseable {
    <T extends Copyable & Serializable> Publisher<T> getOrCreateWriter(String id, String topicName);
    <T extends Copyable & Serializable> Subscriber<T> getOrCreateReader(String id, String topicName, Consumer<T> eventHandler);
    Discoverer openDiscoverer(String id, Collection<String> allowedTopics, Collection<String> deniedTopics, Consumer<TopicData> eventHandler);
    default Discoverer openDiscoverer(String id, Consumer<TopicData> eventHandler) {
        return openDiscoverer(id, null, null, eventHandler);
    }

    void closeDiscoverer(String id);
    void closeTopic(String id);

    ImmutableMap<String, Publisher<?>> getPublishers();
    ImmutableMap<String, Subscriber<?>> getSubscribers();
    ImmutableMap<String, Discoverer> getRunningDiscoverers();
}
