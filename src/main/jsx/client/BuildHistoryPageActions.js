import ReactDOM from 'react-dom';
import React from 'react';
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import {job} from './../api/Api.jsx'; 
import  {removeFilter,addFilter} from './../api/Api.jsx'; 
function dataChange(buildHistory){
  ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
}
function queryChange(buildHistory){
  const actions = buildHistory.actions;
  let query = buildHistory.query;
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
    actions.DataChange({...data, filters: data.buildHistoryTabs});
  });
}

export default function(buildHistory){
  const actions = buildHistory.actions;
  actions.DataChange.onAction = dataChange;
  actions.QueryChange.onAction = queryChange;
  actions.RemoveFilter.onAction = removeFilter;
  actions.AddFilter.onAction = addFilter;
}
