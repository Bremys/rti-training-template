package iface.topics;

import java.io.Serializable;

public interface TopicData extends Serializable {
    String getId();
    String getTopicName();
    ETopicMode getTopicMode();
}

