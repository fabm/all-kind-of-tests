import './style/style.scss';

import * as React from "react";
import { Col, Row, FormGroup, Container, CardHeader, CardBody, Label, Input, FormFeedback, FormText } from "reactstrap";
import TNode, { Model } from "./components/TNode";
import { compose, createStore, Action, applyMiddleware } from 'redux';
import reducer, { Type, fromPath, isValidContent } from './reducers';
import createHistory from "history/createBrowserHistory"
import * as ReactDOM from "react-dom";
import Card from 'reactstrap/lib/Card';
import Projects from './components/Projects'
import { Provider } from 'react-redux';
import thunk from 'redux-thunk';
import axios, { AxiosPromise, AxiosResponse } from 'axios';
import Axios from 'axios';
import { BrowserQRCodeReader, VideoInputDevice } from '@zxing/library';
import { renderToString } from 'react-dom/server'

const history = createHistory();
const location = history.location;

// Listen for changes to the current location.
const unlisten = history.listen((location, action) => {
    // location is an object like window.location
    console.log(action, location.pathname, location.state)
});

// Use push, replace, and go to navigate around.
history.push("/home", {
    some: "state",
    complex: {
        list: ["this", "is", "a", "complex", "state"]
    }
});
history.push("/home2", {
    some: "state",
    simple: "this is a simple state"
});

const wnd: any = window;
wnd.h = history;
wnd.values = {};

const composeEnhancers =
    typeof window === 'object' &&
        wnd.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ ?
        wnd.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__({
            // Specify extension’s options like name, actionsBlacklist, actionsCreators, serialize...
        }) : compose;

const enhancer = composeEnhancers(applyMiddleware(thunk));
const store = createStore(reducer, enhancer);

const callProjects: any = (dispatch: any) => {
    console.log('try dispatch fetch response');

    const promise: AxiosPromise<any> = axios.get('forms/projects');
    promise.then(
        (response: AxiosResponse) => {
            console.log('the response',response.data);
            dispatch({
                type: Type[Type.LOAD_FORM],
                form: response.data
            });
        }, (error: any) => console.log(error));

    return promise;
}

store.dispatch(callProjects);

const ROOT = document.querySelector("body");

function navigate(evt: any) {
    switch (evt.keyCode) {
        case 38: {
            store.dispatch({ type: Type[Type.NAVIGATE_UP] });
            break;
        }
        case 40: {
            store.dispatch({ type: Type[Type.NAVIGATE_DOWN] });
            break;
        }
        case 37: {
            store.dispatch({ type: Type[Type.NAVIGATE_LEFT] });
            break;
        }
        case 39: {
            store.dispatch({ type: Type[Type.NAVIGATE_RIGHT] });
            break;
        }
        case 13: {
            store.dispatch({ type: Type[Type.NAVIGATE_ENTER] });
            break;
        }
        default: {
            console.log(evt.keyCode);
        }
    }
}


function createTNode() {
    return (
        <TNode
            tabIndex={0}
            navigationPath={store.getState().navigationPath}
            selectionPath={store.getState().selectionPath}
            value={store.getState().value}
            onExpand={(path: number[]) => store.dispatch({ type: Type[Type.EXPAND_TOGGLE], path: path })}
            onSelect={(path: number[]) => store.dispatch({ type: Type[Type.SELECT_TOGGLE], path: path })}
            onKeyboardDown={navigate}
        />
    )
}

let cams:any = [];
const codeReader = new BrowserQRCodeReader();
const selectDevice=(deviceId:string)=>{
    console.log("device selected",deviceId);
    codeReader.decodeFromInputVideoDevice(deviceId, 'video')
        .then(result => console.log(result))
        .catch(err => console.error(err));    
}
codeReader.getVideoInputDevices()
    .then(videoInputDevices => {
        cams=videoInputDevices.map(device=>
            <div onClick={()=>selectDevice(device.deviceId)}>{device.label}: {device.deviceId}</div>
        );
        render();
        codeReader.decodeFromInputVideoDevice(undefined, 'video')
            .then(result => console.log(result))
            .catch(err => console.error(err));
        })
    .catch(err => console.error(err));

let renderEditor = true;
let currentEditor: any = null;

let videoElement = <video id="video" width="300" height="200" style={{border: "1px solid gray"}}></video>;


function render() {
    let selectedModel: Model = fromPath(store.getState().value, store.getState().selectionPath);
    const doc = (
        <Provider store={store}>
            <Container>
                <Row>
                    <Col sm={12}><h1>Vertest</h1></Col>
                </Row>
                <Row>
                    <Col sm={3}>{createTNode()}</Col>
                    <Col sm={9}>
                        <Card>
                            <CardHeader>{selectedModel.name}</CardHeader>
                            <CardBody>
                                Link:{selectedModel.link}

                                <Projects />

                            </CardBody>
                        </Card>
                        {videoElement}

                        {cams}
                    </Col>

                </Row>
            </Container>
        </Provider>
    );
    ReactDOM.render(doc, ROOT);
}



render();
store.subscribe(render);

console.log(videoElement);