package application;

import java.util.ArrayList;

import javafx.util.Callback;

public class NavigateStack {
	Callback<Void, Void> endPoint;

	public NavigateStack(Callback<Void, Void> endPoint) {
		this.endPoint = endPoint;
	}


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

	public void clear() {
		topCommand = null;
		CommandStack.clear();
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
			int idx = CommandStack.indexOf(topCommand);
			if(idx==0) {
				topCommand = null;
				endPoint.call(null);
			} else {
				topCommand = CommandStack.get(idx-1);
				topCommand.restore();
			}
		}
	}

	public void next() {
		if(isNextPossible()) {
			int idx = CommandStack.indexOf(topCommand);
			if(idx<CommandStack.size()-1) {
				topCommand = CommandStack.get(idx+1);
				 topCommand.restore();
			}	
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
		public void restore();
	}

}
