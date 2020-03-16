import example.Message;
import org.awaitility.Awaitility;
import org.junit.*;
import topics.Discoverer;
import topics.Subscriber;
import topics.Publisher;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;



public class PubSubTest {

    public static PubSubWrapper api;
    public Consumer<Message> noopConsumer = msg -> {};

    @Before
    public void setUp(){
        api = MockedClasses.getMockedPubSub(); //TODO: Your implementation goes here
    }

    @After
    public void after(){
        api.closeAll();
    }

    @Test
    public void testWriterCreation() {
        Publisher<Message> writer1 = api.getOrCreateWriter("1", "CORONA");
        Publisher<Message> writer2 = api.getOrCreateWriter("2", "CORONA");
        assertThat(writer1).isNotNull();
        assertThat(writer2).isNotNull();
        assertThat(writer1).isSameAs(api.getOrCreateWriter("1", "CORONA"));
        assertThat(writer2).isSameAs(api.getOrCreateWriter("2", "CORONA"));
        assertThat(writer1).isNotSameAs(writer2);
    }

    @Test
    public void testReaderCreation() {
        Subscriber<Message> reader1 = api.getOrCreateReader("1", "CORONA", noopConsumer);
        Subscriber<Message> reader2 = api.getOrCreateReader("2", "CORONA", noopConsumer);
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
        Publisher<Message> writer = api.getOrCreateWriter("writer", "CORONA");
        Subscriber<Message> reader = api.getOrCreateReader("reader", "CORONA", msg -> {
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
        Publisher<Message> writer = api.getOrCreateWriter("writer", "CORONA");
        Subscriber<Message> reader1 = api.getOrCreateReader("reader1", "CORONA", noopConsumer);
        Subscriber<Message> reader2 = api.getOrCreateReader("reader2", "CORONA", noopConsumer);
        Awaitility.await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> assertThat(discovered).hasValue(3));
        assertThat(discoverer.getDiscoveredTopics())
                .containsExactlyInAnyOrder(writer.getTopicData(), reader1.getTopicData(), reader2.getTopicData());
    }
}
