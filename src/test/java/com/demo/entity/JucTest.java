package com.demo.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.locks.LockSupport;

import static jodd.util.ThreadUtil.sleep;

@SpringBootTest
@Slf4j
public class JucTest {
    @Test
    public void test1() throws InterruptedException {
        Thread t1 = new Thread(()->{
            //执行一个对象的park方法
            log.debug("T1 start");
            sleep(3000);
            log.debug("1:park开始");
            LockSupport.park();
            log.debug("1:park结束");
            log.debug("2:park开始");
            LockSupport.park();
            log.debug("2:park结束");
        },"t1");
        t1.start();
        sleep(1000);
        log.debug("1:unpark");
        LockSupport.unpark(t1);
        sleep(2001);
        log.debug("2:unpark");
        LockSupport.unpark(t1);
        t1.join();
    }
}
