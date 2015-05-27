import React from "react";
import Router from 'react-router';
import Convert from  'ansi-to-html';
import LocationHashHelper from './../../mixins/LocationHashHelper.jsx'
import LoadingHelper from './../../mixins/LoadingHelper.jsx'
import loadingsvg from './tail-spin.svg';
import {OrderedMap,List} from 'immutable';
require('./console.less');
export default React.createClass({
  mixins: [LocationHashHelper ,LoadingHelper], 
  componentDidMount(){
    this._scrollToLine(this.selectedHash());
  },
  _onLocationHashChange(event){
    const [oldId, newId] = this.getHashIds(event);
    if(oldId){
      document.getElementById(oldId).classList.remove('highlight');
    }
    if(newId){
      document.getElementById(newId).classList.add('highlight');
    }
  },
  _isBuildLoaded(){
    return this.props.log && this.props.log.size > 1;
  },
  _render(){
    return <span id="buildLog">
      {this._consoleHeader()}
      <pre> {this._renderLog(this.props.log)} {this._spinner()} </pre>
      <span ref="bottom"/>
    </span>;
  },
  _spinner(){
    return this.props.buildResult === 'IN_PROGRESS'? <img src={loadingsvg} />: <span/>;
  },
  _consoleHeader(){
    return <div id="console-header">
      <div className="ui right menu inverted basic icon buttons">
        <a className="ui button" href={window.rootURL+"/"+this.props.url+"/consoleText"}><i className="fa fa-eye"></i>Full Log</a>
        <div className="ui toggle button" onClick={this._scrollToBottom}>
          <i className="fa fa-arrow-down"></i>
        </div>
      </div> 
    </div>
  },
  _scrollToBottom(e){
    e.preventDefault();
    this.refs.bottom.getDOMNode().scrollIntoView();
  },
  _onLineSelect(event){
    if(event.target.tagName == 'A'){
      event.stopPropagation();
      const lineId = event.currentTarget.getAttribute('id');
      Router.HashLocation.push(lineId);
    }
  },
  _scrollToLine(lineId){
    if(lineId){
      const line = document.getElementById(lineId);
      if(line)
        line.scrollIntoView();
    }
  },
  componentDidUpdate(){
    this._scrollToLine(this.selectedHash());
  },
  _isLineSelected(lineNumber){
    return `L${lineNumber}`  == this.selectedHash();
  },
  _openFold(e){
    e.stopPropagation();
    e.currentTarget.classList.toggle('closed');
    e.currentTarget.classList.toggle('open');
  },
  _logFold(lines,startIdx,isLast){
    if(lines.size  === 1){
      return this._logLine(lines.get(0),startIdx);
    }
    const selectedLine = this.selectedHash()!=''? parseInt(this.selectedHash().replace("L",'')) :0;
    const lineSelectedInFold = selectedLine > startIdx && selectedLine < startIdx + lines.size;
    const isOpen = isLast || lineSelectedInFold; 
    const logLines = lines.reduce((list,line,idx) => list.push( this._logLine(line,idx+startIdx)), List());
    return <div key={'fold'+(startIdx)} className={"fold "+ (isOpen? "open":"closed")} onClick={this._openFold}>{logLines}</div>
  },
  _logLine(log,idx){
    return (<p dangerouslySetInnerHTML={{__html: "<a></a>"+new Convert().toHtml(log)}}
      key={idx} className={this._isLineSelected(idx)?'highlight':''} id={`L${idx}`} onClick={this._onLineSelect}>
    </p>);
  },
  _renderLog(logLines){
    const groupedLines = logLines.reduce((list,line)=>{
      if( line.startsWith('$')){
        return  list.push(List.of(line))
      }
      const newLast = list.last().push(line)
      return list.set(-1,newLast);
    } , List.of(List()));
    //Add start indexes Turn [[..],[...],..] => [[[...],1] [[....],4], ..]
    const groupedLinesWithIdx =  groupedLines.reduce((list,group) => {
      if(list.last()){
        return list.push(List.of(group,list.last().get(0).size+ list.last().get(1)))
      }
      return list.push(List.of(group,1));
    }, List());
    return groupedLinesWithIdx.reduce((list,group) => list.push(this._logFold(group.get(0),group.get(1), groupedLinesWithIdx.last() == group  ) ), List() )
  }

});
