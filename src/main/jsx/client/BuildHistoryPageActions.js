import ReactDOM from 'react-dom';
import React from 'react';
import BuildHistoryPage from './../pages/BuildHistoryPage.jsx';
import {job} from './../api/Api.jsx'; 
import  {removeFilter,addFilter} from './../api/Api.jsx'; 
import DrawerMenu from './../Drawer.jsx';
import {Job as AutoRefreshComponent} from './../components/lib/AutoRefreshComponent.js';

import { Drawer, AppBar, MenuItem} from 'material-ui'
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import {white,black} from 'material-ui/styles/colors';

function dataChange(buildHistory){
  const buildHistoryPage = <BuildHistoryPage buildHistory={buildHistory}/>
  const refreshFunction =()=>buildHistory.actions.QueryChange(buildHistory.query);



  const muiTheme = getMuiTheme({
    tabs: {
      backgroundColor: white,
      textColor: black,
      selectedTextColor: black
    }
  });
  const historyPage = <AutoRefreshComponent component={buildHistoryPage} refreshFunction={refreshFunction} refreshInterval={10000}  />
  const page = <MuiThemeProvider muiTheme={muiTheme}><div>
  <Drawer  docked={true} open={true}>
  <MenuItem>Menu Item</MenuItem>
  <MenuItem>Menu Item 2</MenuItem>
  </Drawer>
  <AppBar   title="App Bar Example" isInitiallyOpen={true} >
  </AppBar>
  {historyPage}
  </div>
  </MuiThemeProvider>

  ReactDOM.render(page, document.getElementById('content'));
  // ReactDOM.render(<Drawer menu="job"/>, document.getElementById('nav'));
}
function queryChange(buildHistory,oldQuery){
  const {actions,query} = buildHistory;
  buildHistory.dirty = true;
  actions.DataChange({...buildHistory});
  job("buildHistoryTabs,builds[*,commit[*],cause[*],parameters[*]]",query.filter ,query.limit).then(data => {
    actions.DataChange({...data, filters: data.buildHistoryTabs,dirty: false});
  });
}

export default function(buildHistory){
  const actions = buildHistory.actions;
  actions.DataChange.onAction = dataChange;
  actions.QueryChange.onAction = queryChange;
  actions.RemoveFilter.onAction = removeFilter;
  actions.AddFilter.onAction = addFilter;
}
