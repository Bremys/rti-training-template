package iface.topics;

import java.io.Serializable;

public interface Publisher<T extends Serializable> extends Topic {
    void send(T entity);
}
