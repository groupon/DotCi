__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistory from './pages/BuildHistory.jsx';
import {job} from './api/Api.jsx'; 
window.onload = function (){
  function renderBuildHistory(buildHistory){
    ReactDOM.render(<BuildHistory buildHistory={buildHistory}/>, document.getElementById('content'));
  }


  renderBuildHistory({
    filters: [],
    builds: []
  });
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", 'All',50).then(data => {
    renderBuildHistory({builds: data.builds, filters: data.buildHistoryTabs });
  });
}

