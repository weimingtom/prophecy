package com.ugame.prophecy.mq.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 消息的消费者（接受者）
 * @see http://activemq.apache.org/hello-world.html
 * @see http://lavasoft.blog.51cto.com/62575/190815
 */
public class JmsReceiver {
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
	MessageConsumer consumer = session.createConsumer(destination);
	while (true) {
	    TextMessage message = (TextMessage) consumer.receive(1000);
	    if (null != message) {
		System.out.println("收到消息：" + message.getText());
	    } else {
		break;
	    }
	}
	session.close();
	connection.close();
    }
}
