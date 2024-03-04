package cat.michal.catbase.client;

import cat.michal.catbase.common.exception.CatBaseException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CatBaseClientHandler implements Runnable {

    private final BufferedReader inputStream;
    private final CatBaseClient catBaseClient;

    public CatBaseClientHandler(@NotNull CatBaseClient client) {
        this.catBaseClient = client;
        try {
            this.inputStream = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    @Override
    public void run() {
        while (catBaseClient.isConnected()) {

        }
    }
}
