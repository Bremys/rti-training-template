package iface.topics;

import com.rti.dds.infrastructure.Copyable;

import java.io.Serializable;
import java.util.function.Consumer;

public interface Subscriber<T extends Copyable & Serializable> extends Topic {
    void changeHandler(Consumer<T> newHandler);
}
