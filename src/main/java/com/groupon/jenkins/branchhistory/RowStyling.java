/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.groupon.jenkins.branchhistory;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import hudson.model.Result;

public enum  RowStyling {
    IN_PROGRESS("#fffcf4", "octicon-primitive-dot","#E7D100"),
    SUCCESS("#fafffa", "octicon-check","#038035"),
    FAILURE("snow","octicon-x","#c00"),
    ABORTED("#fdfdfd","octicon-stop","#666")
    ;

    RowStyling(String backgroundColor, String statusIconFont, String statusIconFontColor) {
        this.backgroundColor = backgroundColor;
        this.statusIconFont = statusIconFont;
        this.statusIconFontColor = statusIconFontColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public String getStatusIconFont() {
        return statusIconFont;
    }

    public String getStatusIconFontColor() {
        return statusIconFontColor;
    }

    private String backgroundColor;
    private String statusIconFont;
    private String statusIconFontColor;

    public static RowStyling get(DynamicBuild build) {
        if(build.isBuilding()){
            return RowStyling.IN_PROGRESS;
        }
        if( Result.SUCCESS.equals( build.getResult())){
            return RowStyling.SUCCESS;

        }
        if(Result.FAILURE.equals(build.getResult())){
            return RowStyling.FAILURE;
        }
        return RowStyling.ABORTED;
    }

}
