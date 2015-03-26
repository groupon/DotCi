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
import fetch_polyfill from 'whatwg-fetch';
import {stringify} from 'qs';
export function recentProjects(){
  return _get(window.rootURL + '/recentProjects/');
}
export function job(tree){
  return _get(_jobApiUrl()+"/info/",{tree:tree});
}
export async function deleteCurrentProject(){
  const rsp = await fetch(window.location.pathname.replace('newUi','')+"/doDeleteAjax",{method: 'post' });
  window.location = rsp.headers.get('location');
}

export function removeBranchTab(tabRegex){
  fetch(window.location.pathname.replace('newUi','')+"/removeBranchTab?tabRegex="+tabRegex,{method: 'post'});
}
export function addBranchTab(tabRegex){
  fetch(window.location.pathname.replace('newUi','')+"/addBranchTab?tabRegex="+tabRegex,{method: 'post'});
}

export function fetchBuildHistory(tab) {
    return _get(`${_jobApiUrl()}/buildHistory/${tab}`,{depth:2});
}

function _jobApiUrl() {
    const url = window.location.pathname.replace('newUi','') + 'json';
    return url;
}
function _get(url, params){
  let fetchUrl = params?`${url}?${stringify(params)}`: url;
  return fetch(fetchUrl).then(response=>response.json());
}

