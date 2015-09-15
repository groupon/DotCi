__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistoryPage from './pages/BuildHistoryPage.jsx';
import {job} from './api/Api.jsx'; 
import BuildHistory from './models/BuildHistory.js'
window.onload = function (){
  const buildHistory = new BuildHistory();
  buildHistory.addDataChangeListener(buildHistory => {
    ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
  });
  buildHistory.addQueryChangeListener(buildHistory => {
    let query = buildHistory.query;
    job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
      buildHistory.dataChanged({...data, filters: data.buildHistoryTabs});
    });
  });

  buildHistory.queryChanged({filter: 'All', limit: 50});

}

