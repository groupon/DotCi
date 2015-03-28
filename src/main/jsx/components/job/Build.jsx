import React from "react";
import Router from 'react-router';
import mapIndexed from 'ramda/src/mapIndexed';
require('./build.less');
export default React.createClass({
  mixins: [Router.State],
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.getParams().buildNumber);
    window.addEventListener("hashchange", this._onLineSelectionChange, false);
  },
  componentWillUnmount(){
    window.removeEventListener("hashchange", this._onLineSelectionChange, false);
  },
  _onLineSelectionChange(event){
    const newId = event.newURL.split('#')[1];
    const oldId = event.oldURL.split('#')[1];
    if(oldId){
      document.getElementById(oldId).classList.remove('highlight');
    }
    document.getElementById(newId).classList.add('highlight');
  },
  render(){
    if( this.props.build)
      return <span id="buildLog"><pre> {this._renderLog(this.props.build.log)}</pre></span>;
    return <span/>;
  },
  _onLineSelect(event){
    const lineNumber = event.target.getAttribute('data-line');
    const lineId =`L${lineNumber}`
    Router.HashLocation.push(lineId);
    this._scrollToLine(lineId);
  },
  _scrollToLine(lineId){
    if(lineId && lineId != ""){
      const line = document.getElementById(lineId);
      if(line)
      line.scrollIntoView();
    }
  },
  componentDidUpdate(){
    this._scrollToLine(this._selectedLineHash());
  },
  _selectedLineHash(){
    return Router.HashLocation.getCurrentPath();
  },
  _isLineSelected(lineNumber){
    return `L${lineNumber}`  == this._selectedLineHash();
  },
  _renderLog(logLines){
    return mapIndexed((log,idx) => <p className={this._isLineSelected(idx+1)?'highlight':''} id={`L${idx+1}`}key={idx}><a data-line={idx+1} onClick={this._onLineSelect}/>{log}</p>,logLines); 
  }

});
