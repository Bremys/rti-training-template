package topics;

import java.io.Serializable;
import java.util.function.Consumer;

public interface Subscriber<T extends Serializable> extends Topic {
    void changeHandler(Consumer<T> newHandler);
}
