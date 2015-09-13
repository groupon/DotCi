__webpack_public_path__= window.resURL+'js/';
import React from "react";
import ReactDOM from 'react-dom';
import BuildHistory from './pages/BuildHistory.jsx';
window.onload = function (){
  const buildHistory = {
    filters: [],
    builds: []
  }
  ReactDOM.render(<BuildHistory buildHistory={buildHistory}/>, document.getElementById('content'));
}

