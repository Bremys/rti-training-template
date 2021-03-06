import com.google.common.collect.Lists;
import example.Message;
import iface.PubSubWrapper;
import iface.topics.*;
import impl.RtiPubSub;
import org.awaitility.Awaitility;
import org.junit.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        api.close();
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
        Awaitility.await().untilAsserted(() -> assertThat(received).hasValue(3));
        assertThat(allEqual).isTrue();
    }

    @Test
    public void testDiscovery() {
        AtomicInteger discovered = new AtomicInteger(0);
        Discoverer discoverer = api.openDiscoverer("discoverer", topicData -> discovered.incrementAndGet());
        Publisher<Message> writer = api.getOrCreateWriter("writer", "CORONA");
        Subscriber<Message> reader1 = api.getOrCreateReader("reader1", "CORONA", noopConsumer);
        Subscriber<Message> reader2 = api.getOrCreateReader("reader2", "CORONA", noopConsumer);
        Awaitility.await().untilAsserted(() -> assertThat(discovered).hasValue(3));
        assertThat(discoverer.getDiscoveredTopics())
                .contains(writer.getTopicData(), reader1.getTopicData(), reader2.getTopicData());
    }

    @Test
    public void multipleWritersSingleReader() {
        List<Message> messagesToSend = Arrays.stream("cool stuff bruh !".split(" "))
                                        .map(Message::new)
                                        .collect(Collectors.toList());
        List<Message> received = new CopyOnWriteArrayList<>(); // Thread-safe list
        List<Publisher<Message>> publishers = Lists.newArrayList(api.getOrCreateWriter("writer1", "CORONA"), api.getOrCreateWriter("writer2", "CORONA"));
        api.<Message>getOrCreateReader("reader", "CORONA", received::add);

        for (int i = 0; i < messagesToSend.size(); i++){ // Why can't I access index on streams, java please
            publishers.get(i % publishers.size()).send(messagesToSend.get(i));
        }

        Awaitility.await().untilAsserted(() -> assertThat(received).containsAll(messagesToSend));
    }

    @Test
    public void allowedTopicsDiscovery() {
        Topic corona1 = api.getOrCreateWriter("corona1", "CORONA1");
        Topic corona2 = api.getOrCreateWriter("corona2", "CORONA2");
        Discoverer discoverer1 = api.openDiscoverer("discoverer1", Collections.singleton("CORONA1"), null, topic -> {});
        Discoverer discoverer2 = api.openDiscoverer("discoverer2", Collections.singleton("CORONA2"), null, topic -> {});
        Discoverer discoverer3 = api.openDiscoverer("discoverer3", topic -> {});

        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer1.getDiscoveredTopics())
                                .containsExactly(corona1.getTopicData()));
        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer2.getDiscoveredTopics())
                                .containsExactly(corona2.getTopicData()));
        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer3.getDiscoveredTopics())
                                .containsExactlyInAnyOrder(corona1.getTopicData(), corona2.getTopicData()));
    }

    @Test
    public void deniedTopicsDiscovery() {
        Topic corona1 = api.getOrCreateWriter("corona1", "CORONA1");
        Topic corona2 = api.getOrCreateWriter("corona2", "CORONA2");
        Topic corona3 = api.getOrCreateReader("corona3", "CORONA3", noopConsumer);

        Discoverer discoverer1 = api.openDiscoverer("discoverer1",  null, Collections.singleton("CORONA1"), topic -> {});
        Discoverer discoverer2 = api.openDiscoverer("discoverer2",  null, Collections.singleton("CORONA2"), topic -> {});

        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer1.getDiscoveredTopics())
                                .containsExactlyInAnyOrder(corona3.getTopicData(), corona2.getTopicData()));
        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer2.getDiscoveredTopics())
                                .containsExactlyInAnyOrder(corona3.getTopicData(), corona1.getTopicData()));
    }

    @Test
    public void testAllowedDeniedDiscovery() {
        Topic corona1 = api.getOrCreateWriter("corona1", "CORONA1");
        Topic corona2 = api.getOrCreateWriter("corona2", "CORONA2");
        Discoverer discoverer = api.openDiscoverer("discoverer", Collections.singleton("CORONA1"), Collections.singleton("CORONA2"), topic -> {});

        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer.getDiscoveredTopics())
                                .containsExactly(corona1.getTopicData()));
    }
}
