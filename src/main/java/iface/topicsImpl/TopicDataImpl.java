package iface.topicsImpl;

import iface.topics.ETopicMode;
import iface.topics.TopicData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopicDataImpl implements TopicData {

    private String topicName;
    private String id;
    private ETopicMode topicMode;


    public String toString() {
        return String.format("Topic name: %s, DomainId: %s, Topic mode: %s", topicName, id, topicMode);
    }
}
