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

import React from "react";
import IconLink from '../lib/IconLink.jsx';
export default React.createClass({
  render(){
    return(
      <div className={"ui simple dropdown item"+(this._hasConfigurePermission()?"":" disabled")}>
        <i className="dropdown icon"></i>
        <i className="icon fa fa-cog"/>  Settings
        <div className="fa-stack menu">
          <a  href="configure" className="ui labeled item"><i className="icon fa fa-wrench "/> Configure</a>
          <a  href="#" onClick={this._deleteJob} className="ui labeled item"><i className="icon fa fa-trash-o "/>Delete</a>
        </div>
      </div>
    ); },
    _hasBuildPermission(){
      return this._getPermissions()? this._getPermissions().build :false;
    },
    _hasConfigurePermission(){
      return this._getPermissions()? this._getPermissions().configure :false;
    },
    _deleteJob(){
      let actions = this.props.flux.getActions('app');
      actions.deleteProject();
    },
    _getPermissions(){
      return this._get('permissions').toObject();
    },
    _get(key){
      return this.props.job.get(key);
    }
});
