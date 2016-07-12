import React from 'react';
import contains from 'ramda/src/contains'
import classNames from 'classnames'; 
import Dialog from './../lib/Dialog.jsx';
import {Tabs, Tab} from 'material-ui/Tabs';
require('./branch_tabs.css')
export default React.createClass({
  render(){
    const tabs =  this.props.filters.map((tab,i) => <Tab route={tab} key={tab} onTouchTapi={this._onTabSelect} label={tab}> <h1>{tab}</h1> </Tab>)
    return <Tabs onTouchTapi={this._onTabSelect}>{tabs}</Tabs>
  },
  _onTabSelect(e){
    const filter =e.currentTarget.getAttribute('data-tab')
    this.props.actions.QueryChange({filter});
  }
});
