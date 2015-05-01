import React from "react";
export default {
  _loading(){
    return <div id="log-loading" className="ui  active dimmer">
      <div className="ui content large text loader">Loading</div>
    </div>
  },
  render(){
    const allPropsAvailable =  Object.keys(this.props).reduce((prev,prop) =>  prev && !!this.props[prop], true);
    return allPropsAvailable? this._render(): this._loading();
  }
};
