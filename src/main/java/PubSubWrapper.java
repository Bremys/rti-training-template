import com.google.common.collect.ImmutableMap;
import topics.*;

import java.util.List;
import java.util.function.Consumer;

public interface PubSubWrapper extends AutoCloseable {
    <T> WriterTopic<T> getOrCreateWriter(String id, String topicName);
    <T> ReaderTopic<T> getOrCreateReader(String id, String topicName, Consumer<T> eventHandler);
    Discoverer openDiscoverer(String id, List<String> allowedTopics, List<String> deniedTopics, Consumer<TopicData> eventHandler);
    default Discoverer openDiscoverer(String id, Consumer<TopicData> eventHandler) {
        return openDiscoverer(id, null, null, eventHandler);
    }

    boolean closeDiscoverer(String id);
    boolean closeTopic(String id);

    boolean closeAll();

    ImmutableMap<String, Topic> getRunningTopics();
    ImmutableMap<String, Discoverer> getRunningDiscoverers();
}
