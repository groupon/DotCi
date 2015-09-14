__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistory from './pages/BuildHistory.jsx';
import {job} from './api/Api.jsx'; 
window.onload = function (){
  const buildHistory = {
    filters: [],
    builds: []
  }
  ReactDOM.render(<BuildHistory buildHistory={buildHistory}/>, document.getElementById('content'));
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", 'All',50).then(data => {
    ReactDOM.render(<BuildHistory buildHistory={{builds: data.builds, filters: data.buildHistoryTabs}}/>, document.getElementById('content'));
  });
}

