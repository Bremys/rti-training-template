import com.google.common.collect.Lists;
import iface.PubSubWrapper;
import iface.PubSubWrapperImpl;
import iface.objects.Corona;
import iface.objects.Message;
import iface.topics.Discoverer;
import iface.topics.Publisher;
import iface.topics.Subscriber;
import iface.topics.Topic;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class PubSubTest {

    public static PubSubWrapper api;
    public Consumer<Message> noopConsumer = msg -> {
    };

    @Before
    public void setUp() throws Exception {
        api = new PubSubWrapperImpl();
    }

    @After
    public void after() throws Exception {
        api.close();
    }

    @Test
    public void testWriterCreation() {
        Publisher<Message> writer1 = api.getOrCreateWriter("1", "Corona");
        Publisher<Message> writer2 = api.getOrCreateWriter("2", "Corona");
        assertThat(writer1).isNotNull();
        assertThat(writer2).isNotNull();
        assertThat(writer1).isSameAs(api.getOrCreateWriter("1", "Corona"));
        assertThat(writer2).isSameAs(api.getOrCreateWriter("2", "Corona"));
        assertThat(writer1).isNotSameAs(writer2);
    }

    @Test
    public void testReaderCreation() {
        Subscriber<Message> reader1 = api.getOrCreateReader("1", "Corona", noopConsumer);
        Subscriber<Message> reader2 = api.getOrCreateReader("2", "Corona", noopConsumer);
        assertThat(reader1).isNotNull();
        assertThat(reader2).isNotNull();
        assertThat(reader1).isSameAs(api.getOrCreateReader("1", "Corona", noopConsumer));
        assertThat(reader2).isSameAs(api.getOrCreateReader("2", "Corona", noopConsumer));
        assertThat(reader1).isNotSameAs(reader2);
    }

    @Test
    public void testWritingReading() {
        AtomicInteger received = new AtomicInteger(0);
        AtomicBoolean allEqual = new AtomicBoolean(true);
        Corona toSend = new Corona();
        toSend.msg = "unique af";
        Publisher<Corona> writer = api.getOrCreateWriter("writer", "Corona");
        Subscriber<Corona> reader = api.getOrCreateReader("reader", "Corona", msg -> {
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
        Publisher<Message> writer = api.getOrCreateWriter("writer", "Message");
        Subscriber<Message> reader1 = api.getOrCreateReader("reader1", "Message", noopConsumer);
        Subscriber<Message> reader2 = api.getOrCreateReader("reader2", "Message", noopConsumer);
        Awaitility.await().untilAsserted(() -> assertThat(discovered).hasValue(3));
        assertThat(discoverer.getDiscoveredTopics())
                .contains(writer.getTopicData(), reader1.getTopicData(), reader2.getTopicData());
    }

    @Test
    public void multipleWritersSingleReader() throws InterruptedException {
        List<Corona> messagesToSend = Arrays.stream("cool stuff bruh !".split(" "))
                .map(msg -> {
                    Corona message = new Corona();
                    message.msg = msg;
                    return message;
                })
                .collect(Collectors.toList());
        List<Corona> received = new CopyOnWriteArrayList<>(); // Thread-safe list
        List<Publisher<Corona>> publishers = Lists.newArrayList(api.getOrCreateWriter("writer1", "Corona"), api.getOrCreateWriter("writer2", "Corona"));
        api.<Corona>getOrCreateReader("reader", "Corona", received::add);

        for (int i = 0; i < messagesToSend.size(); i++) { // Why can't I access index on streams, java please
            publishers.get(i % publishers.size()).send(messagesToSend.get(i));
        }

        Awaitility.await().untilAsserted(() -> assertThat(received).containsAll(messagesToSend));
    }

    @Test
    public void allowedTopicsDiscovery() {
        Topic corona1 = api.getOrCreateWriter("corona1", "Corona");
        Topic corona2 = api.getOrCreateWriter("corona2", "Message");
        Discoverer discoverer1 = api.openDiscoverer("discoverer1", Collections.singleton("Corona"), null, topic -> {
        });
        Discoverer discoverer2 = api.openDiscoverer("discoverer2", Collections.singleton("Message"), null, topic -> {
        });
        Discoverer discoverer3 = api.openDiscoverer("discoverer3", topic -> {
        });

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
        Topic corona1 = api.getOrCreateWriter("corona1", "Corona");
        Topic corona2 = api.getOrCreateWriter("corona2", "Message");
        Topic corona3 = api.getOrCreateReader("corona3", "Amitay", noopConsumer);

        Discoverer discoverer1 = api.openDiscoverer("discoverer1", null, Collections.singleton("Corona"), topic -> {
        });
        Discoverer discoverer2 = api.openDiscoverer("discoverer2", null, Collections.singleton("Message"), topic -> {
        });

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
        Topic corona1 = api.getOrCreateWriter("corona1", "Corona");
        Topic corona2 = api.getOrCreateWriter("corona2", "Message");
        Discoverer discoverer = api.openDiscoverer("discoverer", Collections.singleton("Corona"), Collections.singleton("Message"), topic -> {
        });

        Awaitility.await()
                .untilAsserted(
                        () -> assertThat(discoverer.getDiscoveredTopics())
                                .containsExactly(corona1.getTopicData()));
    }
}
