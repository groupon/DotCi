import React from "react";
import Router from 'react-router';
import mapIndexed from 'ramda/src/mapIndexed';
import Convert from  'ansi-to-html';
require('./console.less');
export default React.createClass({
  componentDidMount(){
    window.addEventListener("hashchange", this._onLineSelectionChange, false);
  },
  componentWillUnmount(){
    if(this._isBuildLoaded()) {
      this.props.build.log = [];
    }
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
  _isBuildLoaded(){
    return this.props.build && this.props.build.log && this.props.build.log.length > 1;
  },
  render(){
    if(this._isBuildLoaded())
      return <span id="buildLog"><pre> {this._renderLog(this.props.build.log)}</pre></span>;
    return <div id="log-loading" className="ui  active dimmer">
      <div className="ui content large text loader">Loading</div>
    </div>
  },
  _onLineSelect(event){
    if(event.target.tagName == 'A'){
      event.stopPropagation();
      const lineId = event.currentTarget.getAttribute('id');
      Router.HashLocation.push(lineId);
      this._scrollToLine(lineId);
    }
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
    e.stopPropagation();
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
    return (<p dangerouslySetInnerHTML={{__html: "<a></a>"+new Convert().toHtml(log)}}
      key={idx} className={this._isLineSelected(idx)?'highlight':''} id={`L${idx}`} onClick={this._onLineSelect}>
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
