# User-Adaptive-Data-Vocalization
**Source code of the project of user-adaptive data vocalization.**

*Note: This project is built with IBM Watson and compiled with Maven. You would have to set up Waston first on your site.*

Code Structure:
* main: source files folder.
  - data_info: classes correspond to relations in DB. Hard-coded column names etc.
  - data_vocal.main_package: main package of the system. Class App is where the program executes.
  - database_accessor: classes correspond to different types of database. MonetDB and PostgreSQL are supported.
  - speeches: classes to generate speech. Have different classes correspond to single column and multi-column speeches.
* test: test cases folder. Currently empty, would update later.
