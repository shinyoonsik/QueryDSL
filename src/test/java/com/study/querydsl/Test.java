package com.study.querydsl;

import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    class AtomicIntegerExample {
        private AtomicInteger count = new AtomicInteger(0);

        public void increment() {
            count.incrementAndGet(); // 원자적으로 +1 연산 수행
        }

        public void decrement() {
            count.decrementAndGet(); // 원자적으로 -1 연산 수행
        }

        public int getCount() {
            return count.get(); // 현재 값 반환
        }
    }

    class MyInstance{
        private int count;

        public int getCount(){
            return this.count;
        }

        public void increment() {
            this.count++;
        }

        public void decrement() {
            this.count--;
        }


    }

    @org.junit.jupiter.api.Test
    void test() throws InterruptedException {
        AtomicIntegerExample example = new AtomicIntegerExample();

        // 1000번 증가시키는 스레드
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.increment();
            }
        });

        // 1000번 감소시키는 스레드
        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.decrement();
            }
        });

        thread1.start();
        thread2.start();

        thread1.join(); // main 스레드가 thread1의 종료를 기다림
        thread2.join(); // main 스레드가 thread2의 종료를 기다림

        System.out.println(example.getCount()); // 출력: 0
    }

}
