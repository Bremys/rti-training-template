import example.Message;
import static org.mockito.Mockito.*;
import iface.PubSubWrapper;
import iface.topics.Discoverer;
import iface.topics.Subscriber;
import iface.topics.Publisher;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.function.Consumer;

public class MockedClasses {
    public static PubSubWrapper getMockedPubSub() throws Exception {
        PubSubWrapper mocked = mock(PubSubWrapper.class);
        Subscriber<Message> reader = getMockedSubscriber();
        Publisher<Message> writer = getMockedPublisher();
        Discoverer discoverer = getMockedDiscoverer();
        when(mocked.<Message>getOrCreateReader(anyString(), anyString(), any(Consumer.class))).thenReturn(reader);
        when(mocked.<Message>getOrCreateWriter(anyString(), anyString())).thenReturn(writer);
        when(mocked.openDiscoverer(anyString(), any(Consumer.class))).thenReturn(discoverer);
        Mockito.doNothing().when(mocked).close();
        return mocked;
    }

    private static Subscriber<Message> getMockedSubscriber(){
        Subscriber<Message> mocked = mock(Subscriber.class);
        when(mocked.getTopicData()).thenReturn(null);
        return mocked;
    }

    private static Publisher<Message> getMockedPublisher(){
        Publisher<Message> mocked = mock(Publisher.class);
        Mockito.doNothing().when(mocked).send(any(Message.class));
        when(mocked.getTopicData()).thenReturn(null);
        return mocked;
    }

    private static Discoverer getMockedDiscoverer(){
        Discoverer mocked = mock(Discoverer.class);
        when(mocked.getDiscoveredTopics()).thenReturn(Collections.emptySet());
        return mocked;
    }
}
