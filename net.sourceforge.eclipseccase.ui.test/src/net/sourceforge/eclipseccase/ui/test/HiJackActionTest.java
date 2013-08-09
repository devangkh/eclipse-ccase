package net.sourceforge.eclipseccase.ui.test;

import static org.junit.Assert.*;
import net.sourceforge.eclipseccase.ClearCaseProvider;
import net.sourceforge.eclipseccase.ui.actions.HijackAction;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.eclipse.core.resources.IResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Tests the HiJackAction class.
 * http://easymock.org/EasyMock3_0_Documentation.html
 * 
 * @author eraonel
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ HijackAction.class })
public class HiJackActionTest extends EasyMockSupport {
	
	//TODO: This class is not ready yet. Check forum.
	//https://groups.google.com/forum/#!forum/powermock
	
	// mock instance
	private IResource resourceMock;
	private HijackAction partialMockedHijackAction;
	private ClearCaseProvider clearcaseProviderMock;
	private IResource [] resources;

	@Before
	public void setup() throws Exception {
		resourceMock = EasyMock.createMock(IResource.class);
		clearcaseProviderMock = EasyMock.createMock(ClearCaseProvider.class);
	}

	@Test
	public void testIsEnabledWhenFileIsNotHijacked() throws Exception {
		
		partialMockedHijackAction = PowerMock.createPartialMock(
				HijackAction.class, "getSelectedResources");
		resources = new IResource[] { resourceMock };
		PowerMock.expectPrivate(partialMockedHijackAction,
				"getSelectedResources").andReturn(resources);
				
		EasyMock.expect(clearcaseProviderMock.getClearCaseProvider(EasyMock
				.anyObject(IResource.class)));
		
		//Could not use use isA(IResource.class since I got 
		EasyMock.expect(clearcaseProviderMock.isUnknownState(resourceMock)).andReturn(false);
				
		EasyMock.expect(clearcaseProviderMock.isIgnored(resourceMock)).andReturn(false);
				
		EasyMock.expect(clearcaseProviderMock.isClearCaseElement(resourceMock)).andReturn(false);
				
		EasyMock.expect(clearcaseProviderMock.isHijacked(resourceMock)).andReturn(false);

		// switch to replay state ( use interface) for all mocks.
		replayAll();
		// test that isEnabled returns true
		assertTrue(partialMockedHijackAction.isEnabled());
		// verify that specified behaviour has been used all mocks at once
		verifyAll();

	}

}
