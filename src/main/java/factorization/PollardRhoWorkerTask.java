package factorization;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class PollardRhoWorkerTask implements Runnable{
    // gcd is expensive, so the differences are multiplied up and gcd is only taken once per batch
    private static final int BATCH = 128;

    private final BigInteger n;
    private final AtomicReference<BigInteger> result;
    private final AtomicLong iterations;

    public PollardRhoWorkerTask(BigInteger n, AtomicReference<BigInteger> result, AtomicLong iterations){
        this.n = n;
        this.result = result;
        this.iterations = iterations;
    }

    @Override
    public void run(){
        if(!n.testBit(0)){
            result.compareAndSet(null, BigInteger.TWO);
            return;
        }

        // a run can fail (gcd == n), then simply start again with new random values
        while(result.get() == null){
            BigInteger factor = brent();

            if(factor != null && result.compareAndSet(null, factor)){
                log.info("Factor found (Pollard rho, Brent): {}", factor);
            }
        }
    }

    /**
     * Pollard rho with Brent's cycle detection.
     * Returns a factor of n, or null if the run failed or another worker was faster.
     */
    private BigInteger brent(){
        BigInteger y = randomValue();
        BigInteger c = randomValue();
        BigInteger x = y;
        BigInteger q = BigInteger.ONE;
        BigInteger g = BigInteger.ONE;
        long r = 1;

        while(g.equals(BigInteger.ONE)){
            if(result.get() != null) return null;

            x = y;
            for(long i = 0; i < r; i++){
                y = f(y, c);
            }

            long k = 0;
            while(k < r && g.equals(BigInteger.ONE)){
                long steps = Math.min(BATCH, r - k);

                for(long i = 0; i < steps; i++){
                    y = f(y, c);
                    q = q.multiply(x.subtract(y).abs()).mod(n);
                }

                g = q.gcd(n);
                k += steps;
                iterations.addAndGet(steps);
            }

            r *= 2;
        }

        return g.equals(n) ? null : g;
    }

    // pseudo-random sequence: f(x) = x^2 + c mod n
    private BigInteger f(BigInteger x, BigInteger c){
        return x.multiply(x).add(c).mod(n);
    }

    // random value in [1, n - 1]
    private BigInteger randomValue(){
        return new BigInteger(n.bitLength(), ThreadLocalRandom.current())
                .mod(n.subtract(BigInteger.ONE))
                .add(BigInteger.ONE);
    }
}
