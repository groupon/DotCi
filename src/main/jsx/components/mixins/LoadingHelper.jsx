import React from "react";
require('./loading.css');
export default {
  render(){
    if(this._allPropsAvailable()){
      return this._render()
    }else{
      return this._renderLoading();
    }
  },
  _allPropsAvailable(){
    return Object.keys(this.props).reduce((prev,prop) => prev && (!!this.props[prop] || prop === 'children'), true);
  },
  _renderLoading(){
    return <svg className="spinner" width="65px" height="65px" viewBox="0 0 66 66" xmlns="http://www.w3.org/2000/svg">
      <circle className="path" fill="none" strokeWidth="6" strokeLinecap="round" cx="33" cy="33" r="30"></circle>
    </svg>
  }
};
