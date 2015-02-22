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
import Modal from 'react-bootstrap/lib/Modal';
import ModalTrigger from 'react-bootstrap/lib/ModalTrigger';
import Button from 'react-bootstrap/lib/Button';
import OverlayMixin from 'react-bootstrap/lib/OverlayMixin';
var ConfirmationModal =React.createClass({

    getInitialState: function () {
        return {
            isModalOpen: true
        };
    },
    onYes: function(){
        this.props.onConfirm();
        this.handleToggle();
    },
    handleToggle: function () {
        this.setState({
            isModalOpen: !this.state.isModalOpen
        });
    },
    render: function () {
        if (!this.state.isModalOpen) {
            return <span/>;
        }

        return (
            <Modal title="Are you Sure?" onRequestHide={this.handleToggle}>
                <div className="modal-footer">
                    <Button onClick={this.handleToggle}>No</Button>
                    <Button bsStyle="primary" onClick={this.onYes}>Yes</Button>
                </div>
            </Modal>
        );
    }
});

export default React.createClass({
    render(){
        return( <ModalTrigger modal={<ConfirmationModal onConfirm={this.props.onConfirm}/>}>
        {this.props.children}
        </ModalTrigger>);
    }
});