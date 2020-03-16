import example.Message;
import org.awaitility.Awaitility;
import org.junit.*;
import topics.Discoverer;
import topics.ReaderTopic;
import topics.WriterTopic;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;



public class PubSubTest {

    public static PubSubWrapper api;
    public Consumer<Message> noopConsumer = msg -> {};

    @Before
    public void setUp() throws Exception {
        api = MockedClasses.getMockedPubSub(); //TODO: Your implementation goes here
    }

    @After
    public void after() throws Exception {
        api.closeAll();
    }

    @Test
    public void testWriterCreation() {
        WriterTopic<Message> writer1 = api.getOrCreateWriter("1", "CORONA");
        WriterTopic<Message> writer2 = api.getOrCreateWriter("2", "CORONA");
        assertThat(writer1).isNotNull();
        assertThat(writer2).isNotNull();
        assertThat(writer1).isSameAs(api.getOrCreateWriter("1", "CORONA"));
        assertThat(writer2).isSameAs(api.getOrCreateWriter("2", "CORONA"));
        assertThat(writer1).isNotSameAs(writer2);
    }

    @Test
    public void testReaderCreation() {
        ReaderTopic<Message> reader1 = api.getOrCreateReader("1", "CORONA", noopConsumer);
        ReaderTopic<Message> reader2 = api.getOrCreateReader("2", "CORONA", noopConsumer);
        assertThat(reader1).isNotNull();
        assertThat(reader2).isNotNull();
        assertThat(reader1).isSameAs(api.getOrCreateReader("1", "CORONA", noopConsumer));
        assertThat(reader2).isSameAs(api.getOrCreateReader("2", "CORONA", noopConsumer));
        assertThat(reader1).isNotSameAs(reader2);
    }

    @Test
    public void testWritingReading() {
        AtomicInteger received = new AtomicInteger(0);
        AtomicBoolean allEqual = new AtomicBoolean(true);
        Message toSend = new Message("unique af");
        WriterTopic<Message> writer = api.getOrCreateWriter("writer", "CORONA");
        ReaderTopic<Message> reader = api.getOrCreateReader("reader", "CORONA", msg -> {
            received.incrementAndGet();
            allEqual.compareAndSet(true, toSend.equals(msg));
        });

        writer.send(toSend);
        writer.send(toSend);
        writer.send(toSend);
        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(received).hasValue(3));
        assertThat(allEqual).isTrue();
    }

    @Test
    public void testDiscovery() {
        AtomicInteger discovered = new AtomicInteger(0);
        Discoverer discoverer = api.openDiscoverer("discoverer", topicData -> discovered.incrementAndGet());
        WriterTopic<Message> writer = api.getOrCreateWriter("writer", "CORONA");
        ReaderTopic<Message> reader1 = api.getOrCreateReader("reader1", "CORONA", noopConsumer);
        ReaderTopic<Message> reader2 = api.getOrCreateReader("reader2", "CORONA", noopConsumer);
        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(discovered).hasValue(3));
        assertThat(discoverer.getDiscoveredTopics())
                .containsExactlyInAnyOrder(writer.getTopicData(), reader1.getTopicData(), reader2.getTopicData());
    }
}
