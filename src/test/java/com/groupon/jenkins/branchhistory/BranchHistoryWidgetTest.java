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
package com.groupon.jenkins.branchhistory;

import hudson.widgets.HistoryWidget;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DbBackedBuild;
import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.repository.DynamicBuildRepository;

import static com.groupon.jenkins.testhelpers.DynamicBuildFactory.newBuild;
import static com.groupon.jenkins.testhelpers.TestHelpers.list;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class BranchHistoryWidgetTest {

	private DynamicBuildRepository buildRepo;
	private DynamicProject project;
	private BranchHistoryWidgetModel<DbBackedBuild> historyWidget;

	@Before
	public void setup_mocks() {
		buildRepo = mock(DynamicBuildRepository.class);
		project = mock(DynamicProject.class);
		historyWidget = new BranchHistoryWidgetModel<DbBackedBuild>(project, buildRepo, null);
	}

	@Test
	public void should_not_show_skipped_builds() {
		DynamicBuild unSkippedBuild = newBuild().get();
		DynamicBuild skippedBuild = newBuild().skipped().get();
		when(skippedBuild.isSkipped()).thenReturn(true);
		List<? extends DbBackedBuild> builds = list(skippedBuild, unSkippedBuild);
		when(buildRepo.getLast(project, BranchHistoryWidget.BUILD_COUNT, null)).thenReturn((Iterable<DbBackedBuild>) builds);
		assertEquals(1, Iterables.size(historyWidget.getBaseList()));
		assertEquals(unSkippedBuild, Iterables.getOnlyElement(historyWidget.getBaseList()));
	}

	@Test
	public void should_return_oldest_running_build() {
		DynamicBuild finishedBuild = newBuild().get();
		DynamicBuild finishedBuild2 = newBuild().get();
		DynamicBuild runningBuild = newBuild().building().get();
		DynamicBuild runningBuild2 = newBuild().building().get();
		List<? extends DbBackedBuild> builds = list(finishedBuild, runningBuild, finishedBuild2, runningBuild2);
		String buildId = String.valueOf(runningBuild2.getNumber());
		when(buildRepo.getLast(project, BranchHistoryWidget.BUILD_COUNT, null)).thenReturn((Iterable<DbBackedBuild>) builds);

		assertEquals(buildId, historyWidget.getNextBuildToFetch((Iterable<DbBackedBuild>) builds, adapter).getBuildNumber(adapter));

	}

	@Test
	public void should_return_most_recent_build_plus_one() {
		DynamicBuild finishedBuild = newBuild().get();
		DynamicBuild finishedBuild2 = newBuild().get();
		DynamicBuild finishedBuild3 = newBuild().get();
		List<? extends DbBackedBuild> builds = list(finishedBuild, finishedBuild2, finishedBuild3);
		String buildId = String.valueOf(finishedBuild.getNumber() + 1);

		when(buildRepo.getLast(project, BranchHistoryWidget.BUILD_COUNT, null)).thenReturn((Iterable<DbBackedBuild>) builds);

		assertEquals(buildId, historyWidget.getNextBuildToFetch((Iterable<DbBackedBuild>) builds, adapter).getBuildNumber(adapter));

	}

	@Test
	public void should_return_one_for_empty_build_list() {
		String buildId = "1";
		List<? extends DbBackedBuild> builds = Collections.emptyList();
		when(buildRepo.getLast(project, BranchHistoryWidget.BUILD_COUNT, null)).thenReturn((Iterable<DbBackedBuild>) builds);

		assertEquals(buildId, historyWidget.getNextBuildToFetch((Iterable<DbBackedBuild>) builds, adapter).getBuildNumber(adapter));

	}

	private static HistoryWidget.Adapter adapter = new HistoryWidget.Adapter<DbBackedBuild>() {
		@Override
		public int compare(DbBackedBuild record, String key) {
			return 0;
		}

		@Override
		public String getKey(DbBackedBuild record) {
			return String.valueOf(record.getNumber());
		}

		@Override
		public boolean isBuilding(DbBackedBuild record) {
			return record.isBuilding();
		}

		@Override
		public String getNextKey(String key) {
			return null;
		}
	};

}
