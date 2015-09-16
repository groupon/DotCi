__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistoryPage from './pages/BuildHistoryPage.jsx';
import {job} from './api/Api.jsx'; 
import  BuildHistory from './models/BuildHistory.js'
window.onload = function (){
  const buildHistory = new BuildHistory();
  const Actions = buildHistory.Actions;
  buildHistory.setActionChangeListener((action,buildHistory) => {
    switch(action.name){
      case 'QUERY_CHANGE':
        let query = buildHistory.query;
      job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
        buildHistory.sendAction(Actions.DataChange({...data, filters: data.buildHistoryTabs}));
      });
      break;
      case 'DATA_CHANGE':
        ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
      break;
    }
  });

  buildHistory.sendAction(Actions.QueryChange({filter: 'All', limit: 50}));

}

