package cat.michal.catbase.client;

import cat.michal.catbase.common.exception.CatBaseException;

import java.io.IOException;

public interface BaseClient {
    void connect(String addr, int port) throws CatBaseException;

    void disconnect() throws CatBaseException;
}
