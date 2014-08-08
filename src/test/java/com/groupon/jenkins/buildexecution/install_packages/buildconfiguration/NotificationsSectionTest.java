/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.buildexecution.install_packages.buildconfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.groupon.jenkins.buildexecution.install_packages.buildconfiguration.configvalue.ListValue;
import com.groupon.jenkins.notifications.PostBuildNotifier;

import static com.groupon.jenkins.testhelpers.TestHelpers.list;
import static com.groupon.jenkins.testhelpers.TestHelpers.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class NotificationsSectionTest {

	@Test
	public void should_create_create_notifiers_specified_without_any_options() {
		List<String> pusher_email_notifications = list("pusher_email");
		ListValue<String> value = new ListValue<String>(pusher_email_notifications);
		NotificationsSection notificationSection = spy(new NotificationsSection(value));
		Object emailNotifier = mock(PostBuildNotifier.class);
		doReturn(emailNotifier).when(notificationSection).createNotifier("pusher_email", new HashMap<String, String>());
		assertNotNull(notificationSection.getNotifiers());
		assertEquals(1, notificationSection.getNotifiers().size());
		assertTrue(notificationSection.getNotifiers().contains(emailNotifier));
	}

	@Test
	public void should_create_create_notifiers_specified_with_options() {
		List<Map> notifications = list(map("email", "chairman_meow@groupon.com"));
		ListValue<Map> value = new ListValue<Map>(notifications);
		NotificationsSection notificationSection = spy(new NotificationsSection(value));
		Object emailNotifier = mock(PostBuildNotifier.class);
		doReturn(emailNotifier).when(notificationSection).createNotifier("email", "chairman_meow@groupon.com");
		assertNotNull(notificationSection.getNotifiers());
		assertEquals(1, notificationSection.getNotifiers().size());
		assertTrue(notificationSection.getNotifiers().contains(emailNotifier));
	}

	@Test
	public void shouldnt_run_any_shell_commands() {
		assertEquals(ShellCommands.NOOP, new NotificationsSection(null).toScript(null));
	}
}
