__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from "react-dom";
import page from 'page';
import  Model from './models/Model.js'
import bindBuildHistoryActions from './client/BuildHistoryPageActions.js';
require('./app.css');
import Drawer from './Drawer.jsx';
function getBuildHistoryActions(){
  const buildHistory = new Model();
  return actions;
}
window.onload = function (){

  const jobPath = jobUrl.replace(rootURL,'');
  const rootPath = window.location.pathname.split(jobPath)[0] +jobPath;
  const buildHistory = new Model();
  bindBuildHistoryActions(buildHistory);

  page.base(rootPath);
  page('/','dotCIbuildHistory');
  page('dotCIbuildHistory', function () {
    buildHistory.actions.QueryChange({filter: 'All', limit: 50});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page('dotCIbuildMetrics', function () {
    // actions.QueryChange({filter: 'All', limit: 0});
    ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
  });
  page();
}

