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
    const {dataSets,labels} = this.props.chart.toObject();
    const chartCtx = this.refs.chart.getDOMNode().getContext('2d');
    var chartData = { labels : labels.toJS(), datasets: dataSets.toJS() };
    this.chart = new Chart(chartCtx).Line(chartData, {scaleShowGridLines : false,legendTemplate: this._legend()});
    this.refs.legend.getDOMNode().innerHTML = this.chart.generateLegend();
    this.chart.update();
  },
  _onClick(e){
  },
  _render(){
    let {xLabel,yLabel} = this.props.chart.toObject();
    return (
      <div className="chart-container">
        <div  ref="legend"></div>
        <h5 className="axis-label yaxis-label">{yLabel}</h5>
        <span>
          <h4 className="align-center">{this.props.title}</h4>
          <canvas className="chart" ref='chart' onClick={this._onClick}/>
          <h5 className ="axis-label align-center">{xLabel}</h5>
        </span>
      </div>);
  },
  _legend(){
    return `<ul class=\"<%=name.toLowerCase()%>-legend\">
    <% for (var i=0; i<datasets.length; i++){%>
    <li>
    <span style=\"color:<%=datasets[i].strokeColor%>\">
    <%if(datasets[i].label){%>
    <%=datasets[i].label%>
    <%}%>
    </li></span>
    <%}%>
    </ul>`
  }
})
