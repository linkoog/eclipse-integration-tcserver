/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.eclipse.wst.server.ui.wizard.WizardFragment;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TemplateProperty;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.ui.TcServer21InstanceCreationFragment.InstanceConfiguration;

/**
 * @author Tomasz Zarna
 *
 */
public class TcServerTemplateConfigurationFragment extends WizardFragment {

	public static final String ENTER_VALUE = "Enter a value for all required properties.";

	final private String templateName;

	private IWizardHandle wizardHandle;

	private final List<TemplateProperty> properties;

	public TcServerTemplateConfigurationFragment(String templateName, List<TemplateProperty> properties) {
		Assert.isNotNull(templateName);
		Assert.isNotNull(properties);
		Assert.isLegal(!properties.isEmpty());
		this.templateName = templateName;
		this.properties = properties;
		setComplete(checkIfAllPropertiesHaveDefaultValues());
	}

	private boolean checkIfAllPropertiesHaveDefaultValues() {
		if (properties != null) {
			for (TemplateProperty prop : properties) {
				if (prop.getRawDefault() == null) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void enter() {
		validate();
	}

	@Override
	public void exit() {
		updateModelProperties();
	}

	private void updateModelProperties() {
		InstanceConfiguration model = (InstanceConfiguration) getTaskModel().getObject(
				TcServer21InstanceCreationFragment.INSTANCE_CONFIGURATION);
		if (model == null) {
			return;
		}
		if (model.templateProperties.isEmpty()) {
			model.templateProperties = properties;
		}
		else {
			model.templateProperties.addAll(properties);
		}
	}

	private void validate() {
		boolean errorFound = false;
		for (TemplateProperty prop : properties) {
			if (prop.getValue() == null || prop.getValue().isEmpty()) {
				wizardHandle.setMessage(TcServerTemplateConfigurationFragment.ENTER_VALUE, IMessageProvider.ERROR);
				errorFound = true;
			}
		}
		if (!errorFound) {
			wizardHandle.setMessage(null, IMessageProvider.NONE);
		}
		setComplete(wizardHandle.getMessage() == null);
		wizardHandle.update();
	}

	@Override
	public boolean hasComposite() {
		return true;
	}

	@Override
	public Composite createComposite(Composite parent, IWizardHandle handle) {
		this.wizardHandle = handle;

		handle.setTitle("Template Configuration");
		handle.setDescription("Specify template properties.");
		handle.setImageDescriptor(TcServerImages.WIZB_SERVER);

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		Label templateNameLabel = new Label(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(templateNameLabel);
		templateNameLabel.setText("Enter properties for template " + templateName + ":");

		for (TemplateProperty prop : properties) {
			Label message = new Label(composite, SWT.NONE);
			message.setText(prop.getMessage());

			final Text value = new Text(composite, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(value);
			value.setText(prop.getDefault());
			value.setData(prop);
			value.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					((TemplateProperty) value.getData()).setValue(value.getText());
					validate();
				}
			});
		}

		Dialog.applyDialogFont(composite);
		return composite;
	}
}