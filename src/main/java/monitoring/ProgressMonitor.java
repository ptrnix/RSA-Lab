package monitoring;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ProgressMonitor implements Runnable{
    private final AtomicLong iterations;

    public ProgressMonitor(AtomicLong iterations){
        this.iterations = iterations;
    }

    @Override
    public void run(){
        try{
            while(!Thread.currentThread().isInterrupted()){
                Thread.sleep(1000);
                log.info("Iterations: {}", iterations.get());
            }
        }catch(InterruptedException e){
            // normal shutdown: the factorizer interrupts the monitor when it is done
            log.debug("Monitor stopped");
        }
    }
}
