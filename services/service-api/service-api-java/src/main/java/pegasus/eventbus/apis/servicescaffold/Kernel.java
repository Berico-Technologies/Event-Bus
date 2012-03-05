package pegasus.eventbus.apis.servicescaffold;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import pegasus.eventbus.apis.servicescaffold.events.ComponentListResponse;
import pegasus.eventbus.apis.servicescaffold.events.ComponentRequest;
import pegasus.eventbus.apis.servicescaffold.events.ComponentResponse;
import pegasus.eventbus.client.EventHandler;
import pegasus.eventbus.client.EventManager;
import pegasus.eventbus.client.EventResult;

/**
 * Represents a more significant Service (Kernel) that has
 * aggregate components that are started or stopped internally.
 * @author Richard Clayton (rclayton@bericotechnologies.com)
 *
 */
public abstract class Kernel extends Service {

	public static int MAX_NUMBER_OF_COMPONENTS = 1000;
	
	protected final ArrayBlockingQueue<Component> components 
		= new ArrayBlockingQueue<Component>(MAX_NUMBER_OF_COMPONENTS);
	
	/**
	 * Initialize the Kernel.
	 * @param eventManager Event Manager instance.
	 */
	public Kernel(EventManager eventManager) {
		super(eventManager);
		
		initialize();
	}

	/**
	 * Initialize the Kernel by wiring up the ComponentRequest listener
	 * on the bus.
	 */
	private void initialize(){
		
		this.eventManager.subscribe(
			/**
			 * ComponentRequest Handler
			 */
			new EventHandler<ComponentRequest>(){

				@SuppressWarnings("unchecked")
				public Class<? extends ComponentRequest>[] getHandledEventTypes() {
					return new Class[]{ ComponentRequest.class };
				}

				public EventResult handleEvent(ComponentRequest request) {
					
					if(getServiceId().matches(
							request.getServiceIdOrPattern())){
						
						List<Component> matchingComponents 
							= getMatchingComponents(request.getComponentIdOrPattern());
						
						if(matchingComponents.size() > 0){
						
							switch(request.getAction()){
								case Start : 
									startComponent(request.getComponentIdOrPattern(), request.getOptions());
									break;
								case Stop : 
									stopComponent(request.getComponentIdOrPattern());
									break;
								case Status : 
									getComponentStatus(request.getComponentIdOrPattern());
									break;
								case Uninstall :
									uninstallComponent(request.getComponentIdOrPattern());
									break;
								default : 
									__listComponents();
									break;
							}
						}
					}
					
					return EventResult.Handled;
				}
		});
	}
	
	/**
	 * Start a Component
	 * @param componentId ID of the Component to Start
	 * @param options  A Set of options for the component
	 * @return A reference to that component
	 */
	protected abstract Component doStartComponent(String componentId, Map<String, String> options);
	
	/**
	 * Stop a Component
	 * @param componentId ID of the Component to Stop
	 */
	protected abstract void doStopComponent(String componentId);
	
	/**
	 * Uninstall a Component
	 * @param componentId ID of the Component to Stop
	 */
	protected abstract void doUninstallComponent(String componentId);
	
	/**
	 * Get a Component by its ID
	 * @param componentId ID of the component to get
	 * @return Component or null if it is not found
	 */
	public Component getComponent(String componentId){
		
		for(Component c : this.components){
			
			if(c.getId().equals(componentId)){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Get an array of Components
	 * @return array of components
	 */
	public Component[] getComponents(){
		return this.components.toArray(new Component[]{});
	}
	
	
	public void startComponent(String componentName, Map<String, String> options){
		
		Component c = doStartComponent(componentName, options);
		
		this.components.add(c);
		
		publishComponentResponse(c.getId(), "Component was started.");
	}
	
	public void stopComponent(String componentId){
		
		doStopComponent(componentId);
		
		publishComponentResponse(componentId, "Component was stopped.");
	}
	
	public void uninstallComponent(String componentId){
		
		doUninstallComponent(componentId);
		
		publishComponentResponse(componentId, "Component was uninstalled.");
	}
	
	public ComponentStatus getComponentStatus(String componentId){
		
		Component c = getComponent(componentId);
		
		if(c != null){
		
			ComponentStatus cs = createStatusWrapper(c);
			
			this.eventManager.publish(cs);
			
			return cs;
		}
		
		return null;
	}
	
	private void __listComponents(){
		
		ComponentListResponse clr = new ComponentListResponse(this.getServiceId());
		
		for(Component c : this.components){
			
			clr.addComponentStatus(createStatusWrapper(c));
		}
		
		this.eventManager.publish(clr);
	}
	
	
	protected void publishComponentResponse(String componentId, String message){
		
		this.eventManager.publish(
				new ComponentResponse(
						this.getServiceId(), 
						componentId, 
						message));
	}
	
	protected static ComponentStatus createStatusWrapper(Component c){
		
		return new ComponentStatus(c.getId(), c.getStartTime(), c.getProperties(), c.isRunning());
	}
	
	
	/**
	 * Get all components whose name matches the supplied pattern.
	 * @param componentNamePattern component name pattern
	 * @return List of components that match the pattern
	 */
	protected List<Component> getMatchingComponents(String componentNamePattern){
		
		List<Component> matchingComponents = new ArrayList<Component>();
		
		for(Component c : this.components){
			
			if(c.getId().matches(componentNamePattern)){
			
				matchingComponents.add(c);
			}
		}
		return matchingComponents;
	}
	
}
