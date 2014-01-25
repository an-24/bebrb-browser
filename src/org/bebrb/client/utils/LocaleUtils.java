package org.bebrb.client.utils;

import java.lang.reflect.Field;

import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import application.Main;

public class LocaleUtils {

	public static void localeFields(Object controller) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = controller.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.isAnnotationPresent(javafx.fxml.FXML.class)) {
				String nameRString = "control-"+controller.getClass().getSimpleName()+
							  		 "-"+field.getName();
				if(Main.getStrings().containsKey(nameRString)) {
					String rs = Main.getStrings().getString(nameRString);
					if(rs!=null) {
						Object value = field.get(controller);
						if(value!=null) {
							if(value instanceof Labeled) 
								((Labeled)value).setText(rs);else
									if(value instanceof MenuItem)
										((MenuItem)value).setText(rs);else
											if(value instanceof TextInputControl)
												((TextInputControl)value).setPromptText(rs);
						}
					}
				};
				
			}
		}
	}

}
