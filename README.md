# hibernate-inhertance
Hibernate Inheritance Mapping
A demonstration project exploring the three JPA/Hibernate inheritance strategies — SINGLE_TABLE, JOINED, and TABLE_PER_CLASS — using H2 as the database and a programmatic H2 web console for inspection.

Table of Contents
Overview

Tech Stack

Project Structure

Inheritance Strategies Demonstrated

Configuration

Running the Application

Inspecting the Database

Observed SQL Behaviour

Key Takeaways

Notes & Caveats

Overview
This project illustrates how Hibernate maps Java inheritance hierarchies to relational tables. It defines three independent entity hierarchies, each using a different InheritanceType, and persists sample instances to an H2 database. The generated SQL is logged, making the differences between strategies visible at a glance.

The application also launches the H2 web console so the resulting schema and data can be inspected in a browser.

Tech Stack
Component	Version
Java	8+
Maven	3.x
Hibernate ORM	5.4.30.Final
JPA API	2.2
H2 Database	1.4.200
Project Structure
text
hibernate-inheritance/
├── pom.xml
├── data/                              # H2 database files (created at runtime)
│   └── testdb.mv.db
└── src/
    └── main/
        ├── java/
        │   └── com/example/
        │       ├── App.java
        │       └── model/
        │           ├── singletable/   # InheritanceType.SINGLE_TABLE
        │           │   ├── Vehicule.java   (abstract, @DiscriminatorColumn)
        │           │   ├── Voiture.java
        │           │   └── Moto.java
        │           ├── joined/        # InheritanceType.JOINED
        │           │   ├── Employe.java
        │           │   ├── Developpeur.java
        │           │   └── Manager.java
        │           └── tableperclass/ # InheritanceType.TABLE_PER_CLASS
        │               ├── Produit.java
        │               ├── Livre.java
        │               └── Electronique.java
        └── resources/
            └── META-INF/
                └── persistence.xml
Inheritance Strategies Demonstrated
1. SINGLE_TABLE — Vehicule, Voiture, Moto
All classes in the hierarchy share one table. A discriminator column (vehicule_type) distinguishes the concrete type.

java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicule_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Vehicule { ... }

@Entity
@DiscriminatorValue("VOITURE")
public class Voiture extends Vehicule { ... }
Pros: fast polymorphic queries, simple schema, no joins.
Cons: nullable columns for subclass-specific fields.

2. JOINED — Employe, Developpeur, Manager
Each class gets its own table. Shared fields live in the parent table; subclass tables reference the parent by primary key via foreign keys.

java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Employe { ... }

@Entity
public class Developpeur extends Employe { ... }
Pros: normalized schema, no nullable subclass columns.
Cons: polymorphic queries require joins.

3. TABLE_PER_CLASS — Produit, Livre, Electronique
Each concrete class gets its own fully self-contained table, including all inherited columns. No parent table exists (the parent is abstract).

java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "produit_seq")
    @SequenceGenerator(name = "produit_seq", sequenceName = "hibernate_sequence")
    private Long id;
    ...
}
Pros: table independence, no joins for single-class queries.
Cons: duplicated columns, polymorphic queries rewritten as UNION ALL, requires SEQUENCE or TABLE generation (not IDENTITY).

Configuration
pom.xml — Dependencies
xml
<dependencies>
    <dependency>
        <groupId>javax.persistence</groupId>
        <artifactId>javax.persistence-api</artifactId>
        <version>2.2</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>5.4.30.Final</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.200</version>
    </dependency>
</dependencies>
Note: The H2 dependency uses the default compile scope (not runtime) because App.java imports org.h2.tools.Server to launch the web console.

src/main/resources/META-INF/persistence.xml
xml
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
                                 http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">

    <persistence-unit name="hibernate-inheritance" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <!-- Entity classes -->
        <class>com.example.model.singletable.Vehicule</class>
        <class>com.example.model.singletable.Voiture</class>
        <class>com.example.model.singletable.Moto</class>
        <class>com.example.model.joined.Employe</class>
        <class>com.example.model.joined.Developpeur</class>
        <class>com.example.model.joined.Manager</class>
        <class>com.example.model.tableperclass.Produit</class>
        <class>com.example.model.tableperclass.Livre</class>
        <class>com.example.model.tableperclass.Electronique</class>

        <properties>
            <property name="javax.persistence.jdbc.driver" value="org.h2.Driver"/>
            <property name="javax.persistence.jdbc.url"
                      value="jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE"/>
            <property name="javax.persistence.jdbc.user" value="sa"/>
            <property name="javax.persistence.jdbc.password" value=""/>

            <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
Notable choices:

File-based H2 (jdbc:h2:file:./data/testdb) — data persists between runs.

AUTO_SERVER=TRUE — allows the app and the H2 console / DBeaver to access the same file concurrently.

hbm2ddl.auto=update — creates missing tables, preserves existing data (unlike create-drop).

Running the Application
Build
bash
mvn clean compile
Run
bash
mvn exec:java -Dexec.mainClass="com.example.App"
Or run App.java directly from your IDE.

What happens
Hibernate boots, reads persistence.xml, scans for entities.

hbm2ddl.auto=update creates or updates the schema.

