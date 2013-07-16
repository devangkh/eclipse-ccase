/**
 * 
 */
package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.eclipseccase.ui.wizards.label.LabelData;

import org.easymock.EasyMock;
import org.eclipse.core.resources.IResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author eraonel
 *
 */
public class LabelWizardTest {
	
	private LabelData dataMock;
	private IResource resourceMock;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		dataMock = EasyMock.createMock(LabelData.class);
		resourceMock = EasyMock.createMock(IResource.class);
		
	}
	
	@Test
	public void testDoFinish() throws Exception{
		IResource [] resources = new IResource [] {resourceMock};
		EasyMock.expect(dataMock.getResource()).andReturn(resources);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
