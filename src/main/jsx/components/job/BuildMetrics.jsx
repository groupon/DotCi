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
import BranchTabs from './BranchTabs.jsx';
import RangeSlider from './../lib/RangeSlider.jsx';

export default React.createClass({
  componentDidMount(){
    this._loadBuildMetrics(this.refs.branchTabs.currentTab(),this.refs.buildCount.value());
  },
  _loadBuildMetrics(branchTab,buildCount){
    const actions = this.props.flux.getActions('app');
    actions.clearJobInfo('metrics');
    actions.getJobInfoFromServer('buildHistoryTabs,metrics[name,chart[type,data[*],metadata]]',branchTab,buildCount);
  },
  render(){
    const charts = this.props.metrics?this.props.metrics.map(metric => <LineChart key={metric.get('name')} name={metric.get('name')} chart={metric.get('chart')}/>) : <div/>;
    return <div className="align-center">
      <BranchTabs  ref="branchTabs" onTabChange={this._onTabChange} flux={this.props.flux} tabs={this.props.tabs} defaultTab={'All'}/>
      <RangeSlider ref="buildCount" tooltip="Build count" queryParam="count" onChange={this._onCountChange} min={20}  max={100} step={5}  />
      {charts}
    </div>;
  },
  _onTabChange(tab){
    this._loadBuildMetrics(this.refs.branchTabs.currentTab(),this.refs.buildCount.value());
  },
  _onCountChange(count){
    this._loadBuildMetrics(this.refs.branchTabs.currentTab(),this.refs.buildCount.value());
  }
});
