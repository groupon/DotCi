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
    event.stopPropagation();
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
  _openFold(e){
    e.currentTarget.classList.toggle('closed');
    e.currentTarget.classList.toggle('open');
  },
  _logFold(log,idx,isOpen){
    return (
      <div key={'fold'+idx} className={"fold "+ (isOpen? "open":"closed")} onClick={this._openFold}>
        {mapIndexed((line,lineNo) => this._logLine(line,idx+lineNo),log)}
      </div>
    ) 
  },
  _logLine(log,idx){
    return (<p key={idx} className={this._isLineSelected(idx)?'highlight':''} id={`L${idx}`}>
      <a data-line={idx} onClick={this._onLineSelect}/>{log}
    </p>);
  },

  _renderLog(logLines){
    var groupedLines = [[]];
    for (let i = 0; i < logLines.length; i++) {
      let line =logLines[i];
      if(line.startsWith('$')){
        groupedLines.push([line]);
      }else{
        groupedLines[ groupedLines.length-1].push(line);
      }
    }
    var lineNo = 1;
    return mapIndexed((log,idx) => {
      if(log.length  == 1){
        const logLine =this._logLine(log[0],lineNo);
        lineNo = lineNo + 1;
        return logLine;
      }else{
        const selectedLine = this._selectedLineHash()!=''? parseInt(this._selectedLineHash().replace("L",'')) :0;
        var lineSelectedInFold = selectedLine > lineNo && selectedLine < lineNo + log.length;
        var fold = this._logFold(log,lineNo,idx == groupedLines.length -1 || lineSelectedInFold);
        lineNo = lineNo + log.length
        return fold;
      }
    },groupedLines); 
  }

});
