package com.ugame.prophecy.mq.simple;

public class SynchronizedQueueProducerConsumers {
    public static void main(String[] args) {
	SynchronizedQueue<String> queue = new SynchronizedQueue<String>(10);
	final int GREETING_COUNT = 100;
	Runnable run1 = new SynchronizedQueueProducer("Hello, World!", queue, GREETING_COUNT);
	Runnable run2 = new SynchronizedQueueProducer("Goodbye, World!", queue, GREETING_COUNT);
	Runnable run3 = new SynchronizedQueueConsumer(queue, 2 * GREETING_COUNT);
	Thread thread1 = new Thread(run1);
	Thread thread2 = new Thread(run2);
	Thread thread3 = new Thread(run3);
	thread1.start();
	thread2.start();
	thread3.start();
    }
}

class SynchronizedQueueProducer implements Runnable {
    private String greeting;
    private SynchronizedQueue<String> queue;
    private int greetingCount;
    public SynchronizedQueueProducer(String aGreeting, SynchronizedQueue<String> aQueue, int count) {
	greeting = aGreeting;
	queue = aQueue;
	greetingCount = count;
    }

    @Override
    public void run() {
	try {
	    int i = 1;
	    while (i <= greetingCount) {
		queue.add(i + ": " + greeting);
		i++;
		Thread.sleep(2000);
	    }
	} catch (InterruptedException exception) {
	    
	}
    }
}

class SynchronizedQueueConsumer implements Runnable {
    private SynchronizedQueue<String> queue;
    private int greetingCount;

    public SynchronizedQueueConsumer(SynchronizedQueue<String> aQueue, int count) {
	queue = aQueue;
	greetingCount = count;
    }

    @Override
    public void run() {
	try {
	    int i = 1;
	    while (i <= greetingCount) {
		String greeting = queue.remove();
		System.out.println(greeting);
		i++;
		Thread.sleep(3000);
	    }
	} catch (InterruptedException exception) {
	}
    }

}

class SynchronizedQueue<V> {
    private Object[] elements;
    private int head;
    private int tail;
    private int size;

    public SynchronizedQueue(int capacity) {
	elements = new Object[capacity];
	head = 0;
	tail = 0;
	size = 0;
    }

    @SuppressWarnings("unchecked")
    public synchronized V remove() throws InterruptedException {
	while (size == 0) {
	    wait();
	}
	V r = (V) elements[head];
	head++;
	size--;
	if (head == elements.length) {
	    head = 0;
	}
	notifyAll();
	return r;
    }

    public synchronized void add(V newValue) throws InterruptedException {
	while (size == elements.length) {
	    wait();
	}
	elements[tail] = newValue;
	tail++;
	size++;
	if (tail == elements.length) {
	    tail = 0;
	}
	notifyAll();
    }
}
