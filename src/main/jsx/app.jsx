__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from "react-dom";
import page from 'page';
import  Model from './models/Model.js'
import bindBuildHistoryActions from './client/BuildHistoryPageActions.js';
import bindBuildMetricsActions from './client/BuildMetricsPageActions.js';
require('./app.css');
import Drawer from './Drawer.jsx';
//wiring
window.onload = function (){

  const jobPath = jobUrl.replace(rootURL,'');
  const rootPath = window.location.pathname.split(jobPath)[0] +jobPath;
  const buildHistory = new Model();
  bindBuildHistoryActions(buildHistory);

  const buildMetrics = new Model();
  bindBuildMetricsActions(buildMetrics);

  page.base(rootPath);
  page('/','dotCIbuildHistory');
  page('dotCIbuildHistory', function () {
    buildHistory.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page('dotCIbuildMetrics', function () {
    // actions.QueryChange({filter: 'All', limit: 0});
    buildMetrics.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page();
}

