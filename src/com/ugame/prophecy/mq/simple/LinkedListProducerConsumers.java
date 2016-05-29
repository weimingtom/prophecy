package com.ugame.prophecy.mq.simple;

import java.util.LinkedList;

public class LinkedListProducerConsumers {
    public static void main(String[] argv) {
	LinkedListWorkQueue queue = new LinkedListWorkQueue();
	int numWorkers = 2;
	LinkedListWorker[] workers = new LinkedListWorker[numWorkers];
	for (int i = 0; i < workers.length; i++) {
	    workers[i] = new LinkedListWorker(queue);
	    workers[i].start();
	}
	for (int i = 0; i < 100; i++) {
	    queue.addWork(i);
	}
    }
}

class LinkedListWorkQueue {
    LinkedList<Object> queue = new LinkedList<Object>();

    public synchronized void addWork(Object o) {
	queue.addLast(o);
	notify();
    }

    public synchronized Object getWork() throws InterruptedException {
	while (queue.isEmpty()) {
	    wait();
	}
	return queue.removeFirst();
    }
}

class LinkedListWorker extends Thread {
    LinkedListWorkQueue q;

    LinkedListWorker(LinkedListWorkQueue q) {
	this.q = q;
    }

    @Override
    public void run() {
	try {
	    while (true) {
		Object x = q.getWork();
		if (x == null) {
		    break;
		}
		System.out.println(x);
	    }
	} catch (InterruptedException e) {
	}
    }
}
