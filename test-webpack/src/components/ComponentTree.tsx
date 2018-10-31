import * as React from "react";
import TreeView from './react-treeview'

// This example data format is totally arbitrary. No data massaging is
// required and you use regular js in `render` to iterate through and
// construct your nodes.
const dataSource = [
    {
        type: 'Employees',
        collapsed: false,
        people: [
            {name: 'Paul Gordon', age: 29, sex: 'male', role: 'coder', collapsed: false},
            {name: 'Sarah Lee', age: 27, sex: 'female', role: 'ocamler', collapsed: false},
        ],
    },
    {
        type: 'CEO',
        collapsed: false,
        people: [
            {name: 'Drew Anderson', age: 39, sex: 'male', role: 'boss', collapsed: false},
        ],
    },
];

class OrderChanger {
    listNodes: any;
    currentNode: any;

    keypress(evt: any) {
        console.log(evt);
    }

    up() {

    }

    down() {

    }
}

// For the sake of simplicity, we're gonna use `defaultCollapsed`. Usually, a
// [controlled component](http://facebook.github.io/react/docs/forms.html#controlled-components)
// is preferred.
class CompanyTreeTest extends React.Component {
    render() {
        let createLabel = (content: string): string => content;

        var lastSelection: TreeView = null;

        let nodeSelected = (node: TreeView): boolean => {
            if (!node.state.selected && node !== lastSelection) {
                if (lastSelection !== null) {
                    let newState = {...lastSelection.state, selected: false};
                    lastSelection.setState(newState);
                }
                lastSelection = node;
                return true;
            }
            if (node === lastSelection) {
                return false;
            }
        };

        const orderCharger = new OrderChanger();

        const TestElement = (props: any) => {
            return <div onKeyDown={props.orderChanger.keypress}>test element</div>
        };
        const Leaf = (props: any) => {

            return (
                <TreeView nodeLabel={props.children}
                          key={props.name}
                          defaultCollapsed={false}
                          nodeSelected={nodeSelected}
                          selectable

                />
            )
        };

        return (
            <div className={'react-tree'}>
                {dataSource.map((node, i) => {
                    const type = node.type;
                    return (
                        <TreeView key={i} nodeLabel={createLabel(type)} defaultCollapsed={false}
                                  nodeSelected={nodeSelected}>
                            {node.people.map(person => {
                                return (
                                    <TreeView nodeLabel={createLabel(person.name)} key={person.name}
                                              defaultCollapsed={true} selectable nodeSelected={nodeSelected}>
                                        <TreeView nodeLabel={createLabel(person.name + ' child')} key={person.name}
                                                  defaultCollapsed={false} nodeSelected={nodeSelected} selectable>
                                            <Leaf name={person.age}>age: {person.age}</Leaf>
                                            <Leaf name={person.sex}>sex: {person.sex}</Leaf>
                                            <Leaf name={person.role}>role: {person.role}</Leaf>
                                            <TestElement orderChanger={orderCharger}/>
                                        </TreeView>
                                    </TreeView>
                                );
                            })}
                        </TreeView>
                    );
                })}
            </div>
        );
    }
}

export default CompanyTreeTest;