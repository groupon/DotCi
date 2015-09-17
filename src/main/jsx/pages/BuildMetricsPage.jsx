import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import LineChart from './../components/charts/LineChart.jsx';
export default React.createClass({
  render(){
    const {metrics,filters,actions,query} = this.props.buildMetrics;
    const {AddFilter,RemoveFilter,QueryChange} = actions;
    const countSlider = <RangeSlider
      tooltip="Build count"  
      value={query.limit} 
      onChange={(limit)=> QueryChange({limit})} 
      min={20}  
      max={100} 
      step={5}  />;

      const buildFilters = <BuildFilters  
        selectedFilter={query.filter} 
        filters={filters}
        actions= {{AddFilter,RemoveFilter,QueryChange}}
      />;
      const charts = metrics.map(metric => <span key={metric.title} className="build-chart">
        <LineChart  title={metric.title} chart={metric.chart}/><hr/>
      </span>);
      return <div className="build-metrics">
        <span className="action-bar">
          {buildFilters}
          {countSlider}
        </span>
        <div className="charts">{charts}</div>
      </div>;
  }
});
