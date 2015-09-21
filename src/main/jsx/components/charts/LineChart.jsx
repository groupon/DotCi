import React from 'react';
import Chart from 'chart.js';
import contains from 'ramda/src/contains'
import {remove} from './../../util/List.js';
require('./line_chart.css');
export default React.createClass({
  getInitialState(){
    return {unchecked: []};
  },
  componentDidUpdate(){
    this._renderChart();
  },
  componentDidMount(){
    this.componentDidUpdate();
  },
  _renderChart(){
    const {dataSets,labels} = this.props.chart;
    const chartCtx = this.refs.chart.getContext('2d');
    const filteredDataSet = this._removeUnchecked(dataSets);
    if(filteredDataSet.size == 0){
      var chartData = { labels :[], datasets: [] };
    }else{
      var chartData = { labels : labels, datasets: filteredDataSet };
      new Chart(chartCtx).Line(chartData, {scaleShowGridLines : false});
    }
  },
  _removeUnchecked(dataSets){
    return dataSets.filter(dataSet => !this._isUnChecked(dataSet.label));
  },
  render(){
    let {xLabel,yLabel} = this.props.chart;
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

    const {dataSets} = this.props.chart;
    return  <div className="legend">
      {dataSets.map(dataSet=> <div key={dataSet.label} >
        <div className="ui checkbox">
          <input  id={dataSet.label} type="checkbox" name={dataSet.label}  onChange={this._dataSetChanged} checked={this._isSelected(dataSet.label)}/>
          <label htmlFor={dataSet.label} style={{color: dataSet.strokeColor }} >{dataSet.label}</label>
        </div>
      </div>)}
    </div>
  },
  _isSelected(label){
    return !this._isUnChecked(label);
  },
  _isUnChecked(label){
    return contains(x => x === label) (this.state.unchecked)
  },
  _dataSetChanged(e){
    const targetLabel =e.target.getAttribute('name');
    const {dataSets} = this.props.chart;
    if(!e.target.checked) {
      this.state.unchecked.push(targetLabel)
      this.replaceState({unchecked: this.state.unchecked});
    }else{
      this.replaceState({unchecked: remove(targetLabel)(this.state.unchecked)});
    }
  }

})
