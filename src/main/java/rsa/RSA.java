package rsa;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

// Textbook RSA for demonstration purposes only (no padding, tiny keys, NOT secure).
public final class RSA{
    public static final BigInteger DEFAULT_E = BigInteger.valueOf(65537);
    private static final int MIN_BIT_LENGTH = 24;
    private static final SecureRandom RANDOM = new SecureRandom();

    private RSA(){}

    /**
     * Generates a public key whose modulus has exactly bitLength bits.
     * p, q and the private exponent are thrown away on purpose,
     * the attacker has to rebuild them by factoring n.
     */
    public static RsaPublicKey generatePublicKey(int bitLength){
        if(bitLength < MIN_BIT_LENGTH){
            throw new IllegalArgumentException("Key size must be at least " + MIN_BIT_LENGTH + " bits");
        }

        BigInteger p;
        BigInteger q;
        BigInteger n;
        do{
            p = BigInteger.probablePrime(bitLength / 2, RANDOM);
            q = BigInteger.probablePrime(bitLength - bitLength / 2, RANDOM);
            n = p.multiply(q);
            // retry if n is one bit too short, p == q, or e is not invertible modulo phi
        }while(p.equals(q) || n.bitLength() != bitLength || !phi(p, q).gcd(DEFAULT_E).equals(BigInteger.ONE));

        return new RsaPublicKey(n, DEFAULT_E);
    }

    /**
     * Encrypts a text block by block. A single RSA block must be smaller than n,
     * so with a small n the text has to be split into several blocks.
     * <p>
     * Every block gets a leading 0x01 byte before it is turned into a number.
     * Otherwise leading zero bytes would get lost in the BigInteger conversion.
     */
    public static List<BigInteger> encrypt(String text, RsaPublicKey key){
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        int chunkSize = chunkSize(key.n());
        List<BigInteger> blocks = new ArrayList<>();

        for(int offset = 0; offset < data.length; offset += chunkSize){
            int length = Math.min(chunkSize, data.length - offset);

            byte[] block = new byte[length + 1];
            block[0] = 1;
            System.arraycopy(data, offset, block, 1, length);

            // c = m^e mod n
            blocks.add(new BigInteger(1, block).modPow(key.e(), key.n()));
        }

        return blocks;
    }

    /**
     * Decrypts all blocks with the private exponent d and joins them back to the text.
     */
    public static String decrypt(List<BigInteger> blocks, BigInteger n, BigInteger d){
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for(BigInteger c : blocks){
            // m = c^d mod n
            byte[] block = c.modPow(d, n).toByteArray();

            // skip the 0x01 marker byte that encrypt() put in front of the block
            out.write(block, 1, block.length - 1);
        }

        return out.toString(StandardCharsets.UTF_8);
    }

    /**
     * Rebuilds the private exponent d from the public key and one prime factor p of n.
     */
    public static BigInteger privateExponent(RsaPublicKey key, BigInteger p){
        BigInteger q = key.n().divide(p);

        // d is the multiplicative inverse of e modulo phi(n)
        return key.e().modInverse(phi(p, q));
    }

    // Maximum number of text bytes per block: one byte is reserved for the marker
    // and the block value has to stay below n.
    private static int chunkSize(BigInteger n){
        return (n.bitLength() - 1) / 8 - 1;
    }

    // Euler's totient for n = p * q
    private static BigInteger phi(BigInteger p, BigInteger q){
        return p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
    }
}
