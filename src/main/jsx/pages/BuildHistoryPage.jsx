import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
require('./build_history_page.css');
export default React.createClass({
  render(){
    const {query,builds,actions,dirty} = this.props.buildHistory;
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
      const progressBar = dirty?  <paper-progress  indeterminate ></paper-progress> : <span/>
      const buildHistoryTable = builds?<BuildHistoryTable 
        textFilter={query.textFilter} 
        grouped={query.grouped} 
        buildFilters={buildFilters} 
        countSlider={countSlider} 
        queryChangeAction={QueryChange}
        builds ={builds}/> :<span/>;

        return(<span>
          {progressBar}
          {buildHistoryTable}
          <div className="align-center" >
          </div>
        </span>);
  }
});
