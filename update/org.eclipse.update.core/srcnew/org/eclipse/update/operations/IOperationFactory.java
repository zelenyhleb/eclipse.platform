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

package org.eclipse.update.operations;

import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;

/**
 * IOperation
 */
public interface IOperationFactory {
	public IOperation createConfigOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, IOperationListener listener);
	public IOperation createUnconfigOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, IOperationListener listener);
	public IOperation createInstallOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, FeatureHierarchyElement2[] optionalElements,IFeatureReference[] optionalFeatures, IVerificationListener verifier, IOperationListener listener);
	public IOperation createUninstallOperation(IInstallConfiguration config, IConfiguredSite targetSite, IFeature feature, IOperationListener listener);
	public IOperation createBatchInstallOperation(IInstallOperation[] operations);
}