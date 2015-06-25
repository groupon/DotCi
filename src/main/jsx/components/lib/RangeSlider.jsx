import React from 'react';
import Router from 'react-router';
import qs from 'qs';
import Url from './../../vendor/url.js'
require('./range_slider.css');
export default React.createClass({
  statics: {
    currentValue(){
      return new Url().query["buildCount"] || 20;
    }
  },
  getInitialState(){
    return {value: this._getQueryValue() || this.props.min}
  },
  render(){
    return <span className="range-slider-container hint--top" data-hint={this.props.tooltip}>
      <input ref="input" 
        onChange={this._onChange} 
        onMouseUp={this._onInput} 
        defaultValue={this.state.value} 
        min={this.props.min} 
        step={this.props.step} 
        max={this.props.max} 
        type="range"/>
      <span className="label">{this.state.value}</span>
    </span>;
  },
  _onInput(e){
    const u = new Url();
    const newValue =e.target.value;
    u.query["buildCount"]= newValue;
    window.history.pushState('','',u.toString())
    this.props.onChange(newValue);
  },
  _onChange(e){
    this.replaceState({value: e.target.value});
  },
  _getQueryValue(){
    return new Url().query["buildCount"];
  },
  value() {
    return this.refs.input.getDOMNode().value;
  }

});
