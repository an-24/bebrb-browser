package org.bebrb.client.controls.validators;

import java.util.ArrayList;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;


@SuppressWarnings("serial")
public class ValidateController extends ArrayList<Validator> {
	
	public boolean action(Pane root) {
		boolean b = true;
		int minIdx = Integer.MAX_VALUE;
		Control first = null;
		for (Validator v : this) {
			v.action();
			if(v.isInvalidate()) {
				b = false;
				int idx = findInPane((short) 0,v.getControl(),root);
				if(idx>=0 && idx<minIdx) {
					minIdx = idx;
					first = v.getControl();
				}
			}
		}
		if(minIdx<Integer.MAX_VALUE) first.requestFocus();
			
		return b;
	}

	private int findInPane(short start,Control control, Parent p) {
		int i = start<<16;
		for (Node n : p.getChildrenUnmodifiable()) {
			if(n==control) return i;
			if(n instanceof Parent) {
				int idx = findInPane(++start,control,(Parent)n);
				if(idx>=0) return idx;
			}
			i++;
		}
		return -1;
	}

}
