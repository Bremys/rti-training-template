package topics;

import java.util.function.Consumer;

public interface ReaderTopic<T> extends Topic {
    void changeHandler(Consumer<T> newHandler);
}
