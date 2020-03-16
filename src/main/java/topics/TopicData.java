package topics;

public interface TopicData {
    String getId();
    String getTopicName();
    ETopicMode getTopicMode();
}

enum ETopicMode {
    READ,
    WRITE
}
