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
package org.eclipse.update.internal.search;

import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

public interface IUpdateSiteAdapter {
	
	public String getLabel();
	public URL getURL();
	public ISite getSite(IProgressMonitor monitor);

}

