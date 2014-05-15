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
package com.groupon.jenkins.dynamic.buildconfiguration;

import hudson.EnvVars;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static com.groupon.jenkins.testhelpers.TestHelpers.list;
import static com.groupon.jenkins.testhelpers.TestHelpers.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OneToTwoTranslatorTest {

	@Test
	public void should_not_do_any_translation_if_already_version2() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("environment", "something"), new EnvVars());
		assertFalse(translator.needsTranslation());

		translator = new OneToTwoTranslator(map("language", "ruby"), new EnvVars());
		assertTrue(translator.needsTranslation());
	}

	@Test
	public void should_move_language_into_enviroment_section() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby"), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertEquals("ruby", getEnvironment(config).get("language"));
	}

	@Test
	public void should_rename_nodejs_node() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "nodejs"), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertEquals("node", getEnvironment(config).get("language"));
	}

	@Test
	public void should_move_services_to_packages() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "nodejs", "services", list("mongo", "mysql")), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getEnvironment(config).get("packages"));
		assertNotNull("mongo", ((List<?>) getEnvironment(config).get("packages")).get(0));
		assertNotNull("mysql", ((List<?>) getEnvironment(config).get("packages")).get(1));
	}

	@Test
	public void should_move_ruby_version_to_language_version() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "ruby_version", "ruby-1.8.7"), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getEnvironment(config).get("language_versions"));
		assertEquals("1.8.7", ((List<?>) getEnvironment(config).get("language_versions")).get(0));
	}

	@Test
	public void should_move_ruby_versions_to_language_version() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "ruby_version", list("ruby-1.8.7", "ree-1.9.3")), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getEnvironment(config).get("language_versions"));
		assertEquals("1.8.7", ((List<?>) getEnvironment(config).get("language_versions")).get(0));
		assertEquals("ree-1.9.3", ((List<?>) getEnvironment(config).get("language_versions")).get(1));
	}

	@Test
	public void should_move_script_into_build_run() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "script", "rake test"), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getBuild(config));
		assertEquals("rake test", getBuild(config).get("run"));
	}

	@Test
	public void should_move_output_files_to_output_files_plugin_section() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "output_files", "blah"), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(config.get("plugins"));
		assertTrue(config.get("plugins") instanceof List);
	}

	@Test
	public void should_move_multiconfig_scripts_into_build_run() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "script", list(map("spec", "rake test"), map("cucumber", "echo cucumber"))), new EnvVars());
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getBuild(config));
		assertEquals("rake test", ((Map<?, ?>) getBuild(config).get("run")).get("spec"));
	}

	@Test
	public void should_move_notifications_section() {
		OneToTwoTranslator translator = new OneToTwoTranslator(map("language", "ruby", "notifications", map("master", map("email", "foo@example.com"))), new EnvVars("BRANCH", "master"));
		Map<?, ?> config = translator.getConfig();
		assertNotNull(getBuild(config));
		Object notifications = config.get("notifications");
		assertNotNull(notifications);
		assertTrue(notifications instanceof List);
		assertTrue(((List<?>) notifications).get(0) instanceof Map);
		assertEquals("foo@example.com", ((Map<?, ?>) ((List<?>) notifications).get(0)).get("email"));
	}

	private Map<?, ?> getBuild(Map<?, ?> config) {
		return (Map<?, ?>) config.get("build");
	}

	private Map<?, ?> getEnvironment(Map<?, ?> config) {
		return (Map<?, ?>) config.get("environment");
	}

}
