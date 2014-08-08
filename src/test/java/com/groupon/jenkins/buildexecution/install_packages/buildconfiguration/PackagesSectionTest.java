package com.groupon.jenkins.buildexecution.install_packages.buildconfiguration;

import hudson.matrix.Combination;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.groupon.jenkins.buildexecution.install_packages.buildconfiguration.configvalue.ListOrSingleValue;
import com.groupon.jenkins.buildexecution.install_packages.buildconfiguration.configvalue.StringValue;

import static org.junit.Assert.assertEquals;

public class PackagesSectionTest {

	@Test
	public void should_not_install_language_packages_if_language_is_unknown() {
		ListOrSingleValue<String> packageValue = new ListOrSingleValue<String>("package1");
		PackagesSection packagesSection = new PackagesSection(packageValue, new LanguageSection(new StringValue("unknown")), new LanguageVersionsSection(new ListOrSingleValue<String>("version1")));
		String installPackagesScript = packagesSection.getInstallPackagesScript(new Combination(ImmutableMap.of("one", "two")));
		assertEquals("install_packages package1", installPackagesScript);
	}
}
