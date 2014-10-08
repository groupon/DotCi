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
package com.groupon.jenkins.dynamic.build;

import hudson.model.Result;

public class CurrentBuildState {

	private final Result result;
	private final String state;

	public CurrentBuildState(String state, Result result) {
		this.state = state;
		this.result = result;
	}

	public boolean isBuilding() {
		return state == null ? false : State.valueOf(state).compareTo(State.POST_PRODUCTION) < 0;
	}

	public Result getResult() {
		return this.result;
	}

	private static enum State {
		/**
		 * Build is created/queued but we haven't started building it.
		 */
		NOT_STARTED,
		/**
		 * Build is in progress.
		 */
		BUILDING,
		/**
		 * Build is completed now, and the status is determined, but log files
		 * are still being updated.
		 */
		POST_PRODUCTION,
		/**
		 * Build is completed now, and log file is closed.
		 */
		COMPLETED
	}

}
