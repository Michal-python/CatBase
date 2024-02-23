package cat.michal.catbase.server.procedure;

public final class ProcedureRegistry {
    private ProcedureRegistry() {
    }

    public static final ConnectionEstablishmentProcedure CONNECTION_ESTABLISHMENT_PROCEDURE = new ConnectionEstablishmentProcedure();
}