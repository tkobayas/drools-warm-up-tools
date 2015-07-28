package com.github.tkobayas.drools.warmup.loadtest.simple;

import java.util.concurrent.CountDownLatch;

import com.github.tkobayas.drools.warmup.loadtest.join.JoinMultiThreadTestBase;

public class ThroughputObserver implements Runnable {
    
    private CountDownLatch startLatch;
    
    public ThroughputObserver(CountDownLatch latch) {
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
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long currentTime = System.currentTimeMillis();
            long currentNum = MultiThreadTestBase.resultNum.get();
            
            if ((currentNum - lastNum) == 0) {
                break;
            }
            
//            System.out.println((lastTime - startTime) + " -> " + (currentTime - startTime)
//                    + " : throughput = " + ((double)(currentNum - lastNum)/(currentTime - lastTime)) + "(num/ms)");
            
            System.out.println(((double)(currentNum - lastNum)/(currentTime - lastTime)));
            
            lastNum = currentNum;
            lastTime = currentTime;
            
        }
    }

}
