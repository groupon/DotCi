import React from "react";
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
import ProgressBar from './../components/lib/ProgressBar.jsx';
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
      const buildHistoryTable = builds?<BuildHistoryTable 
        grouped={query.grouped} 
        buildFilters={buildFilters} 
        countSlider={countSlider} 
        queryChangeAction={QueryChange}
        builds ={builds}/> :<span/>;

        return(<span>
          <ProgressBar visible={dirty} />
          {buildHistoryTable}
          <div className="align-center" >
          </div>
        </span>);
  }
});
