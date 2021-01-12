package iface.topicsImpl;

import iface.topics.Topic;
import iface.topics.TopicData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TopicImpl implements Topic {

    private TopicData topicData;

    @Override
    public void close() throws Exception {

    }
}
