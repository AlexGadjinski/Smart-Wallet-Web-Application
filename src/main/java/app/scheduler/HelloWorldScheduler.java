package app.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldScheduler {

    // Scheduled Job / Cron Job
    @Scheduled(fixedDelay = 10000)
    public void sayHelloEvery10Seconds() {
//        System.out.println("Hello World!!!");
    }
}
