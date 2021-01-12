package iface;

import com.google.common.collect.ImmutableMap;
import iface.topics.Discoverer;
import iface.topics.Publisher;
import iface.topics.Subscriber;
import iface.topics.TopicData;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Consumer;

public class PubSubWrapperImpl implements PubSubWrapper{
    @Override
    public <T extends Serializable> Publisher<T> getOrCreateWriter(String id, String topicName) {
        return null;
    }

    @Override
    public <T extends Serializable> Subscriber<T> getOrCreateReader(String id, String topicName, Consumer<T> eventHandler) {
        return null;
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
        return null;
    }

    @Override
    public ImmutableMap<String, Subscriber<?>> getSubscribers() {
        return null;
    }

    @Override
    public ImmutableMap<String, Discoverer> getRunningDiscoverers() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
