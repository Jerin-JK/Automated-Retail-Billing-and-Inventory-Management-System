package auth.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2 password hashing using only the JDK. */
public final class PasswordHasher {
    // PBKDF2 work factor and derived-key size used for password storage.
    private static final int ITERATIONS = 210_000;
    private static final int BITS = 256;

    // Prevent instantiation of this static utility.
    private PasswordHasher() {
    }

    /** Creates a salted PBKDF2 hash and encodes its parameters for storage. */
    public static String hash(char[] password) {
        // Generate a fresh random salt so equal passwords get different hashes.
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] derived = derive(password, salt, ITERATIONS);
        return "pbkdf2-sha256$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derived);
    }

    /** Checks a password against the stored algorithm, salt, and hash. */
    public static boolean verify(char[] password, String encoded) {
        try {
            String[] parts = encoded.split("\\$");
            if (parts.length != 4 || !parts[0].equals("pbkdf2-sha256"))
                return false;
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, Base64.getDecoder().decode(parts[2]), Integer.parseInt(parts[1]));
            if (actual.length != expected.length)
                return false;
            // Accumulate byte differences so comparison does not stop at the first mismatch.
            int diff = 0;
            for (int i = 0; i < actual.length; i++)
                diff |= actual[i] ^ expected[i];
            return diff == 0;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /** Derives the fixed-length key using PBKDF2-HMAC-SHA256. */
    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Password hashing unavailable", ex);
        // Clear the password copy held by the key specification.
        } finally {
            spec.clearPassword();
        }
    }
}
