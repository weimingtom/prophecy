package com.ugame.prophecy.mq.simple;

import java.util.Vector;

public class VectorProducerConsumers {
    public static void main(String args[]) {
	Queue queue = new Queue();
	new VectorProducer(queue).start();
	new VectorConsumer("ConsumerA", queue).start();
	new VectorConsumer("ConsumerB", queue).start();
	new VectorConsumer("ConsumerC", queue).start();
    }
}

class VectorProducer extends Thread {
    Queue queue;

    VectorProducer(Queue queue) {
	this.queue = queue;
    }

    @Override
    public void run() {
	int i = 0;
	while (true) {
	    queue.add(i++);
	}
    }
}

class VectorConsumer extends Thread {
    String str;
    Queue queue;

    VectorConsumer(String str, Queue queue) {
	this.str = str;
	this.queue = queue;
    }

    @Override
    public void run() {
	while (true) {
	    System.out.println(str + ": " + queue.remove());
	}
    }
}

class Queue {
    private final static int SIZE = 5;
    private Vector<Object> queue = new Vector<Object>();
    private int count = 0;

    synchronized void add(int i) {
	while (count == SIZE) {
	    try {
		wait();
	    } catch (InterruptedException ie) {
		ie.printStackTrace();
		System.exit(0);
	    }
	}
	queue.addElement(new Integer(i));
	++count;
	notifyAll();
    }

    synchronized int remove() {
	while (count == 0) {
	    try {
		wait();
	    } catch (InterruptedException ie) {
		ie.printStackTrace();
		System.exit(0);
	    }
	}
	Integer iobj = (Integer) queue.firstElement();
	queue.removeElement(iobj);
	--count;
	notifyAll();
	return iobj.intValue();
    }
}
