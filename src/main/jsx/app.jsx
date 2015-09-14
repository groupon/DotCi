__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistoryPage from './pages/BuildHistoryPage.jsx';
import {job} from './api/Api.jsx'; 
import BuildHistory from './models/BuildHistory.js'
window.onload = function (){
  const buildHistory = new BuildHistory();
  buildHistory.addChangeListener(buildHistory => {
    ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
  });
  buildHistory.historyChanged({builds:[],filters:[]});
  buildHistory.queryChange({filter:'All', limit:50 });
}

