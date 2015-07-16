package com.github.tkobayas.drools.warmup.loadtest.join;

import java.util.concurrent.CountDownLatch;

import com.github.tkobayas.drools.warmup.loadtest.join.JoinMultiThreadTestBase;

public class JoinThroughputObserver implements Runnable {
    
    private CountDownLatch startLatch;
    
    public JoinThroughputObserver(CountDownLatch latch) {
        startLatch = latch;
    }

    public void run() {
        
        try {
            startLatch.await();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        long startTime = System.currentTimeMillis();
        long lastTime = startTime;
        
        long lastNum = 0;
        
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currentTime = System.currentTimeMillis();
            long currentNum = JoinMultiThreadTestBase.resultNum.get();
            
            if ((currentNum - lastNum) == 0) {
                System.out.println("load test is not working?");
                break;
            }
            
            System.out.println((lastTime - startTime) + " -> " + (currentTime - startTime)
                    + " : throughput = " + ((double)(currentNum - lastNum)/(currentTime - lastTime)) + "(num/ms)");
            
            lastNum = currentNum;
            lastTime = currentTime;
            
        }
    }

}
