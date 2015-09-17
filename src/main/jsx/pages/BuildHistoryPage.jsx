import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
export default React.createClass({
  render(){
    const {query,builds,actions} = this.props.buildHistory;
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
        filters={this.props.buildHistory.filters}
        actions= {{AddFilter,RemoveFilter,QueryChange}}
      />;
      return(<div className="align-center" >
        <BuildHistoryTable buildFilters={buildFilters} countSlider={countSlider} builds ={builds}/>
      </div>);
  }
});
