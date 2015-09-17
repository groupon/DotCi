import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import LineChart from './../components/charts/LineChart.jsx';
export default React.createClass({
  render(){
    const metrics = this.props.buildMetrics.metrics;
    const charts = metrics.map(metric => <span key={metric.title} className="build-chart">
      <LineChart  title={metric.title} chart={metric.chart}/><hr/>
    </span>);
    return <div className="build-metrics">
      <span className="action-bar">
        <BranchTabs  ref="branchTabs" onTabChange={this._onTabChange} flux={this.props.flux} tabs={this.props.tabs} />
        <RangeSlider ref="buildCount" tooltip="Build count" queryParam="count" onChange={this._onCountChange} min={20}  max={100} step={5}  />
      </span>
      <div className="charts">{charts}</div>
    </div>;
  }
});
