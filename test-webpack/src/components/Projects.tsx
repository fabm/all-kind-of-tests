import { Col, Row, FormGroup, Container, CardHeader, CardBody, Label, Input, FormFeedback, FormText, Form } from "reactstrap";
import * as React from "react";
import { connect } from "react-redux";
import { Type } from "../reducers";

interface EventProps{
    nameChange:(evt:any)=>void;
}
interface ValueProps{    
    isLoading: boolean;
    isInvalidName: boolean;
    isEmptyName:boolean;
    name:string;
}

interface Props extends EventProps,ValueProps{}

function loadingRender(){
    return <div>is loading...</div>
}

function formRender(p:Props){
    return <Form onSubmit={(e)=>{
        console.log('prevent',e);
        e.preventDefault();
    }
    }>
        <FormGroup
            controlId="projectName"
            validationState={p.isInvalidName}
        >
            <Label>Add project name</Label>
            {!p.isInvalidName && <FormText>Example help text that remains empty.</FormText>}
            <Input
                invalid={p.isInvalidName}
                placeholder="nice placeholder"
                onChange={p.nameChange}
                value={p.name}
            />
            <FormFeedback>that's right it's not valid</FormFeedback>
        </FormGroup>
    </Form>

}

function Projects(p:Props) {
    if(p.isLoading){
        return loadingRender();
    }else{
        return formRender(p);
    }
}

function mapStateToProps(state:any):ValueProps{
    if(state.form.projects === undefined){
        return {        
            isLoading:true,
            isEmptyName:null,
            name:null,
            isInvalidName:null
        }
    }
    return {        
        isLoading:false,
        isEmptyName:state.form.projects.value.length===0,
        name:state.form.projects.value,
        isInvalidName:state.form.projects.value.lenght>3
    }
}
function mapdispatchToProps(dispatch:any):EventProps{
    return {
        nameChange:(evt)=>{
            dispatch({
                type: Type[Type.TYPE_DATA],
                content: evt.target.value,
                component: 'projects'
            })
        }
    }
}

export default connect(mapStateToProps,mapdispatchToProps)(Projects);