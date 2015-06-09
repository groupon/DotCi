import React from 'react';
import Chart from 'chart.js';
import  LoadingHelper from './../mixins/LoadingHelper.jsx';
import {Set} from 'immutable';
require('./line_chart.css');
export default React.createClass({
  mixins:[LoadingHelper],
  getInitialState(){
    return {unchecked: Set()};
  },
  componentDidUpdate(){
    this._renderChart();
  },
  componentDidMount(){
    this.componentDidUpdate();
  },
  _renderChart(){
    const {dataSets,labels} = this.props.chart.toObject();
    const chartCtx = this.refs.chart.getDOMNode().getContext('2d');
    const filteredDataSet = this._removeUnchecked(dataSets);
    if(filteredDataSet.size == 0){
      var chartData = { labels :[], datasets: [] };
    }else{
      var chartData = { labels : labels.toJS(), datasets: filteredDataSet.toJS() };
      new Chart(chartCtx).Line(chartData, {scaleShowGridLines : false});
    }
  },
  _removeUnchecked(dataSets){
    return dataSets.filter(dataSet => !this.state.unchecked.has(dataSet.get('label')));
  },
  _render(){
    let {xLabel,yLabel} = this.props.chart.toObject();
    return (
      <div className="chart-container">
        <div>
          {this._legend()}
        </div>
        <h5 className="axis-label yaxis-label">{yLabel}</h5>
        <span>
          <h4 className="align-center">{this.props.title}</h4>
          <canvas className="chart" ref='chart' onClick={this._onClick}/>
          <h5 className ="axis-label align-center">{xLabel}</h5>
        </span>
      </div>);
  },
  _legend(){
    const {dataSets} = this.props.chart.toObject();
    return  <div className="legend">
      {dataSets.map(dataSet=> <div key={dataSet.get('label')} style={{color: dataSet.get('strokeColor') }} >
        <input type="checkbox" name={dataSet.get('label')}  onChange={this._dataSetChanged} checked={this._isSelected(dataSet.get('label'))}/>{dataSet.get('label')}
      </div>)}
    </div>
  },
  _isSelected(label){
    return !this.state.unchecked.has(label);
  },
  _dataSetChanged(e){
    const targetLabel =e.target.getAttribute('name');
    const {dataSets} = this.props.chart.toObject();
    if(!e.target.checked) {
      this.replaceState({unchecked: this.state.unchecked.add(targetLabel)});
    }else{
      this.replaceState({unchecked: this.state.unchecked.delete(targetLabel)});
    }
  }

})
