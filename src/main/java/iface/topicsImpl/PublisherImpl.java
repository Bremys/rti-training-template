package iface.topicsImpl;

import iface.topics.Publisher;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class PublisherImpl<T extends Serializable> implements Publisher<T> {

    @NonNull
    private TopicData topicData;

    @Override
    public void send(T entity) {

    }

    @Override
    public void close() throws Exception {

    }
}
