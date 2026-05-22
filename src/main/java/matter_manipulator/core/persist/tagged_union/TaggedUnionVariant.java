package matter_manipulator.core.persist.tagged_union;

public interface TaggedUnionVariant<TSelf extends TaggedUnionVariant<TSelf>> {

    TaggedUnionLoader<TSelf> getLoader();

}
