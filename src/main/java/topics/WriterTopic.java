package topics;

public interface WriterTopic<T> extends Topic {
    boolean send(T entity);
}
