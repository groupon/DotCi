import ReactDOM from 'react-dom';
import React from 'react';
import BuildMetricsPage from './../pages/BuildMetricsPage.jsx';
import {job} from './../api/Api.jsx'; 
import  {removeFilter,addFilter} from './../api/Api.jsx'; 
function dataChange(buildMetrics){
  ReactDOM.render(<BuildMetricsPage buildMetrics={buildMetrics}/>, document.getElementById('content'));
}
function queryChange(buildMetrics){
  const actions = buildMetrics.actions;
  let query = buildMetrics.query;
  job("buildHistoryTabs,metrics[title,chart[*,dataSets[*]]]",query.filter ,query.limit).then(data => {
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
