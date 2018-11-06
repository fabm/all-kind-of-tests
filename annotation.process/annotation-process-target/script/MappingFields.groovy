toCassandraType = { table, name, type ->
    log.info('xxx')
    'text'
}

getStrategy = { table-> 'SimpleStrategy'}
getReplicationFactor = { table-> 1}
getkeyspace = { table-> 'nu3w'}