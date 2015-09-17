import ReactDOM from 'react-dom';
import React from 'react';
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import {job} from './../api/Api.jsx'; 
export {removeFilter,addFilter} from './../api/Api.jsx'; 
export function dataChange(buildHistory){
  ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
}
export function queryChange(buildHistory){
  const actions = buildHistory.actions;
  let query = buildHistory.query;
  ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
    actions.DataChange({...data, filters: data.buildHistoryTabs});
  });
}
