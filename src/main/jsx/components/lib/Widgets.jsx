/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Groupon, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
import React from 'react';
require('./widgets.less');
import filter from 'ramda/src/filter';
import Router from 'react-router';
export default React.createClass({
    render(){
      const activeWidget = this._activeWidget();
      var navs = filter(widget => !widget.props.tabVisibleWhenActive || this._isWidgetSelected(widget), this.props.children).map((widget,index) => this._tabItem(widget,index,widget===activeWidget));
        return(
            <div className="flex-row top-buffer">
                <div className="activeWidget">
                    {activeWidget}
                </div>
                <div className="ui vertical menu  buttons">
                    {navs}
                </div>
            </div>

        );
    },
    _isWidgetSelected(widget){
      return widget.props.url ==this.props.activeWidget;
    },
    _activeWidget(){
     return this.props.children.find((widget)=> this._isWidgetSelected(widget));
    },
    _tabItem(widget,index,isActive){
      var className = isActive? 'active':'';
      className += " widget ui labeled icon button item";
	    return <Router.Link key={index} data-index={index} className={className} to="job-widgets" params={{widget: widget.props.url}}> 
		        <i className={widget.props.icon + " ui icon"}/>{widget.props.name}
		    </Router.Link>;
		   

    }
});

