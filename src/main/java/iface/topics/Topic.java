package iface.topics;

public interface Topic extends AutoCloseable {
    TopicData getTopicData();
}
