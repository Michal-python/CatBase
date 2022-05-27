package cat.michal.catbase;

import cat.michal.catbase.exception.CatBaseException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class CatBaseServerHandler implements Runnable {
    private final CatBase catBase;
    private final Socket client;
    private final BufferedReader inputStream;

    public CatBaseServerHandler(@NotNull Socket client, CatBase catBase) {
        this.client = client;
        this.catBase = catBase;
        try {
            this.inputStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new CatBaseException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String message = inputStream.readLine();
                System.out.println("[S] "+client.getRemoteSocketAddress()+" pisze " + message);
                catBase.sendMessage(message);
            } catch (IOException ignored) {
            }
        }
    }
}
