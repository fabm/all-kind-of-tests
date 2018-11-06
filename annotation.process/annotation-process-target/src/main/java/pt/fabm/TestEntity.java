package pt.fabm;

@Entity(table = "test_table", keyspace = "nu3w", script = "script/MappingFields.groovy")
@NamedQuery(name = "byId", value = "select id, name by id")
@NamedQuery(name = "insertX", value = "insert into xxx")
public class TestEntity {

    @Field(value = "id_field", keyType = Field.KeyType.PARTITION_KEY)
    private String id;

    @Field(value = "name_field", keyType = Field.KeyType.CLUSTERING_KEY)
    private String name;
}
