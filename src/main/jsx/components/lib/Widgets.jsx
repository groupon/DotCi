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
import Router from 'react-router';
export default React.createClass({
    render(){
	var navs = this.props.children.map((widget,index) => this._tabItem(widget,index));
        return(
            <div className="flex-row top-buffer">
                <div className="activeWidget">
                    {this._activeWidget()}
                </div>
                <div className="tabbable tabs-right">
                    <ul className="nav nav-tabs">
                    {navs}
                    </ul>
                </div>
            </div>

        );
    },
    _activeWidget(){
      let activeWidget = this.props.children.find((widget)=> widget.props.url ==this.props.activeWidget );
      return activeWidget? activeWidget : this.props.children[0];
    },
    _tabItem(widget,index){
      var className = this.props.activeWidget?'': index === 0? 'active':'';
	    return <li key={index}>
        <Router.Link data-index={index} className={className} to="job-widgets" params={{widget: widget.props.url}}> 
		        <i className={widget.props.icon}/>{widget.props.name}
		    </Router.Link>
		   </li>; 

    }
});

