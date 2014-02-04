package org.bebrb.client.utils;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class Resources extends ResourceBundle {
	private static final ResourceBundle bungles = ResourceBundle.getBundle(
			"bundles.strings", Locale.getDefault(), new UTF8Control());

	public static final ResourceBundle getBungles() {
		return bungles;
	}

}
