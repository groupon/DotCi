__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from "react-dom";
import page from 'page';
import  Job from './models/Job.js'
import bindBuildHistoryActions from './client/BuildHistoryPageActions.js';
import bindBuildMetricsActions from './client/BuildMetricsPageActions.js';
require('./app.css');
import Drawer from './Drawer.jsx';
//wiring
window.onload = function (){

  const jobPath = jobUrl.replace(rootURL,'');
  const rootPath = window.location.pathname.split(jobPath)[0] +jobPath;
  const buildHistory = new Job();
  bindBuildHistoryActions(buildHistory);

  const buildMetrics = new Job();
  bindBuildMetricsActions(buildMetrics);



  page.base(rootPath);
  page('/','dotCIbuildHistory');
  page('dotCIbuildHistory', function () {
    buildHistory.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page('dotCIbuildMetrics', function () {
    buildMetrics.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page(':build',(ctx)=>{
    const buildNumber = ctx.path;
  });
  page();
}

