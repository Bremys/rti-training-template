import com.google.common.collect.ImmutableMap;
import topics.*;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

public interface PubSubWrapper extends AutoCloseable {
    <T extends Serializable> Publisher<T> getOrCreateWriter(String id, String topicName);
    <T extends Serializable> Subscriber<T> getOrCreateReader(String id, String topicName, Consumer<T> eventHandler);
    Discoverer openDiscoverer(String id, List<String> allowedTopics, List<String> deniedTopics, Consumer<TopicData> eventHandler);
    default Discoverer openDiscoverer(String id, Consumer<TopicData> eventHandler) {
        return openDiscoverer(id, null, null, eventHandler);
    }

    boolean closeDiscoverer(String id);
    boolean closeTopic(String id);

    ImmutableMap<String, Publisher> getPublishers();
    ImmutableMap<String, Subscriber> getSubscribers();
    ImmutableMap<String, Discoverer> getRunningDiscoverers();
}
