import ReactDOM from 'react-dom';
import React from 'react';
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import {job} from './../api/Api.jsx'; 
import  {removeFilter,addFilter} from './../api/Api.jsx'; 
import Drawer from './../Drawer.jsx';
import {Job as AutoRefreshComponent} from './../components/lib/AutoRefreshComponent.js';
function dataChange(buildHistory){
  const buildHistoryPage = <BuildHistoryPage buildHistory={buildHistory}/>
  const refreshFunction =()=>{};
  ReactDOM.render(<AutoRefreshComponent component={buildHistoryPage} refreshFunction={refreshFunction}  />, document.getElementById('content'));
  ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
}
function queryChange(buildHistory,oldQuery){
  const {actions,query} = buildHistory;
  buildHistory.dirty = true;
  actions.DataChange({...buildHistory});
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
    actions.DataChange({...data, filters: data.buildHistoryTabs,dirty: false});
  });
}

export default function(buildHistory){
  const actions = buildHistory.actions;
  actions.DataChange.onAction = dataChange;
  actions.QueryChange.onAction = queryChange;
  actions.RemoveFilter.onAction = removeFilter;
  actions.AddFilter.onAction = addFilter;
}
