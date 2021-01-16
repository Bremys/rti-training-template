package iface.topicsImpl;

import com.rti.dds.domain.DomainParticipant;
import com.rti.dds.infrastructure.Duration_t;
import com.rti.dds.infrastructure.RETCODE_NO_DATA;
import com.rti.dds.infrastructure.ResourceLimitsQosPolicy;
import com.rti.dds.infrastructure.StatusKind;
import com.rti.dds.subscription.*;
import com.rti.dds.topic.Topic;
import com.rti.dds.typecode.TypeCode;
import com.rti.dds.util.LoanableSequence;
import iface.objects.Plot;
import iface.objects.PlotDataReader;
import iface.objects.PlotSeq;
import iface.objects.PlotTypeSupport;
import iface.topics.Subscriber;
import iface.topics.TopicData;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.function.Consumer;

@Getter
public class SubscriberImpl<T extends Serializable> implements Subscriber<T> {

    @NonNull
    private TopicData topicData;
    private Consumer<T> handler;
    private com.rti.dds.subscription.Subscriber subscriber;
    private PlotDataReader dataReader;

    public SubscriberImpl(DomainParticipant participant, TopicData topicData) {
        subscriber = participant.create_subscriber(
                DomainParticipant.SUBSCRIBER_QOS_DEFAULT,
                null,
                StatusKind.STATUS_MASK_NONE);
        Topic topic = participant.find_topic(topicData.getTopicName(), Duration_t.DURATION_AUTO);
        if (topic == null) {

            topic = participant.create_topic(
                    topicData.getTopicName(),
                    PlotTypeSupport.get_type_name(),
                    DomainParticipant.TOPIC_QOS_DEFAULT,
                    null,   // listener
                    StatusKind.STATUS_MASK_NONE);
        }
        dataReader = (PlotDataReader)
                subscriber.create_datareader(
                        topic,
                        com.rti.dds.subscription.Subscriber.DATAREADER_QOS_DEFAULT,
                        new MyDataReader(),
                        StatusKind.STATUS_MASK_ALL);

        if (dataReader == null) {
            System.err.println("! Unable to create DDS Data Reader");
            throw new RuntimeException("HelloSubscriber creation failed");
        }
    }

    @Override
    public void changeHandler(Consumer<T> newHandler) {
        handler = newHandler;
    }

    @Override
    public void close() throws Exception {

    }

    private class MyDataReader implements DataReaderListener {

        private LoanableSequence _dataSeq = new PlotSeq();
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
            PlotDataReader plotDataReader = (PlotDataReader) dataReader;

            try {
                plotDataReader.take(
                        _dataSeq,
                        _infoSeq,
                        ResourceLimitsQosPolicy.LENGTH_UNLIMITED,
                        SampleStateKind.ANY_SAMPLE_STATE,
                        ViewStateKind.ANY_VIEW_STATE,
                        InstanceStateKind.ANY_INSTANCE_STATE);

                for (int i = 0; i < _dataSeq.size(); ++i) {
                    SampleInfo info = (SampleInfo) _infoSeq.get(i);

                    if (info.valid_data) {
                        Plot p = _dataSeq.get(i);
                        System.out.println("Plot came " + p.toString());
                    }
                }
            } catch (RETCODE_NO_DATA noData) {
                // No data to process
            } finally {
                plotDataReader.return_loan(_dataSeq, _infoSeq);
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
