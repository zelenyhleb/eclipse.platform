package org.eclipse.team.tests.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.target.IRemoteTargetResource;
import org.eclipse.team.core.target.Site;
import org.eclipse.team.core.target.TargetManager;
import org.eclipse.team.core.target.TargetProvider;

public class TeamTest extends EclipseWorkspaceTest {
	
	protected static final IProgressMonitor DEFAULT_PROGRESS_MONITOR = new NullProgressMonitor();
	
	Properties properties;
	
	public TeamTest() {
		super();
	}
	public TeamTest(String name) {
		super(name);
	}
	
	protected IProject getNamedTestProject(String name) throws CoreException {
		IProject target = getWorkspace().getRoot().getProject(name);
		if (!target.exists()) {
			target.create(null);
			target.open(null);
		}
		assertExistsInFileSystem(target);
		return target;
	}
	
	protected IProject getUniqueTestProject(String prefix) throws CoreException {
		// manage and share with the default stream create by this class
		return getNamedTestProject(prefix + "-" + Long.toString(System.currentTimeMillis()));
	}
	
	protected IStatus getTeamTestStatus(int severity) {
		return new Status(severity, "org.eclipse.team.tests.core", 0, "team status", null);
	}
		/**
	 * Retrieves the Site object that the TargetProvider is contained in.
	 * @return Site
	 */
	Site getSite() {
		try {
			URL url = new URL(properties.getProperty("location"));
			return TargetManager.getSite(properties.getProperty("target"), url);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	/**
	 * Creates filesystem 'resources' with the given names and fills them with random text.
	 * @param container An object that can hold the newly created resources.
	 * @param hierarchy A list of files & folder names to use as resources
	 * @param includeContainer A flag that controls whether the container is included in the list of resources.
	 * @return IResource[] An array of resources filled with variable amounts of random text
	 * @throws CoreException
	 */
	public IResource[] buildResources(IContainer container, String[] hierarchy, boolean includeContainer) throws CoreException {
		List resources = new ArrayList(hierarchy.length + 1);
		if (includeContainer)
			resources.add(container);
		resources.addAll(Arrays.asList(buildResources(container, hierarchy)));
		IResource[] result = (IResource[]) resources.toArray(new IResource[resources.size()]);
		ensureExistsInWorkspace(result, true);
		for (int i = 0; i < result.length; i++) {
			if (result[i].getType() == IResource.FILE) // 3786 bytes is the average size of Eclipse Java files!
				 ((IFile) result[i]).setContents(getRandomContents(100), true, false, null);
		}
		return result;
	}
	public IResource[] buildEmptyResources(IContainer container, String[] hierarchy, boolean includeContainer) throws CoreException {
		List resources = new ArrayList(hierarchy.length + 1);
		resources.addAll(Arrays.asList(buildResources(container, hierarchy)));
		if (includeContainer)
			resources.add(container);
		IResource[] result = (IResource[]) resources.toArray(new IResource[resources.size()]);
		ensureExistsInWorkspace(result, true);
		return result;
	}
	/**
	 * Creates an InputStream filled with random text in excess of a specified minimum.
	 * @param sizeAtLeast 	The minimum number of chars to fill the input stream with.
	 * @return InputStream The input stream containing random text.
	 */
	protected static InputStream getRandomContents(int sizeAtLeast) {
		StringBuffer randomStuff = new StringBuffer(sizeAtLeast + 100);
		while (randomStuff.length() < sizeAtLeast) {
			randomStuff.append(getRandomSnippet());
		}
		return new ByteArrayInputStream(randomStuff.toString().getBytes());
	}
	/**
	 * Produces a random chunk of text from a finite collection of pre-written phrases.
	 * @return String Some random words.
	 */
	public static String getRandomSnippet() {
		switch ((int) Math.round(Math.random() * 10)) {
			case 0 :
				return "este e' o meu conteudo (portuguese)";
			case 1 :
				return "Dann brauchen wir aber auch einen deutschen Satz!";
			case 2 :
				return "I'll be back";
			case 3 :
				return "don't worry, be happy";
			case 4 :
				return "there is no imagination for more sentences";
			case 5 :
				return "customize yours";
			case 6 :
				return "foo";
			case 7 :
				return "bar";
			case 8 :
				return "foobar";
			case 9 :
				return "case 9";
			default :
				return "these are my contents";
		}
	}
	public TargetProvider createProvider(IProject project) throws TeamException {
		// Ensure the remote folder exists
		IRemoteTargetResource remote = getSite().getRemoteResource().getFolder(
			new Path(properties.getProperty("test_dir")).append(project.getName()).toString());
		if (! remote.exists(null)) {
			remote.mkdirs(null);
		}
		TargetManager.map(project, getSite(), new Path(properties.getProperty("test_dir")).append(project.getName()));
		TargetProvider target = getProvider(project);
		return target;
	}
	
	public TargetProvider getProvider(IProject project) throws TeamException {
		return TargetManager.getProvider(project);
	}
	
	public void sleep(int ms) {
		try {
			Thread.currentThread().sleep(ms);
		} catch (InterruptedException e) {
			System.err.println("Testing was rudely interrupted.");
		}
	}
	void assertLocalEqualsRemote(IProject project) throws CoreException, TeamException {
		IProject newProject = getNamedTestProject("equals");
		TargetProvider target = TargetManager.getProvider(project);
		IResource[] localResources = project.members();
		for (int i = 0; i < localResources.length; i++) {
			assertEquals(target.getRemoteResourceFor(localResources[i]), localResources[i]);
		}
	}
	// Assert that the two containers have equal contents
	protected void assertEquals(IRemoteResource container1, IResource container2) throws CoreException, TeamException {
		if (container2.getType() == IResource.FILE) {
			// Ignore .project file
			if (container2.getName().equals(".project")) return;
			assertTrue(compareContent(container1.getContents(null), ((IFile) container2).getContents()));
		} else {
			IRemoteResource[] remoteResources = container1.members(null);
			IResource[] localResources = ((IFolder) container2).members();
			for (int i = 0; i < localResources.length; i++) {
				assertEquals(remoteResources[i],localResources[i]);
			}
		}
	}
}
