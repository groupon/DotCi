__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistoryPage from './pages/BuildHistoryPage.jsx';
import {job,removeFilter,addFilter} from './api/Api.jsx'; 
import  BuildHistory from './models/BuildHistory.js'
window.onload = function (){
  const buildHistory = new BuildHistory();
  const actions = buildHistory.actions;
  actions.DataChange.onAction = function(buildHistory){
    ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
  }
  actions.QueryChange.onAction = function(buildHistory){
    let query = buildHistory.query;
    ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
    job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
      actions.DataChange({...data, filters: data.buildHistoryTabs});
    });
  }
  actions.RemoveFilter.onAction = removeFilter;
  actions.AddFilter.onAction = addFilter;

  actions.QueryChange({filter: 'All', limit: 50});

}

