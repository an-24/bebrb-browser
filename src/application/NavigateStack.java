package application;

import java.util.LinkedList;

public class NavigateStack {

	private LinkedList<CommandPoint> CommandStack = new LinkedList<>();
	private CommandPoint topCommand = null;

	public void push(CommandPoint point) {
		if(topCommand==null) {
			CommandStack.push(point);
		} else {
			int idx = CommandStack.indexOf(topCommand);
			// cut stack
			while(idx<CommandStack.size()-1) {
				CommandStack.remove(idx+1);
			}
			CommandStack.push(point);
		}
		topCommand = point;
	}
	
	public CommandPoint getTop() {
		return topCommand;
	}

	public void back() {
		if(isBackPossible()) {
			topCommand.retry();
			int idx = CommandStack.indexOf(topCommand);
			if(idx==0) topCommand = null; 
				 else topCommand = CommandStack.get(idx-1); 
		}
	}

	public void next() {
		if(isNextPossible()) {
			if(topCommand==null) topCommand = CommandStack.getFirst(); 
			topCommand.next();
			int idx = CommandStack.indexOf(topCommand);
			if(idx<CommandStack.size()-1)
				topCommand = CommandStack.get(idx+1); 
		}
	}
	
	public boolean isNextPossible() {
		return (CommandStack.size()>0) && (topCommand==null ||
				CommandStack.getLast()!=topCommand);
	}

	public boolean isBackPossible() {
		return topCommand!=null; 
	}

	
	public interface CommandPoint {
		public void retry();
		public void next();
	}
}
