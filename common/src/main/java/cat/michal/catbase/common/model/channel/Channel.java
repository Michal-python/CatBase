package cat.michal.catbase.common.model.channel;

public interface Channel {
    Channel WAITING = new WaitingForDeterminationChannel();
}
