package cat.michal.catbase.server.procedure;

public final class ProcedureRegistry {
    private ProcedureRegistry() {
    }

    public static final ConnectionEstablishmentProcedure CONNECTION_ESTABLISHMENT_PROCEDURE = new ConnectionEstablishmentProcedure();
    public static final ExchangeDetermineProcedure EXCHANGE_DETERMINE_PROCEDURE = new ExchangeDetermineProcedure();
    public static final InternalMessageProcedure INTERNAL_MESSAGE_PROCEDURE = new InternalMessageProcedure();
}
