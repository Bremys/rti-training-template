package topics;

import java.io.Serializable;

public interface Publisher<T extends Serializable> extends Topic {
    boolean send(T entity);
}
