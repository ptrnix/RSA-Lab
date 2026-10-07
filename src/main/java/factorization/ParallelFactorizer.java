package factorization;

import monitoring.ProgressMonitor;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Template method: starts the workers and the monitor and waits for the result.
 * Subclasses only decide what the workers do.
 */
public abstract class ParallelFactorizer implements Factorizer{
    protected final int threads;

    protected ParallelFactorizer(int threads){
        this.threads = threads;
    }

    // Creates the workers. The first one that finds a factor stores it in result.
    protected abstract List<Runnable> createWorkers(BigInteger n, AtomicReference<BigInteger> result, AtomicLong iterations);

    @Override
    public BigInteger findFactor(BigInteger n) throws InterruptedException{
        AtomicReference<BigInteger> result = new AtomicReference<>();
        AtomicLong iterations = new AtomicLong();

        Thread monitor = new Thread(new ProgressMonitor(iterations), "monitor");
        monitor.setDaemon(true);
        monitor.start();

        // close() waits until all workers have finished
        try(ExecutorService executor = Executors.newFixedThreadPool(threads)){
            createWorkers(n, result, iterations).forEach(executor::submit);
        }finally{
            monitor.interrupt();
        }

        BigInteger factor = result.get();
        if(factor == null) throw new IllegalStateException(name() + ": no factor found");
        return factor;
    }
}
