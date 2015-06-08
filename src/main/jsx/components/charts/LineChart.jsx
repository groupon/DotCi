import React from 'react';
import Chart from 'chart.js';
import  LoadingHelper from './../mixins/LoadingHelper.jsx';
require('./line_chart.css');
export default React.createClass({
  mixins:[LoadingHelper],
  componentDidUpdate(){
    this._renderChart();
  },
  componentDidMount(){
    this.componentDidUpdate();
  },
  _renderChart(){
    const chartCtx = this.refs.chart.getDOMNode().getContext('2d');
    const labels = this.props.chart.get('data').map(t => t.get('x')).toArray();
    const data= this.props.chart.get('data').map(t =>t.get('y')).toArray();
    var chartData = {
      labels ,
      datasets: [
        {
          label: this.props.name,
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
    this.chart = new Chart(chartCtx).Line(chartData, { scaleShowGridLines : false,legendTemplate: this._legend()});
    this.refs.legend.getDOMNode().innerHTML = this.chart.generateLegend();
    this.chart.update();
  },
  _onClick(e){
  },
  _render(){
    return (<div className="lineChart">
      <div className="align-center" ref="legend"></div>
      <canvas className="chart" ref='chart' onClick={this._onClick}/></div>);
  },
  _legend(){
    let {x,y} = this.props.chart.get('metadata').toObject();
    return `<div><h5>${this.props.name}</h5><div>X - ${x}</div><div> Y - ${y}</div></div>`
  }
})
