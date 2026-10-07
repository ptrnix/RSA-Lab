package factorization;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class PollardRhoFactorizer extends ParallelFactorizer{
    public PollardRhoFactorizer(int threads){
        super(threads);
    }

    @Override
    public String name(){
        return "Pollard rho (Brent)";
    }

    @Override
    protected List<Runnable> createWorkers(BigInteger n, AtomicReference<BigInteger> result, AtomicLong iterations){
        // every worker starts from a different random point, the first one to succeed wins
        List<Runnable> workers = new ArrayList<>();
        for(int i = 0; i < threads; i++){
            workers.add(new PollardRhoWorkerTask(n, result, iterations));
        }

        return workers;
    }
}
