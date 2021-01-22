package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.domain.DomainParticipantFactory;
import com.rti.dds.infrastructure.InstanceHandle_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.Topic;
import com.rti.dds.util.LoanableSequence;
import iface.objects.Utils;
import iface.topics.Subscriber;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.function.Consumer;

@SuppressWarnings("FieldMayBeFinal")
@Getter
public class SubscriberImpl<T extends Serializable> implements Subscriber<T> {

    private static final Logger logger = LogManager.getLogger(SubscriberImpl.class);
    @NonNull
    private TopicData topicData;
    private Consumer<T> handler;
    private com.rti.dds.subscription.Subscriber subscriber;
    private DataReader dataReader;

    public SubscriberImpl(DomainParticipant participant, TopicData topicData, Topic topic) {
        this.topicData = topicData;
        this.subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);

        DataReaderQos qos = new DataReaderQos();
        DomainParticipantFactory.TheParticipantFactory.get_datareader_qos_from_profile(qos, "DefaultLibrary", "default");
        qos.user_data.value.addAllByte(topicData.getId().getBytes());

        this.dataReader =
                subscriber.create_datareader(
                        topic,
                        qos,
                        new MyDataReader(),
                        StatusKind.STATUS_MASK_ALL);
        if (this.dataReader == null) {
            logger.error("Unable to create DDS Data Reader!");
            throw new RuntimeException("HelloSubscriber creation failed");
        }
        topic.enable();
        subscriber.enable();
    }

    @Override
    public void changeHandler(Consumer<T> newHandler) {
        handler = newHandler;
    }

    @Override
    public void close() throws Exception {
        this.subscriber.delete_contained_entities();
    }

    private class MyDataReader implements DataReaderListener {

        private LoanableSequence _dataSeq = new LoanableSequence(Utils.getObjectClass(topicData.getTopicName()));
        private SampleInfoSeq _infoSeq = new SampleInfoSeq();

        @Override
        public void on_requested_deadline_missed(DataReader dataReader, RequestedDeadlineMissedStatus requestedDeadlineMissedStatus) {

        }

        @Override
        public void on_requested_incompatible_qos(DataReader dataReader, RequestedIncompatibleQosStatus requestedIncompatibleQosStatus) {

        }

        @Override
        public void on_sample_rejected(DataReader dataReader, SampleRejectedStatus sampleRejectedStatus) {

        }

        @Override
        public void on_liveliness_changed(DataReader dataReader, LivelinessChangedStatus livelinessChangedStatus) {

        }

        @Override
        public void on_data_available(DataReader dataReader) {
            try {
                dataReader.take_instance_untyped(
                        _dataSeq,
                        _infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        InstanceHandle_t.HANDLE_NIL,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.ANY_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE
                );
                _dataSeq.forEach(handler);

            } catch (RETCODE_NO_DATA ignore) {
            } finally {
                dataReader.return_loan_untyped(_dataSeq, _infoSeq);
            }
        }

        @Override
        public void on_sample_lost(DataReader dataReader, SampleLostStatus sampleLostStatus) {

        }

        @Override
        public void on_subscription_matched(DataReader dataReader, SubscriptionMatchedStatus subscriptionMatchedStatus) {

        }
    }
}
