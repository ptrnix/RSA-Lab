package factorization;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class BruteForceFactorizer extends ParallelFactorizer{
    public BruteForceFactorizer(int threads){
        super(threads);
    }

    @Override
    public String name(){
        return "Brute force";
    }

    @Override
    protected List<Runnable> createWorkers(BigInteger n, AtomicReference<BigInteger> result, AtomicLong iterations){
        // n = p * q, so the smaller factor is at most sqrt(n)
        // (works for n up to 126 bit, because the search range has to fit into a long)
        long maxSearch = n.sqrt().longValueExact();

        // split the search range evenly: [0, block - 1], [block, 2 * block - 1], ...
        long blockSize = maxSearch / threads;

        List<Runnable> workers = new ArrayList<>();
        for(int i = 0; i < threads; i++){
            long start = Math.max(3, i * blockSize);
            long end = (i == threads - 1) ? maxSearch : (i + 1) * blockSize - 1;
            workers.add(new BruteForceWorkerTask(n, start, end, result, iterations));
        }

        return workers;
    }
}
