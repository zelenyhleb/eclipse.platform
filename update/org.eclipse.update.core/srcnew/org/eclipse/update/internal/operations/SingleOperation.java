/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.operations;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.operations.*;


public abstract class SingleOperation extends Operation implements ISingleOperation {
	
	protected IFeature feature;
	protected IFeature oldFeature;
	protected IInstallConfiguration config;
	protected IConfiguredSite targetSite;

//	private boolean optionalDelta;
	
	
	public SingleOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, IOperationListener listener) {
		super(listener);
		this.feature = feature;
		this.config = config;
		this.targetSite = targetSite;
	}

	public IFeature getFeature() {
		return feature;
	}
	
	public IFeature getOldFeature() {
		return oldFeature;
	}
	
//	public boolean isOptionalDelta() {
//		return optionalDelta;
//	}
	
	public IConfiguredSite getTargetSite() {
		return targetSite;
	}
	
	public IInstallConfiguration getInstallConfiguration() {
		return config;
	}
		
	void ensureUnique()
		throws CoreException {
		
		// Only need to check features that patch other features.	
		boolean patch = false;
		if (targetSite == null)
			targetSite = feature.getSite().getCurrentConfiguredSite();
		IImport[] imports = feature.getImports();
		for (int i = 0; i < imports.length; i++) {
			if (imports[i].isPatch()) {
				patch = true;
				break;
			}
		}
		if (!patch)
			return;
			
		IFeature localFeature =
			UpdateManager.getLocalFeature(targetSite, feature);
		ArrayList oldFeatures = new ArrayList();
		// First collect all older active features that
		// have the same ID as new features marked as 'unique'.
		UpdateManager.collectOldFeatures(localFeature, targetSite, oldFeatures);
		// Now unconfigure old features to enforce uniqueness
		for (int i = 0; i < oldFeatures.size(); i++) {
			IFeature oldFeature = (IFeature) oldFeatures.get(i);
			unconfigure(config, oldFeature);
		}
	}

	static void configure(IInstallConfiguration config, IFeature feature)
		throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			site.configure(feature);
		}
	}

	static boolean unconfigure(IInstallConfiguration config, IFeature feature)
		throws CoreException {
		IConfiguredSite site = UpdateManager.getConfigSite(feature, config);
		if (site != null) {
			PatchCleaner2 cleaner = new PatchCleaner2(site, feature);
			boolean result = site.unconfigure(feature);
			cleaner.dispose();
			return result;
		}
		return false;
	}
}
