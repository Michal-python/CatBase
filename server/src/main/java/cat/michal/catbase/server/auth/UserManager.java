package cat.michal.catbase.server.auth;

import cat.michal.catbase.injector.annotations.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public final class UserManager {
    private final List<User> users = new ArrayList<>();

    public void registerUser(String login, String password) {

        if(users.stream().anyMatch(element -> element.login().equals(login))) {
            throw new IllegalArgumentException("Users' logins must be unique (" + login + ") \uD83E\uDD13☝");
        }

        users.add(new User(login, encodePassword(password)));
    }

    public String encodePassword(String password) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {}

        assert digest != null;
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(encodedHash);
    }

    @SuppressWarnings("unused")
    public void unregisterUser(String login) {
        users.removeIf(user -> user.login().equals(login));
    }

    public List<User> getUsers() {
        return users;
    }
}
