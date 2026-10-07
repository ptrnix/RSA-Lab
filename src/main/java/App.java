import factorization.BruteForceFactorizer;
import factorization.Factorizer;
import factorization.PollardRhoFactorizer;
import lombok.extern.slf4j.Slf4j;
import rsa.RSA;
import rsa.RsaPublicKey;

import java.math.BigInteger;
import java.util.List;
import java.util.Scanner;

// p -> first prime number
// q -> second prime number
// n -> modulus (p * q)
// e -> public exponent
// d -> private exponent
// m -> original message (plaintext)
// c -> encrypted message (ciphertext)

// Encryption: c = m^e mod n
// Decryption: m = c^d mod n
// If p and q are known, d can be calculated.
// Therefore, p and q must remain secret.

@Slf4j
public class App{
    private static final String DEFAULT_TEXT = "Hello World!";
    private static final int DEFAULT_BIT_LENGTH = 64;
    private static final int THREADS = Math.min(Runtime.getRuntime().availableProcessors(), 16);

    public static void main(String[] args) throws InterruptedException{
        Scanner scanner = new Scanner(System.in);
        String text = ask(scanner, "Text to encrypt", DEFAULT_TEXT);
        int bitLength = Integer.parseInt(ask(scanner, "Key size in bits", String.valueOf(DEFAULT_BIT_LENGTH)));

        // 1) The victim: only the public key and the ciphertext leave this step
        RsaPublicKey key = RSA.generatePublicKey(bitLength);
        List<BigInteger> ciphertext = RSA.encrypt(text, key);
        log.info("Public key: n={} ({} bit), e={}", key.n(), key.n().bitLength(), key.e());
        log.info("Ciphertext: {}", ciphertext);

        // 2) The attacker: factor n, rebuild the private key and decrypt
        log.info("Attacking with {} threads", THREADS);
        for(Factorizer factorizer : List.of(new BruteForceFactorizer(THREADS), new PollardRhoFactorizer(THREADS))){
            attack(factorizer, key, ciphertext);
        }
    }

    private static void attack(Factorizer factorizer, RsaPublicKey key, List<BigInteger> ciphertext) throws InterruptedException{
        log.info("===== {} =====", factorizer.name());
        long start = System.nanoTime();

        BigInteger p = factorizer.findFactor(key.n());
        BigInteger d = RSA.privateExponent(key, p);
        String plaintext = RSA.decrypt(ciphertext, key.n(), d);

        log.info("Decrypted: \"{}\" in {} ms", plaintext, (System.nanoTime() - start) / 1_000_000);
    }

    private static String ask(Scanner scanner, String question, String fallback){
        System.out.print(question + " [" + fallback + "]: ");
        String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
        return line.isEmpty() ? fallback : line;
    }
}
