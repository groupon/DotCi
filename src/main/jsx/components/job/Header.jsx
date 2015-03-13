import React from "react";
import IconLink from '../lib/IconLink.jsx';
require('./header.less');
export default React.createClass({
  render(){
    return(
      <div className="ui menu">
        <IconLink href={this.props.githubUrl}  icon="octicon octicon-mark-github">{this.props.fullName}</IconLink>
        <IconLink href="build?delay=0sec" disabled={!this._hasBuildPermission()} icon="fa fa-rocket"> Build Now</IconLink>
        <div id="settings" className="right ui compact menu">
          <div className={"button labeled ui simple dropdown item"+(this._hasConfigurePermission()?"":" disabled")}>
            <i className="dropdown icon"></i>
            <i className="icon fa fa-cog"/>  Settings
            <div className="fa-stack menu">
              <a  href="configure" className="ui labeled item"><i className="icon fa fa-wrench "/> Configure</a>
              <a  href="#" onClick={this._deleteJob} className="ui labeled item"><i className="icon fa fa-trash-o "/>Delete</a>
            </div>
          </div>
        </div>

      </div>
    );
  },
  _hasBuildPermission(){
    return this.props.permissions? this.props.permissions.build :false;
  },
  _hasConfigurePermission(){
    return this.props.permissions? this.props.permissions.configure :false;
  },
  _deleteJob(){
    let actions = this.props.flux.getActions('app');
    actions.deleteProject();
  }
});
