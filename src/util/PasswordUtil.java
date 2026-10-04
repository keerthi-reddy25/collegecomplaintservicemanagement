package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Turns a plain password into a SHA-256 hash (64 lowercase hex characters).
 * MySQL's SHA2(password, 256) gives the same result, which is why the sample
 * users in schema.sql can log in.
 *
 * Note: plain SHA-256 is fine for learning. A real application should use a
 * salted, slow hash such as bcrypt.
 */
public class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available on this JVM", e);
        }
    }
}
