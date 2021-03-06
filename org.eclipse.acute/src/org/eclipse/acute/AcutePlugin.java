/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - Initial implementation
 *  Lucas Bullen (Red Hat Inc.) - Issue #99 - Specify dotnet path
 *******************************************************************************/
package org.eclipse.acute;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AcutePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.acute"; //$NON-NLS-1$

	// The shared instance
	private static AcutePlugin plugin;

	/**
	 * The constructor
	 */
	public AcutePlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static AcutePlugin getDefault() {
		return plugin;
	}

	public static void logError(Throwable t) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, t.getMessage(), t));
	}

	public static void logError(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	/**
	 * Calls {@link #getDotnetCommand(boolean)} with the <code>true</code> parameter
	 * enabling the command error dialogs to be shown
	 *
	 * @return Path to the <code>dotnet</code> command specified in the preferences
	 * @throws IllegalStateException
	 *             If no path has been set
	 */
	public static String getDotnetCommand() {
		return getDotnetCommand(true);
	}

	/**
	 * Used to retrieve the path to the <code>dotnet</code> command. If no path has
	 * been set, then a warning will be given allowing the opening the preferences
	 * view and throws the {@link IllegalStateException} exception
	 *
	 * @param showErrors if <code>true</code> and the set path is not valid, an error dialog will
	 * be shown pointing the user to the preferences to fix the problem
	 * @return Path to the <code>dotnet</code> command specified in the preferences
	 * @throws IllegalStateException
	 *             If no path has been set
	 */
	public static String getDotnetCommand(boolean showErrors) {
		String path = plugin.getPreferenceStore().getString(AcutePreferenceInitializer.explicitDotnetPathPreference);
		if (path.isEmpty()) {
			if(showErrors) {
				openCommandErrorDialog("No `dotnet` Path Set",
						"There is no path to the `dotnet` command, please specify the correct path in the preferences.");
			}
		} else {
			String version = DotnetVersionUtil.getVersion(path);
			if (!DotnetVersionUtil.isValidVersionFormat(version)) {
				if(showErrors) {
					openCommandErrorDialog("Invalid `dotnet` Path Set","`dotnet --version` failed to return a version, please specify the correct command path in the preferences.");
				}
			} else if (!DotnetVersionUtil.isValidVersionNumber(version)) {
				if(showErrors) {
					openCommandErrorDialog("Invalid `dotnet` Version","`dotnet` version 2.0 or greater is required, please specify a command path to a new version of `dotnet` in the preferences.");
				}
			} else {
				return path;
			}
		}
		throw new IllegalStateException();
	}

	private static void openCommandErrorDialog(String title, String content) {
		Display.getDefault().asyncExec(() -> {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			int dialogResponse = MessageDialog.open(MessageDialog.CONFIRM, shell, title,
					content, SWT.NONE, "Open Preferences", "Cancel");
			if (dialogResponse == 0) {
				PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(shell,
						AcutePreferencePage.PAGE_ID,
						new String[] { AcutePreferencePage.PAGE_ID }, null);
				preferenceDialog.setBlockOnOpen(false);
				preferenceDialog.open();
			}
		});
	}
}
