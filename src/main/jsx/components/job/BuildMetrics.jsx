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
import LineChart from './../charts/LineChart.jsx';
import LoadingHelper from './../mixins/LoadingHelper.jsx';
import BranchTabs from './BranchTabs.jsx';

export default React.createClass({
  mixins: [LoadingHelper],
  componentWillMount(){
    const actions = this.props.flux.getActions('app');
    actions.getJobInfoFromServer('buildHistoryTabs,metrics[name,chart[type,data[*],metadata]]');
  },
  _render(){
    return <div className="align-center">
      <BranchTabs  onTabChange={this._onTabChange} flux={this.props.flux} tabs={this.props.tabs} defaultTab={'All'}/>
      { this.props.metrics.map(metric => <LineChart key={metric.get('name')} name={metric.get('name')} chart={metric.get('chart')}/>)}
    </div>;
  }
});
