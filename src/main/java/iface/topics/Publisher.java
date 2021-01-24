package iface.topics;

import com.rti.dds.infrastructure.Copyable;

import java.io.Serializable;

public interface Publisher<T extends Copyable & Serializable> extends Topic {
    void send(T entity);
}
