package com.ugame.prophecy.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class JmsSender {
    public static void main(String[] args) throws JMSException {
	ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
		ActiveMQConnection.DEFAULT_USER,
		ActiveMQConnection.DEFAULT_PASSWORD,
		"tcp://127.0.0.1:61616");
	Connection connection = connectionFactory.createConnection();
	connection.start();
	Session session = connection.createSession(Boolean.TRUE,
		Session.AUTO_ACKNOWLEDGE);
	Destination destination = session.createQueue("my-queue");
	MessageProducer producer = session.createProducer(destination);
	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
	sendMsg(session, producer);
	session.commit();
	connection.close();
    }

    public static void sendMsg(Session session, MessageProducer producer)
	    throws JMSException {
	TextMessage message = session.createTextMessage("Hello ActiveMQ！");
	producer.send(message);
	System.out.println("");
    }
}
