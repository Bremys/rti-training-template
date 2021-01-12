package iface.topicsImpl;

import iface.topics.ETopicMode;
import iface.topics.TopicData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopicDataImpl implements TopicData {

    private String topicName;
    private int Id;
    private ETopicMode topicMode;

}
