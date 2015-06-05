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

import { Store } from 'flummox';
import reject from 'ramda/src/reject';
import React from 'react';
import {fromJS,Map,List} from 'immutable';
var update =React.addons.update;
export default class JobStore extends Store {
  constructor(flux) {
    super();
    let actionIds = flux.getActionIds('app');
    this.register(actionIds.jobInfoChanged, this.jobInfoChanged);
    this.register(actionIds.buildHistoryChanged, this.buildHistoryChanged);
    this.register(actionIds.tabAdded, this.tabAdded);
    this.register(actionIds.tabRemoved, this.tabRemoved);
    this.register(actionIds.clearJobInfo, this.clearJobInfo);
    this.state = {job: Map()};
  }
  clearJobInfo(path){
    this._setState(this._getState().delete(path)) ;
  }
  jobInfoChanged(jobInfo){
    const newData = this._getState().mergeDeep(fromJS(jobInfo));
    this._setState(newData);
  }
  _getState(){
    return this.state.job;
  }
  _setState(newData){
    return this.setState({job: newData});
  }
  tabRemoved(tabToBeRemoved){
    let {buildHistoryTabs} = this._getState().toObject();
    let filteredTabs = buildHistoryTabs.toSeq().filter(tab => tab != tabToBeRemoved);
    const newState = this._getState().withMutations(map => {
      map.set('buildHistoryTabs', fromJS(filteredTabs.toArray()))
    });
    this._setState(newState);
  }
  tabAdded(newTabRegex){
    let {buildHistoryTabs}= this._getState().toObject();
    buildHistoryTabs = buildHistoryTabs.push(newTabRegex);
    const newState = this._getState().merge({buildHistoryTabs});
    this._setState(newState);
  }
  buildHistoryChanged(builds){
    this._setState(this._getState().set('builds',fromJS(builds)))
  }
}
