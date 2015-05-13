import React from 'react';
import Router from 'react-router';
import qs from 'qs';
import Url from './../../vendor/url.js'
export default React.createClass({
  render(){
    const value = this._getQueryValue() || this.props.min
    return <span className="ui label"> {value} <input onMouseUp={this._onInput} defaultValue={value} min={this.props.min} step={this.props.step} max={this.props.max} className="detail" type="range"/> </span>;
  },
  _onInput(e){
    const u = new Url();
    const newValue =e.target.value;
    u.query[this.props.queryParam]= newValue;
    window.history.pushState('','',u.toString())
    this.props.onChange(newValue);
  },
  _getQueryValue(){
    return new Url().query[this.props.queryParam];
  }

});
