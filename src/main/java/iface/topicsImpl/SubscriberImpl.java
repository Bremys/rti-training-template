package iface.topicsImpl;

import iface.topics.Subscriber;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.function.Consumer;

@Getter
@RequiredArgsConstructor
public class SubscriberImpl<T extends Serializable> implements Subscriber<T> {

    @NonNull
    private TopicData topicData;
    private Consumer<T> handler;

    @Override
    public void changeHandler(Consumer<T> newHandler) {
        handler = newHandler;
    }

    @Override
    public void close() throws Exception {

    }
}
