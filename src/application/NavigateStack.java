package application;

import java.util.ArrayList;

public class NavigateStack {

	private ArrayList<CommandPoint> CommandStack = new ArrayList<>();
	private CommandPoint topCommand = null;

	public void push(CommandPoint point) {
		if(topCommand==null) {
			CommandStack.add(point);
		} else {
			// cut stack
			cut(topCommand);
			CommandStack.add(point);
		}
		topCommand = point;
	}
	
	public CommandPoint getTop() {
		return topCommand;
	}

	public int cut(CommandPoint point) {
		int r = 0;
		int idx = CommandStack.indexOf(point);
		while(idx<CommandStack.size()-1) {
			CommandStack.remove(idx+1);
			r++;
		}
		topCommand = point;
		return r;
	}
	
	public void back() {
		if(isBackPossible()) {
			CommandPoint cmd = topCommand; 
			cmd.back();
			int idx = CommandStack.indexOf(topCommand);
			if(idx==0) topCommand = null; 
				 else topCommand = CommandStack.get(idx-1);
			if(cmd instanceof CommandPointEx)
				((CommandPointEx)cmd).afterBack();
		}
	}

	public void next() {
		if(isNextPossible()) {
			if(topCommand==null) topCommand = CommandStack.get(0); 
			CommandPoint cmd = topCommand; 
			cmd.next();
			int idx = CommandStack.indexOf(topCommand);
			if(idx<CommandStack.size()-1)
				topCommand = CommandStack.get(idx+1); 
			if(cmd instanceof CommandPointEx)
				((CommandPointEx)cmd).afterNext();
		}
	}
	
	public boolean isNextPossible() {
		return (CommandStack.size()>0) && (topCommand==null ||
				CommandStack.get(CommandStack.size()-1)!=topCommand);
	}

	public boolean isBackPossible() {
		return topCommand!=null; 
	}

	public CommandPoint getNext(CommandPoint p) {
		int idx = CommandStack.indexOf(p);
		if(idx<CommandStack.size()-1)
			return CommandStack.get(idx+1);
		return null;
	}

	public CommandPoint getBack(CommandPoint p) {
		int idx = CommandStack.indexOf(p);
		if(idx>0) return CommandStack.get(idx-1);
		return null;
	}
	
	
	public interface CommandPoint {
		public void back();
		public void next();
	}
	public interface CommandPointEx extends CommandPoint {
		public void afterBack();
		public void afterNext();
	}
}
