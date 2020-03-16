package topics;

public interface Topic extends AutoCloseable {
    TopicData getTopicData();
}
