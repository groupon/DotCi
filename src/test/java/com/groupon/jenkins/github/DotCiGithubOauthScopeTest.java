package com.groupon.jenkins.github;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.groupon.jenkins.SetupConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DotCiGithubOauthScopeTest {

	private DotCiGitHubOAuthScope dotCiScope;
	private SetupConfig setupConfig;

	@Before
	public void setup() {
		dotCiScope = spy(new DotCiGitHubOAuthScope());
		setupConfig = mock(SetupConfig.class);
		doReturn(setupConfig).when(dotCiScope).getSetupConfig();
	}

	@Test
	public void should_ask_for_private_repo_permssions_on_if_setup_in_config() {
		when(setupConfig.hasPrivateRepoSupport()).thenReturn(true);
		Collection<String> scopes = dotCiScope.getScopesToRequest();
		assertTrue(scopes.contains("repo"));
	}

	@Test
	public void should_not_ask_for_private_repo_permssions_on_if_not_setup_in_config() {
		when(setupConfig.hasPrivateRepoSupport()).thenReturn(false);
		Collection<String> scopes = dotCiScope.getScopesToRequest();
		assertFalse(scopes.contains("repo"));
	}

}