Three entities are persisted (Voiture, Moto, Developpeur, Manager, Livre, Electronique — depending on the current App version).

Polymorphic queries are executed and results printed to the console.

The H2 web console is started on port 8082.

Inspecting the Database
Once the app is running, open:

text
http://localhost:8082
Log in with:

Field	Value
JDBC URL	jdbc:h2:file:./data/testdb
User	sa
Password	(empty)
Then explore:

sql
SHOW TABLES;

-- SINGLE_TABLE hierarchy
SELECT * FROM VEHICULE;

-- JOINED hierarchy (requires joins)
SELECT e.*, d.* FROM EMPLOYE e
LEFT JOIN DEVELOPPEUR d ON e.ID = d.ID;

-- TABLE_PER_CLASS hierarchy (union view)
SELECT * FROM LIVRE
UNION ALL
SELECT * FROM ELECTRONIQUE;

-- TABLE_PER_CLASS as Hibernate generates it (with discriminator)
SELECT id, nom, auteur, NULL AS garantie, 1 AS clazz_ FROM LIVRE
UNION ALL
SELECT id, nom, NULL AS auteur, garantie, 2 AS clazz_ FROM ELECTRONIQUE;
Alternatively, use DBeaver or any JDBC client pointed at ./data/testdb.mv.db (thanks to AUTO_SERVER=TRUE, concurrent access works).

Observed SQL Behaviour
SINGLE_TABLE — polymorphic select
sql
select
    vehicule0_.id, vehicule0_.marque, vehicule0_.nombrePortes,
    vehicule0_.sidecar, vehicule0_.vehicule_type
from Vehicule vehicule0_
One table, no joins, all subclasses returned in a single pass.

JOINED — polymorphic select
sql
select
    employe0_.id, employe0_.nom,
    employe0_1_.langage,
    employe0_2_.nombreEquipes,
    case
        when employe0_1_.id is not null then 1
        when employe0_2_.id is not null then 2
        when employe0_.id is not null then 0
    end as clazz_
from Employe employe0_
left outer join Developpeur employe0_1_ on employe0_.id = employe0_1_.id
left outer join Manager     employe0_2_ on employe0_.id = employe0_2_.id
The CASE expression resolves the concrete type; the joins pull subclass columns.

TABLE_PER_CLASS — polymorphic select
sql
select ...
from (
    select id, nom, auteur, null as garantie, 1 as clazz_ from Livre
    union
    all select id, nom, null as auteur, garantie, 2 as clazz_ from Electronique
) produit0_
A synthetic UNION ALL across all concrete subclass tables, with NULL padding for columns that don't exist in a given table.

Key Takeaways
Aspect	SINGLE_TABLE	JOINED	TABLE_PER_CLASS
Number of tables	1	1 + N subclasses	N subclasses
Polymorphic query	Trivial scan	Joins + CASE	UNION ALL
Nullable columns	Many	None	N/A (duplicated)
ID generation	IDENTITY OK	IDENTITY OK	SEQUENCE/TABLE only
Pagination at SQL level	Yes	Yes	No (in-memory merge)
FK to parent entity	Yes	Yes	No
Best when	Frequent polymorphic queries	Normalized schema matters	Subclasses rarely queried together
Rule of thumb: prefer SINGLE_TABLE or JOINED. Reach for TABLE_PER_CLASS only when its specific properties (table independence, no shared parent) are actually required.

Notes & Caveats
Persistence unit name
Persistence.createEntityManagerFactory("hibernate-inheritance") must match the name attribute of <persistence-unit> exactly.

Entity discovery
<exclude-unlisted-classes> is not set, so Hibernate scans the classpath for @Entity classes. The <class> list is therefore advisory, not exhaustive — if you want strict control, set <exclude-unlisted-classes>true</exclude-unlisted-classes> and list every entity explicitly.

H2 URL choice
URL	Persists?	Shareable with console/DBeaver?
jdbc:h2:mem:testdb	❌	❌
jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1	❌	Only within the same JVM
jdbc:h2:file:./data/testdb	✅	❌ (file locked)
jdbc:h2:file:./data/testdb;AUTO_SERVER=TRUE	✅	✅
DB_CLOSE_DELAY only applies to in-memory databases. AUTO_SERVER is what enables concurrent access to a file-based DB.

hbm2ddl.auto values
Value	On startup	On shutdown	Use case
create-drop	Drop + create	Drop	Unit tests
create	Drop + create	—	Fresh schema each run
update	Create missing / alter	—	Development (default here)
validate	Verify only	—	Production
none	Nothing	—	Full manual control
create-drop will erase your data on EntityManagerFactory.close() — which is why it's incompatible with inspecting the DB after the app exits.

TABLE_PER_CLASS + IDENTITY
Hibernate will refuse to start if you use GenerationType.IDENTITY with TABLE_PER_CLASS, because per-table auto-increment counters can produce colliding IDs across tables. Use SEQUENCE or TABLE instead.

Resource cleanup
The current App closes the EntityManager and EntityManagerFactory before starting the H2 console. With hbm2ddl.auto=update this is fine — the tables and data remain. With create-drop, they would be dropped.

License
This project is provided as-is for educational purposes.
