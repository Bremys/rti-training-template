package iface.topicsImpl;

import iface.topics.Discoverer;
import iface.topics.TopicData;
import lombok.Getter;

import java.util.Set;
import java.util.function.Consumer;


@Getter
public class DiscovererImpl implements Discoverer {

    private int id;

    @Override
    public Set<TopicData> getDiscoveredTopics() {
        return null;
    }

    @Override
    public void changeHandler(Consumer<TopicData> newHandler) {

    }

    @Override
    public void close() throws Exception {

    }
}
