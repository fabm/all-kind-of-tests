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

// For the sake of simplicity, we're gonna use `defaultCollapsed`. Usually, a
// [controlled component](http://facebook.github.io/react/docs/forms.html#controlled-components)
// is preferred.
class CompanyPeople extends React.Component {
    render() {
        return (
            <div>
                <div className={"a b"}>cenas</div>
                {dataSource.map((node, i) => {
                    const type = node.type;
                    const label:any = <span className="node">{type}</span>;
                    return (
                        <TreeView key={type + '|' + i} nodeLabel={label} defaultCollapsed={false}>
                            {node.people.map(person => {
                                const label2:any = <span className="node">{person.name}</span>;
                                return (
                                    <TreeView nodeLabel={label2} key={person.name} defaultCollapsed={false}>
                                        <div className="info">age: {person.age}</div>
                                        <div className="info">sex: {person.sex}</div>
                                        <div className="info">role: {person.role}</div>
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

export default CompanyPeople;