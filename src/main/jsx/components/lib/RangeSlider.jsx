import React from 'react';
import Router from 'react-router';
import qs from 'qs';
import Url from './../../vendor/url.js'
import CustomAttributes from './../mixins/CustomAttributes.jsx';
require('./range_slider.css');
export default React.createClass({
  mixins: [CustomAttributes],
  statics: {
    currentValue(){
      return new Url().query["buildCount"] || 20;
    }
  },
  getInitialState(){
    return {value: this._getQueryValue() || this.props.min}
  },
  componentDidMount(){
    this.refs['ca-slider'].getDOMNode().addEventListener('value-change', function(e) {
      this._onInput(e);
    }.bind(this))
  },
  render(){
    return <span className="range-slider-container hint--top" data-hint={this.props.tooltip}>
      <paper-slider  ref="ca-slider" 
        attrs={{value:this.state.value, 
          editable: true, 
        expand: true,
        pin: true,
        min:this.props.min,
        max: this.props.max }}>
      </paper-slider>
    </span>;
  },
  _onInput(e){
    const u = new Url();
    const newValue =e.target.value;
    u.query["buildCount"]= newValue;
    window.history.pushState('','',u.toString())
    this.replaceState({value: newValue});
    this.props.onChange(newValue);
  },
  _getQueryValue(){
    return new Url().query["buildCount"];
  },
  value() {
    return this.refs.input.getDOMNode().value;
  }

});
