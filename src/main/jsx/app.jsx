__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistoryPage from './pages/BuildHistoryPage.jsx';
import {job} from './api/Api.jsx'; 
class BuildHistoryModel {
  constructor(){
    this.filters= [];
    this.builds= [];
  }
  get filterChange(){
  }
  buildCountChange(){
  }
}
window.onload = function (){
  const buildHistory = new BuildHistoryModel();
  function renderBuildHistory(buildHistory){
    ReactDOM.render(<BuildHistoryPage buildHistory={buildHistory}/>, document.getElementById('content'));
  }


  renderBuildHistory(buildHistory);

  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]", 'All',50).then(data => {
    renderBuildHistory({builds: data.builds, filters: data.buildHistoryTabs });
  });
}

