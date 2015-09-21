import React from "react";
import Convert from  'ansi-to-html';
import loadingsvg from './tail-spin.svg';
import scrollIntoView from 'scroll-into-view';
require('./console.less');
import last from 'ramda/src/last'
export default React.createClass({
  getInitialState(){
    return {logPinned: false}
  },
  componentDidMount(){
    // this._scrollToLine(this.selectedHash());
    // window.addEventListener('scroll',this._onLogScroll);
  },
  componentWillUnmount() {
    // window.removeEventListener('scroll',this._onLogScroll);
  },
  selectedHash(){
    return 0;
  },
  componentDidUpdate(){
    if(this.state.logPinned){
      this._scrollToBottom();
    }else{
      this._scrollToLine(this.selectedHash());
    }
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
  render(){
    return <span  id="buildLog">
      {this._consoleHeader()}
      <pre> 
        <span ref="buildLog" >
          {this._scrollButtons()}
          {this._renderLog(this.props.log)} 
          {this._spinner()} 
        </span>
      </pre>
      <span id="bottom" ref="bottom"/>
    </span>;
  },
  _spinner(){
    return this.props.inProgress? <img src={loadingsvg} />: <span/>;
  },
  _scrollButtons(){
    const color = this.state.logPinned? "green": "#999" ; 
    return ( <paper-button 
      ref="pinLog" 
      class="pinLog" 
      id="pinLog" 
      style = {{color}} 
      onClick={this._onPinLog}>
      <iron-icon icon="arrow-drop-down-circle"/>
      <paper-tooltip  htmlFor="pinLog">Scroll to tail</paper-tooltip>
    </paper-button>);
  },
  _consoleHeader(){
    return <div id="console-header">
      <paper-icon-button 
        className="hint--left" 
        data-hint="Full Log" 
        style={{color: "white"}} 
        onClick={this._fullLog} icon="reorder"></paper-icon-button>
    </div>
  },
  _fullLog(e){
    e.preventDefault();
    window.location = window.rootURL+"/"+this.props.url+"/consoleText";
  },
  _setInitialTop(){
    if(!this.intialTop){
      this.intialTop = this.refs.buildLog.getBoundingClientRect().top;
    }
  },

  _onLogScroll(e){
    this._setInitialTop();
    const newPos = this.refs.buildLog.getBoundingClientRect().top
    const scroll =  this.intialTop - newPos;
    // console.log(`Initial : ${this.intialTop}  newPos: ${newPos} scroll: ${Math.abs(scroll)}`);
    this.refs.pinLog.style.top= Math.abs(scroll)+"px"
  },
  _onPinLog(e){
    this.setState({logPinned: !this.state.logPinned});
    this._scrollToBottom(e);
  },
  _scrollToBottom(e){
    if(e){
      e.preventDefault();
    }
    this._setInitialTop();
    scrollIntoView( this.refs.bottom);
    this._onLogScroll();
  },
  _onLineSelect(event){
    if(event.target.tagName === 'SPAN'|| event.target.tagName === 'A'){
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
  _isLineSelected(lineNumber){
    return `L${lineNumber}`  == this.selectedHash();
  },
  _openFold(e){
    e.stopPropagation();
    e.currentTarget.classList.toggle('closed');
    e.currentTarget.classList.toggle('open');
  },
  _logFold(lines,startIdx,isLast){
    if(lines.length  === 1){
      return this._logLine(lines[0],startIdx);
    }
    const selectedLine = this.selectedHash()!=''? parseInt(this.selectedHash().replace("L",'')) :0;
    const lineSelectedInFold = selectedLine > startIdx && selectedLine < startIdx + lines.size;
    const isOpen = isLast || lineSelectedInFold; 
    const logLines = lines.reduce((list,line,idx) => {
      list.push( this._logLine(line,idx+startIdx))
      return list;
    }, []);
    return <div key={'fold'+(startIdx)} className={"fold "+ (isOpen? "open":"closed")} onClick={this._openFold}>{logLines}</div>
  },
  _logLine(log,idx){
    return (<p dangerouslySetInnerHTML={{__html: "<a></a><span>"+new Convert().toHtml(this.escapeHtml(log)) + "</span>"}}
      key={idx} className={this._isLineSelected(idx)?'highlight':''} id={`L${idx}`} onClick={this._onLineSelect}>
    </p>);
  },
  entityMap:{
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': '&quot;',
    "'": '&#39;',
    "/": '&#x2F;'
  },
  escapeHtml(string) {
    return String(string).replace(/[&<>"'\/]/g,s =>  this.entityMap[s])
  },
  _renderLog(logLines){
    const log =this.props.log;
    // _logFold(lines,startIdx,isLast){
    let startIdx =1 ; 
    return log.reduce((groups,logGroup,idx) =>{
      const fold = this._logFold(logGroup,startIdx,idx == log.length-1);
      groups.push(fold);
      startIdx+=logGroup.length;
      return groups;
    },[]);
  }

});
