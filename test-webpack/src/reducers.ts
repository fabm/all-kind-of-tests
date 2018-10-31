import { Model, TNodeData } from "./components/TNode";

export enum Type {
    EXPAND_TOGGLE,
    SELECT_TOGGLE,
    EXPAND_NODE,
    COLLAPSE_NODE,
    NAVIGATE_UP,
    NAVIGATE_DOWN,
    NAVIGATE_RIGHT,
    NAVIGATE_LEFT,
    NAVIGATE_ENTER,
    TYPE_DATA,
    ADD_NODE,
    LOAD_FORM,
    FETCH_FORM
}

export function isValidContent(component:string, content:string):'error'|null{
    switch(component){
        case 'test':
            if(content.length<3){
                return 'error';
            }
            return null;
        break;
    }
}

const init: TNodeData | any = {
    form: {},
    selectionPath: [0],
    navigationPath: [0],
    value: [{
        name: 'Projects',
        selectable: true,
        expanded: false,
        children: [],
        link: 'root'
    }
    ]
};

function pathList(models: Model[], path: number[]): Model[] {
    let currentLevel: Model[] = models;

    let pathSliced = path.slice(0, path.length - 1);
    for (let idx of pathSliced) {
        currentLevel = currentLevel[idx].children;
    }
    return currentLevel;
}

export function fromPath(models: Model[], path: number[]): Model {
    return pathList(models, path)[lastElement(path)];
}

function lastElement(path: number[]): number {
    return path[path.length - 1];
}

function cloneModels(models: Model[]): Model[] {
    return models.map(model => {
        return { ...model, children: cloneModels(model.children) }
    }).slice(0);
}

export default function reducer(state: TNodeData|any = init, action: {
    type: string,
    path?: number[],
    form?: {
        [index:string]:{
            value:string
        }
    }
    component?:string,
    content?:string
}) {
    switch (action.type) {
        case Type[Type.EXPAND_TOGGLE]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            let toToggle = fromPath(clone.value, action.path);
            toToggle.expanded = !toToggle.expanded;
            return clone;
        }
        case Type[Type.SELECT_TOGGLE]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            clone.selectionPath = action.path;
            return clone;
        }
        case Type[Type.NAVIGATE_DOWN]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            let list = pathList(clone.value, clone.navigationPath);
            let lIndex = lastElement(clone.navigationPath);
            let model: Model = fromPath(clone.value, clone.navigationPath);
            if (model.children.length > 0 && model.expanded) {
                clone.navigationPath = [...clone.navigationPath, 0];
                return clone;
            }
            let slicedNavPath = clone.navigationPath;
            while (slicedNavPath.length > 0) {
                model = fromPath(clone.value, slicedNavPath);
                list = pathList(clone.value, slicedNavPath);
                lIndex = lastElement(slicedNavPath);
                if (lIndex < list.length - 1) {
                    slicedNavPath[slicedNavPath.length - 1] = ++lIndex;
                    clone.navigationPath = slicedNavPath;
                    return clone;
                }
                slicedNavPath = slicedNavPath.slice(0, slicedNavPath.length - 1);
            }

            return clone;
        }
        case Type[Type.NAVIGATE_UP]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            let list = pathList(clone.value, clone.navigationPath);
            let lIndex = lastElement(clone.navigationPath);
            if (lIndex > 0) {
                clone.navigationPath[clone.navigationPath.length - 1] = --lIndex;
                return clone;
            }
            if (clone.navigationPath.length > 1) {
                clone.navigationPath = clone.navigationPath.slice(0, clone.navigationPath.length - 1);
            }

            return clone;
        }
        case Type[Type.NAVIGATE_RIGHT]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            let model: Model = fromPath(clone.value, clone.navigationPath);
            if (model.children.length > 0) {
                model.expanded = true;
            }

            return clone;
        }
        case Type[Type.NAVIGATE_LEFT]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            let model: Model = fromPath(clone.value, clone.navigationPath);
            if (model.children.length > 0) {
                model.expanded = false;
            }

            return clone;
        }
        case Type[Type.NAVIGATE_ENTER]: {
            let clone: TNodeData = { ...state, value: cloneModels(state.value) };
            clone.selectionPath = [...clone.navigationPath];
            return clone;
        }
        case Type[Type.TYPE_DATA]: {
            let clone = {
                ...state,
                form: {...state.form,[action.component]:{
                    value: action.content
                }}
            };
            clone.selectionPath = [...clone.navigationPath];
            return clone;
        }
        case Type[Type.LOAD_FORM]:{
            console.log('this is the action',action);
            let clone = {
                ...state,
                form: action.form.forms
            };
            console.log('there');
            return clone;
        }
        default:
            return state;
    }
}