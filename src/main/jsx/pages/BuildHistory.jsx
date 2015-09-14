import React from "react";
import LoadingHelper from './../components/mixins/LoadingHelper.jsx';
import RangeSlider from './../components/lib/RangeSlider.jsx';
import BuildFilters from './../components/job/BuildFilters.jsx';
import BuildHistoryTable from './../components/job/BuildHistoryTable.jsx';
export default React.createClass({
  mixins:[LoadingHelper],
  _loadBuildHistory(){
    const actions =this.props.flux.getActions('app');
    actions.getJobInfoFromServer("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", this._currentTab(),this._buildCount());
  },
  _currentTab(){
    return BranchTabs.currentTab();
  },
  _buildCount(){
    return RangeSlider.currentValue();
  },
  _render(){
    const {filters,builds} = this.props.buildHistory;
    const countSlider = <RangeSlider ref="buildCount" tooltip="Build count"  onChange={this._onCountChange} min={20}  max={100} step={5}  />;
    const buildFilters = <BuildFilters  ref="branchTabs" onTabChange={this._onTabChange}  filters={filters} />;
    return(<div className="align-center" >
      <BuildHistoryTable buildFilters={buildFilters} countSlider={countSlider} builds ={builds}/>
    </div>);
  },
  _onTabChange(tab){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(tab, this._buildCount());
  },
  _onCountChange(count){
    let actions = this.props.flux.getActions('app');
    actions.buildHistorySelected(this._currentTab(),count);
  }
});
