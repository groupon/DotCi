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
import Nav from 'react-bootstrap/lib/Nav';
import NavItem from 'react-bootstrap/lib/NavItem';
require('./widgets.less')
export default React.createClass({
    getInitialState(){
        return {currentSelection: 0}
    },
    render(){
        let activeWidget =  this.props.children[this.state.currentSelection];
        var navs = this.props.children.map((widget,index) => <li key={index} className={this.state.currentSelection==index?'active':''}><a data-index={index} href="#" onClick={this._onActiveTabChange}> {widget.props.name}</a></li> );
        return(
            <div className="row top-buffer">
                <div className="col-md-9 activeWidget">
                    {activeWidget}
                </div>
                <div className="col-md-3 tabbable tabs-right">
                    <ul className="nav nav-tabs">
                    {navs}
                    </ul>
                </div>
            </div>

        );
    },
    _onActiveTabChange(event){
        var tab =parseInt(event.currentTarget.getAttribute('data-index'))
        this.setState({currentSelection: tab});
        event.preventDefault();
    }
});

