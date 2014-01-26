package org.bebrb.client.controls;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class PaneControl extends AnchorPane {

	private ObservableList<Pane> pages = FXCollections.observableArrayList();
	private Pane activePage;
	private OnChange onChange;
	private OnChanged onChanged;
	
	public PaneControl() {
		super();
	}

	public ObservableList<Pane> getPages() {
		return pages;
	}

	public Pane getActivePage() {
		return activePage;
	}

	public void setActivePage(Pane activePage) {
		if(this.activePage != activePage) {
			if(onChange==null || (onChange!=null && onChange.change(activePage))) {
				Pane oldPane = this.activePage;
				this.activePage = activePage;
				update();
				if(onChanged!=null) onChanged.changed(oldPane);
			}
		}
	}

	public void setActivePage(int activePage) {
		setActivePage(pages.get(activePage));
	}
	
	private void update() {
		getChildren().clear();
		if(activePage!=null) {
			getChildren().add(activePage);
			AnchorPane.setBottomAnchor(activePage,0D);
			AnchorPane.setTopAnchor(activePage,0D);
			AnchorPane.setLeftAnchor(activePage,0D);
			AnchorPane.setRightAnchor(activePage,0D);
		}
	} 

	public OnChange getOnChange() {
		return onChange;
	}

	public void setOnChange(OnChange onChange) {
		this.onChange = onChange;
	}

	public OnChanged getOnChanged() {
		return onChanged;
	}

	public void setOnChanged(OnChanged onChanged) {
		this.onChanged = onChanged;
	}

	
	public interface OnChange {
		public boolean change(Pane newPane);
	}

	public interface OnChanged {
		public void changed(Pane oldPane);
	}
}
