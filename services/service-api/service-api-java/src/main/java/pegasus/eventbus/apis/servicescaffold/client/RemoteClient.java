package pegasus.eventbus.apis.servicescaffold.client;

import java.util.Scanner;

import pegasus.eventbus.amqp.AmqpConfiguration;
import pegasus.eventbus.amqp.AmqpConnectionParameters;
import pegasus.eventbus.amqp.AmqpEventManager;
import pegasus.eventbus.apis.servicescaffold.events.ServiceRequest;
import pegasus.eventbus.apis.servicescaffold.events.ServiceRequest.Action;
import pegasus.eventbus.client.EventManager;

public class RemoteClient {

	Scanner scanner = new Scanner(System.in);
	EventManager em = null;
	
	public RemoteClient(EventManager em){
		
		this.em = em;
		
		this.em.subscribe(new ConsoleServiceResponseHandler());
		
		this.em.subscribe(new ConsoleServiceStatusHandler());
	}
	
	public static void main(String[] args) {
		
		AmqpConfiguration config = AmqpConfiguration.getDefault(
				"svc-remote-client", 
				new AmqpConnectionParameters(
					"amqp://guest:guest@localhost:5672/"));
    	
    	EventManager em = new AmqpEventManager(config);
    	
    	em.start();
	
    	RemoteClient client = new RemoteClient(em);
    	
    	while(client.nextCommand()){}
    	
    	em.close();
	}
	
	boolean nextCommand(){

		pl("\nPlease enter a command.");
		p("client > ");
		
		String input = scanner.nextLine();

		if(input.trim() == null){
			return nextCommand();
		}
		
		ClientCommand cmd = ClientCommand.parse(input);
		
		if(cmd.command.equalsIgnoreCase("start")){
			
			this.start(cmd.options[0]);
		}
		
		if(cmd.command.equalsIgnoreCase("stop")){
					
			this.stop(cmd.options[0]);		
		}
		
		if(cmd.command.equalsIgnoreCase("status")){
			
			this.status(cmd.options[0]);
		}
		
		if(cmd.command.equalsIgnoreCase("showall")){
			
			this.showAllServices();
		}
		
		if(cmd.command.equalsIgnoreCase("help")){
			
			this.help();
		}
		
		if(cmd.command.equalsIgnoreCase("quit")){
			
			return false;
		}
		
		waitNSeconds(2);
		
		return true;
	}

	void showAllServices(){
		
		status(".*");
	}
	
	void start(String service){
		
		sendServiceRequest(service, Action.Start);
	}

	void stop(String service){
		
		sendServiceRequest(service, Action.Stop);
	}
	
	void status(String service){
		
		sendServiceRequest(service, Action.Status);
	}
	
	void sendServiceRequest(String service, Action action){
		
		em.publish(new ServiceRequest(service, action));
	}
	
	void help(){
		
		pl("");
		pl("Valid Commands");
		pl("\t [start, stop, status, showall, help, quit]");
		pl("");
		pl("\tstart - starts a service(s)");
		pl("\t\tSyntax:  start [service name or regular expression]");
		pl("");
		pl("\tstop - stops a service(s)");
		pl("\t\tSyntax:  stop [service name or regular expression]");
		pl("");
		pl("\tstatus - status of a service(s)");
		pl("\t\tSyntax:  status [service name or regular expression]");
		pl("");
		pl("\tshowall - get the status of all services");
		pl("\t\tSyntax:  showall [service name or regular expression]");
		pl("");
		pl("\thelp - this command");
		pl("\t\tSyntax:  help");
		pl("");
		pl("\tquit - shutdown the client");
		pl("\t\tSyntax:  quit");
	}
	
	public static void p(String message){
		System.out.print(message);
	}
	
	public static void p(String message, Object... params){
		System.out.print(String.format(message, params));
	}
	
	public static void pl(String message){
		System.out.println(message);
	}
	
	public static void pl(String message, Object... params){
		System.out.println(String.format(message, params));
	}
	
	private void waitNSeconds(int n) {
		try {
			Thread.sleep(n * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static class ClientCommand {
		
		String command = null;
		String[] options = null;
		
		public static ClientCommand parse(String command){
			
			String[] parts = command.trim().split("\\s+");
			
			ClientCommand ccmd = new ClientCommand();
			
			if(parts.length == 0){
				
				ccmd.command = command;
			} 
			else {
				
				ccmd.command = parts[0];
			}

			if(parts.length > 1){
				
				ccmd.options = new String[parts.length -1];
				
				for(int i = 1; i < parts.length; i++){
					
					ccmd.options[i - 1] = parts[i];
				}
			}
			
			return ccmd;
		}
	}
}
