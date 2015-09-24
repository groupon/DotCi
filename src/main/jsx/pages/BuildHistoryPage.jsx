import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
require('./build_history_page.css');
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
      const progressBar = this.props.buildHistory.dirty?  <paper-progress  indeterminate ></paper-progress> : <span/>
      return(<span>
        {progressBar}
        <div className="align-center" >
          <BuildHistoryTable buildFilters={buildFilters} countSlider={countSlider} builds ={builds}/>
        </div>
      </span>);
  }
});
