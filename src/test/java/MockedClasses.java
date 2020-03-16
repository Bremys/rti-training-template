import example.Message;
import static org.mockito.Mockito.*;
import topics.Discoverer;
import topics.ReaderTopic;
import topics.WriterTopic;

import java.util.Collections;
import java.util.function.Consumer;

public class MockedClasses {
    public static PubSubWrapper getMockedPubSub(){
        PubSubWrapper mocked = mock(PubSubWrapper.class);
        ReaderTopic<Message> reader = getMockedReaderTopic();
        WriterTopic<Message> writer = getMockedWriterTopic();
        Discoverer discoverer = getMockedDiscoverer();
        when(mocked.<Message>getOrCreateReader(anyString(), anyString(), any(Consumer.class))).thenReturn(reader);
        when(mocked.<Message>getOrCreateWriter(anyString(), anyString())).thenReturn(writer);
        when(mocked.openDiscoverer(anyString(), any(Consumer.class))).thenReturn(discoverer);
        when(mocked.closeAll()).thenReturn(false);
        return mocked;
    }

    private static ReaderTopic<Message> getMockedReaderTopic(){
        ReaderTopic<Message> mocked = mock(ReaderTopic.class);
        when(mocked.getTopicData()).thenReturn(null);
        return mocked;
    }

    private static WriterTopic<Message> getMockedWriterTopic(){
        WriterTopic<Message> mocked = mock(WriterTopic.class);
        when(mocked.send(any(Message.class))).thenReturn(false);
        when(mocked.getTopicData()).thenReturn(null);
        return mocked;
    }

    private static Discoverer getMockedDiscoverer(){
        Discoverer mocked = mock(Discoverer.class);
        when(mocked.getDiscoveredTopics()).thenReturn(Collections.emptyList());
        return mocked;
    }
}
