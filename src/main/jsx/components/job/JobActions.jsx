import React from "react";
export default React.createClass( {
  render(){
    const jobActions = [];
    this._add(jobActions, "send", <a href="build?delay=0sec">Build Now</a>);
    this._add(jobActions, "delete", <a href="delete">Delete</a>);
    this._add(jobActions, "settings", <a href="configure">Configure</a>);
    this._add(jobActions, "view-quilt", <a href="toggleNewUI">Classic UI</a>);
    return <span>{jobActions}</span>;
  },
  _add(actions, icon,action){
    actions.push(
      <paper-icon-item key={icon}>
        <iron-icon icon={icon} />{action}
      </paper-icon-item>
    );
  }
})
