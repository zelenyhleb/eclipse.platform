/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.variants;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * A resource comparator that uses the <code>ThreeWaySynchronizer</code>
 * to compare local resources to their resource variants. The local state
 * is determined using the local modification state and the remote state
 * is determined by comparing the base bytes to the remote bytes obtained
 * from the synchronizer.
 * 
 * @since 3.0
 */
public class ThreeWayResourceComparator implements IResourceVariantComparator {
	
	private ThreeWaySynchronizer synchronizer;
	
	/**
	 * Create a three-way resource comparator that uses the <code>ThreeWaySynchronizer</code>
	 * to compare a local resource to a resource variant.
	 * @param synchronizer
	 */
	public ThreeWayResourceComparator(ThreeWaySynchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#compare(org.eclipse.core.resources.IResource, org.eclipse.team.core.variants.IResourceVariant)
	 */
	public boolean compare(IResource local, IResourceVariant remote) {
		// First, ensure the resources are the same gender
		if ((local.getType() == IResource.FILE) == remote.isContainer()) {
			return false;
		}
		try {
			// If the file is locally modified, it cannot be in sync
			if (local.getType() == IResource.FILE && getSynchronizer().isLocallyModified(local)) {
				return false;
			}
			// If there is no base, the local cannopt match the remote
			if (getSynchronizer().getBaseBytes(local) == null) return false;
			// Otherwise, assume they are the same if the remote equals the base
			return equals(getSynchronizer().getBaseBytes(local), getBytes(remote));
		} catch (TeamException e) {
			TeamPlugin.log(e);
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#compare(org.eclipse.team.core.variants.IResourceVariant, org.eclipse.team.core.variants.IResourceVariant)
	 */
	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		byte[] bytes1 = getBytes(base);
		byte[] bytes2 = getBytes(remote);
		return equals(bytes1, bytes2);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.variants.IResourceVariantComparator#isThreeWay()
	 */
	public boolean isThreeWay() {
		return true;
	}
	
	private ThreeWaySynchronizer getSynchronizer() {
		return synchronizer;
	}
	
	private byte[] getBytes(IResourceVariant remote) {
		return remote.asBytes();
	}
	
	private boolean equals(byte[] syncBytes, byte[] oldBytes) {
		if (syncBytes.length != oldBytes.length) return false;
		for (int i = 0; i < oldBytes.length; i++) {
			if (oldBytes[i] != syncBytes[i]) return false;
		}
		return true;
	}
}
