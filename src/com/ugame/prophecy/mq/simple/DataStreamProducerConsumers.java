package com.ugame.prophecy.mq.simple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @see http://www.java2s.com/CN/Tutorial/Java/0160__Thread/ProducerandcomsumerwithDataInputStreamandDataOutputStream.htm
 * @author Administrator
 *
 */
public class DataStreamProducerConsumers {
    public static void main(String[] args) throws IOException {
	PipedOutputStream pout = new PipedOutputStream();
	PipedInputStream pin = new PipedInputStream(pout);
	DataStreamNumberProducer fw = new DataStreamNumberProducer(pout, 20);
	DataStreamNumberConsumer fr = new DataStreamNumberConsumer(pin);
	fw.start();
	fr.start();
    }
}

class DataStreamNumberProducer extends Thread {
    private DataOutputStream theOutput;
    private int howMany;

    public DataStreamNumberProducer(OutputStream out, int howMany) {
	theOutput = new DataOutputStream(out);
	this.howMany = howMany;
    }

    @Override
    public void run() {
	try {
	    for (int i = 0; i < howMany; i++) {
		theOutput.writeInt(i);
	    }
	} catch (IOException ex) {
	    System.err.println(ex);
	}
    }
}

class DataStreamNumberConsumer extends Thread {
    private DataInputStream theInput;
    
    public DataStreamNumberConsumer(InputStream in) {
	theInput = new DataInputStream(in);
    }

    @Override
    public void run() {
	try {
	    while (true) {
		System.out.println(theInput.readInt());
	    }
	} catch (IOException ex) {
	    if (ex.getMessage().equals("Pipe broken")
		    || ex.getMessage().equals("Write end dead")) {
		// normal termination
		return;
	    }
	    ex.printStackTrace();
	}
    }
}
