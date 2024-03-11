package cat.michal.catbase.server.data.time;

public interface TimeSource {

    long nanos();

    long millis();

}
