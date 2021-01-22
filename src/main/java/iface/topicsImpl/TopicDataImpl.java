package iface.topicsImpl;

import iface.topics.ETopicMode;
import iface.topics.TopicData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class TopicDataImpl implements TopicData {

    private String topicName;
    private String id;
    private ETopicMode topicMode;


    public String toString() {
        return String.format("Topic name: %s, Id: %s, Topic mode: %s", topicName, id, topicMode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicDataImpl topicData = (TopicDataImpl) o;
        return Objects.equals(topicName, topicData.topicName) && Objects.equals(id, topicData.id) && topicMode == topicData.topicMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, id, topicMode);
    }
}
