import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ConsoleRunner {
	 public static void main(String[] args) throws IOException {
			// /event-stream-processor-war/src/main/webapp/WEB-INF/espservice-context.xml
	    	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("espservice-context.xml");
	        System.out.println("ESP Service started... Hit any key to stop.");
	        System.in.read();
	        context.close();
	    }
}