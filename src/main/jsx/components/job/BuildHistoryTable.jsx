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

import React from 'react';
import BuildRow from './BuildRow.jsx';
import FilterBar from './../FilterBar.jsx';
import GroupedBuildsView from './GroupedBuildsView.jsx';
import LinearBuildsView from './LinearBuildsView.jsx';
import ToggleButton from './../lib/ToggleButton.jsx';
import RangeSlider from './../lib/RangeSlider.jsx';
require('./build_history.css');
export default React.createClass({
  getDefaultProps(){
    return { grouped: false};
  },
  render(){
    return(
      <div>
        <span className="buildHistory-bar" >
          {this.props.buildFilters}
          {this.props.countSlider}
          <ToggleButton checked={this.props.grouped} tooltip="Pipeline View" onClick={this._onPipelineViewChange}></ToggleButton>
        </span>
        {this.props.grouped? <GroupedBuildsView builds={this.props.builds} /> : <LinearBuildsView builds={this.props.builds} />}
      </div>
    );
  },
  _onPipelineViewChange(grouped){
    this.props.queryChangeAction({grouped});
  },
  _onFilterChange(textFilter){
    this.props.queryChangeAction({textFilter});
  }
});

