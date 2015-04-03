import React from "react";
import Console from './Console.jsx';
import BuildRow from '../BuildRow.jsx'
export default  React.createClass({
  componentDidMount(){
    const actions = this.props.flux.getActions('app');
    actions.currentBuildChanged(this.props.url);
  },
  render(){
    return this.props.build? (<div>
      <BuildRow  {...this.props.build}/>
      {this._buildActions()}
      <Console {...this.props} />
    </div>):<div/>;
  },
  _buildActions(){
    return <div>
      {this.props.cancelable? this._cancelButton():''}
      <div className="circular ui icon button">
        <i className="fa fa-times-circle-o"></i>
      </div>
    </div>
  },
  _cancelButton(){
    return <div className="circular ui icon button">
      <i className="fa fa-refresh"></i>
    </div>
  }
});
