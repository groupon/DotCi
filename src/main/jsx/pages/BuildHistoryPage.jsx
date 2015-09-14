import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
export default React.createClass({
  render(){
    const {filters,builds,filterChange,buildCountChange} = this.props.buildHistory;
    const countSlider = <RangeSlider ref="buildCount" tooltip="Build count"  onChange={buildCountChange} min={20}  max={100} step={5}  />;
    const buildFilters = <BuildFilters  ref="branchTabs" onFilterChange={filterChange}  filters={filters} />;
    return(<div className="align-center" >
      <BuildHistoryTable buildFilters={buildFilters} countSlider={countSlider} builds ={builds}/>
    </div>);
  }
});
