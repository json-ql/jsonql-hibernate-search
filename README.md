# jsonql-hibernate-search

Hibernate Search filter query builder for [JSON-QL](https://github.com/json-ql). Allows to apply filters with Hibernate Search using standard Lucene query syntax. Usually used with local Lucene instance and index.

## How to use

To use this library please add it as your gradle/maven dependency with [these remarks](https://github.com/json-ql/jsonql-core#how-to-use):

```groovy
dependencies {
    implementation 'com.lifeinide.jsonql:jsonql-hibernate-search:VERSION'
}
```

## Example usage

The following example uses Hibernate working on H2 database, whereas the Lucene full-text index is created in local filesystem.

> Note, you can check the full working example in [tests](src/test).

### 1. Add required dependencies

```groovy
{
dependencies {
    implementation group: 'com.lifeinide.jsonql', name: 'jsonql-hibernate-search', version: '1.0.1'
    implementation group: 'com.h2database', name: 'h2', version: '1.4.199'
}
``` 

### 2. Create your own entity

```java
@Entity
public class User {

    @Id private Long id;
    
    // this field becomes searchable using HibernateSearchFilterQueryBuilder
    @Field(name = HibernateSearch.FIELD_TEXT)  
    protected String username;

    // this field is additionally stored in the index as "admin" field, 
    // and allows us to additionally filter out search results
    @Field(name = "admin", analyze = Analyze.NO, norms = Norms.NO)  
    protected boolean admin = false;

    // getters and setters go here

}
```

> Note, that Hibernate Search supports primitive fields like `boolean` out of the box. However, if you want to filter data with some custom things like related entities, you need to use custom `FieldBridge`. Please take a look at `BaseDomainFieldBridge` and `RangeNumberBridge` provided by this lib.

### 3. Create `META-INF/persistence.xml` mapping

```xml
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
			 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			 xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd"
			 version="2.0">
	<persistence-unit name="my-unit-name">
		<class>User</class>
		<exclude-unlisted-classes>true</exclude-unlisted-classes>
		<properties>
			<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
			<property name="hibernate.hbm2ddl.auto" value="update"/>
			<property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
			<property name="javax.persistence.jdbc.url" value="jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"/>
			<property name="hibernate.search.default.directory_provider" value="filesystem"/>
			<property name="hibernate.search.default.indexBase" value="tmp"/>
		</properties>
	</persistence-unit>
</persistence>
```

### 4. Create your own filtering frame

The frame below supports paging and sorting automatically, we just need to add custom filtering fields. We want to make possible to search existing users and filter them using `admin` flag:

```java
public class UserFilter extends DefaultPageableRequest {

    protected String query;
    protected SingleValueQueryFilter<Boolean> admin;

    // getters and setters go here

}
```

### 5. Write the controller

Then you just need to expose `UserFilter` frame in your endpoint. We don't enforce any request-handling technology and this library can be used with any JSON or GraphQL based framework. Here is a simple hyphothetical controller that could be used to search users:

```java
@Controller("/user")
public class UserController {

    @Post("/search")
    public Page<User> searchUsers(UserFilter filter) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("my-unit-name");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        try {
            return new DefaultHibernateSearchFilterQueryBuilder<User>(new HibernateSearch(em), User.class, filter.getQuery())
                .add("admin", filter.getAdmin())
                .list(filter);
        } finally {
            entityManager.getTransaction().commit();
            entityManager.close();
        }
    }   

}
```

Of course, this is ony a clunky example. Usually the `EntityManager` will be obtained  elsewhere, and it's the only thing required to build `HibernateSearchFilterQueryBuilder`.

### 6. Test the controller

Now, you can test the controller sending following JSON-s to `searchUsers()` endpoint:

To just get all users with the username containing "john" unpaginated:

```json
{
  "query": "john"
}
```   

To search users paginated:

```json
{
  "pageSize": 20,
  "page": 1,
  "query": "john"
}
```

To search only admins paginated:

```json
{
  "pageSize": 20,
  "page": 1,
  "query": "john",
  "admin": {
    "condition": "eq",
    "value": true  
  } 
}
```

And of course you can add a lot more fields to your `User` entity and then consider them in `UserFilter`. Besides `SingleValueQueryFilter`, which just filters our a single primitive value like `Boolean` or `Enum`, there're plenty of other predefined filters available and described [here](https://github.com/json-ql/jsonql-core).
