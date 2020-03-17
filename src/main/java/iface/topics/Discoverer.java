package iface.topics;

import java.util.Set;
import java.util.function.Consumer;

public interface Discoverer extends AutoCloseable{
    Set<TopicData> getDiscoveredTopics();
    String getId();
    void changeHandler(Consumer<TopicData> newHandler);
}
