package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.domain.DomainParticipantListener;
import com.rti.dds.infrastructure.Cookie_t;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.Locator_t;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.publication.*;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.InconsistentTopicStatus;
import com.rti.dds.topic.Topic;
import iface.topics.Discoverer;
import iface.topics.TopicData;
import lombok.Getter;

import java.util.Set;
import java.util.function.Consumer;


@Getter
public class DiscovererImpl implements Discoverer {

    private String id;
    private DomainParticipant participant;

    public DiscovererImpl(String domainId){
        this.id = domainId;
    }

    @Override
    public Set<TopicData> getDiscoveredTopics() {
        return null;
    }

    @Override
    public void changeHandler(Consumer<TopicData> newHandler) {

    }

    @Override
    public void close() throws Exception {

    }
}
