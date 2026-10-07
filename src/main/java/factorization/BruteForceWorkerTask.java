package factorization;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class BruteForceWorkerTask implements Runnable{
    // how often the shared counter is updated and the result is checked
    private static final int BATCH = 1_000_000;

    private final BigInteger n;
    private final long start;
    private final long end;
    private final AtomicReference<BigInteger> result;
    private final AtomicLong iterations;

    public BruteForceWorkerTask(BigInteger n, long start, long end, AtomicReference<BigInteger> result, AtomicLong iterations){
        this.n = n;
        this.start = start;
        this.end = end;
        this.result = result;
        this.iterations = iterations;
    }

    @Override
    public void run(){
        // n is odd, so only odd candidates have to be tested
        long candidate = (start % 2 == 0) ? start + 1 : start;
        long counter = 0;

        for(; candidate <= end; candidate += 2){
            BigInteger divisor = BigInteger.valueOf(candidate);

            if(n.mod(divisor).signum() == 0){
                if(result.compareAndSet(null, divisor)){
                    log.info("Factor found (brute force): {}", divisor);
                }
                return;
            }

            if(++counter % BATCH == 0){
                iterations.addAndGet(BATCH);
                if(result.get() != null) return; // another worker was faster
            }
        }
    }
}
