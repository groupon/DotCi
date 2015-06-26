import React from "react";
import Loading from './Loading.jsx'
require('./loading.css');
export default {
  render(){
    if(this._allPropsAvailable()){
      return this._render()
    }else{
      return <Loading/>;
    }
  },
  _allPropsAvailable(){
    return Object.keys(this.props).reduce((prev,prop) => prev && (!!this.props[prop] || prop === 'children'), true);
  }
};
