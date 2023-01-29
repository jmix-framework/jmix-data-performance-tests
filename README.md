# Jmix Data Performance Tests

This project demonstrates different approaches to saving data in [Jmix](https://jmix.io) framework and compares their performance.

The tests use the same model: [Employee](src/main/java/com/company/demo/entity/Employee.java) entity which has a few string, date and number attributes and a reference to the [Department](src/main/java/com/company/demo/entity/Department.java).

All tests save 10_000 new instances of the `Employee` entity.

See [DataManagerTest](src/test/java/com/company/demo/DataManagerTest.java), [EntityManagerTest](src/test/java/com/company/demo/EntityManagerTest.java), [JdbcTest](src/test/java/com/company/demo/JdbcTest.java) classes.

Results of the execution on MacBook M1 Pro:

Using `DataManager`:

- DataManagerTest.oneByOne: 15954 ms
- DataManagerTest.oneByOne_discardSaved: 5300 ms
- DataManagerTest.oneByOne_discardSaved_unconstrained: 2770 ms
- DataManagerTest.allAtOnce_discardSaved_unconstrained: 2674 ms
- DataManagerTest.inBatches (100): 1055 ms
- DataManagerTest.inBatches_unconstrained (100): 811 ms

Using `EntityManager`:

- EntityManagerTest.oneByOne: 2373 ms
- EntityManagerTest.allAtOnce: 721 ms
- EntityManagerTest.inBatches (100): 344 ms

Using `JdbcTemplate`:

- JdbcTest.oneByOne: 1405 ms
- JdbcTest.inBatches (100): 430 ms
