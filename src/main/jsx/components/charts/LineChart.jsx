import React from 'react';
import Chart from 'chart.js';
export default React.createClass({
  xcomponentDidUpdate(){
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
})
