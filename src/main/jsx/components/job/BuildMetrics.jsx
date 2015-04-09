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
import Chart from 'chart.js';
require("./build_metrics.less");
export default React.createClass({
  componentWillMount(){
    const actions = this.props.flux.getActions('app');
    actions.getJobInfoFromServer('buildTimes[*]');
  },
  componentDidUpdate(){
    const buildTimesCtx = this.refs.buildTimes.getDOMNode().getContext('2d');
    const labels = this.props.buildTimes.map(t =>t.get('x')).toArray();
    const data= this.props.buildTimes.map(t =>t.get('y')).toArray();
    var chartData = {
      labels ,
      datasets: [
        {
          label: 'Build Times',
          fillColor: 'rgba(220,220,220,0.2)',
          strokeColor: 'rgba(220,220,220,1)',
          pointColor: 'rgba(0,0,0,1)',
          pointStrokeColor: '#fff',
          pointHighlightFill: '#fff',
          pointHighlightStroke: 'rgba(220,220,220,1)',
          data
        }
      ]
    };
    const chart = new Chart(buildTimesCtx).Line(chartData, { scaleShowGridLines : false,legendTemplate : "<div><h5>Build Times( successful master)</h5><div>X - Build Number</div><div> Y - Build Time (Mins)</div></div>"});
    this.refs.legend.getDOMNode().innerHTML = chart.generateLegend();
  },
  render(){
    return (<div id="build-metrics">
      <div ref="legend"></div>
      <canvas ref='buildTimes' width='600' height='400'></canvas></div>);
  }
});
