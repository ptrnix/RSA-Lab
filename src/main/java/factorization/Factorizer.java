package factorization;

import java.math.BigInteger;

// Strategy: different algorithms that split n into its prime factors.
public interface Factorizer{
    // Returns a non-trivial factor of n (1 < factor < n).
    BigInteger findFactor(BigInteger n) throws InterruptedException;
    String name();
}
