__webpack_public_path__= window.resURL+'js/';
import React from "react";
import page from 'page';
import  BuildHistory from './models/BuildHistory.js'
import * as buildHistoryActions from './client/BuildHistoryPageActions.js';
require('./app.css');
import Drawer from './Drawer.jsx';
window.onload = function (){
  const buildHistory = new BuildHistory();
  const actions = buildHistory.actions;
  actions.DataChange.onAction = buildHistoryActions.dataChange;
  actions.QueryChange.onAction = buildHistoryActions.queryChange;
  actions.RemoveFilter.onAction = buildHistoryActions.removeFilter;
  actions.AddFilter.onAction = buildHistoryActions.addFilter;

  const jobPath = jobUrl.replace(rootURL,'');
  const rootPath = window.location.pathname.split(jobPath)[0] +jobPath;

  actions.QueryChange({filter: 'All', limit: 50});
  // ReactDOM.render(<Drawer/>, document.getElementById('nav'));
  page.base(rootPath);
  page('/', function () {
    actions.QueryChange({filter: 'All', limit: 50});
  });
  page();
}

