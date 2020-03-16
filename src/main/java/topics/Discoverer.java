package topics;

import java.util.List;
import java.util.function.Consumer;

public interface Discoverer extends AutoCloseable{
    List<TopicData> getDiscoveredTopics();
    String getId();
    void changeHandler(Consumer<TopicData> newHandler);
}
