# VoltCore

### Description

Welcome to **VoltCore**, the central backend service of **Project Volt** – an innovative educational platform designed to help students learn programming through interactive coding challenges and contests. VoltCore is the central hub that powers the platform, managing user data, orchestrating coding challenges, and facilitating communication between the frontend and various backend services.

VoltCore is built using Java and the Spring framework, providing a robust and scalable architecture to handle the needs of our growing user base. It plays a critical role in the following areas:

- **User Management**: Handling authentication, authorization, and user profiles.
- **Challenge Management**: Creating, storing, and managing coding challenges.
- **Contest Management**: Organizing and managing programming contests, including real-time updates and leaderboards.
- **Communication**: Coordinating with other services like VoltDynamo for submission judging via Kafka.

This repository contains the source code, configuration, and documentation for VoltCore. Whether you are a developer looking to contribute, a student aiming to understand the backend architecture, or an educator seeking to integrate Project Volt into your curriculum, this readme will guide you through the setup and usage of VoltCore.

### What is Project Volt?

**Project Volt** is an educational platform designed to help students learn programming through engaging and interactive methods. It offers a variety of coding challenges and contests tailored to different skill levels, providing an effective way for learners to practice and improve their coding skills.

The platform is composed of the following components:

- **[VoltUI](https://github.com/ProjectVolt/VoltUI)**: The frontend interface built with NextJS. It provides users with an intuitive and responsive environment to interact with the platform, access coding challenges, participate in contests, and view their progress.
- **[VoltCore](https://github.com/ProjectVolt/VoltCore)**: The core backend service written in Java and powered by the Spring framework. VoltCore handles user management, challenge creation, contest organization, and communication with other services.
- **[VoltDynamo](https://github.com/ProjectVolt/VoltDynamo)**: A specialized microservice responsible for judging code submissions. Written in Java and using the Spring framework, VoltDynamo utilizes Kafka for efficient communication with VoltCore, ensuring timely and accurate evaluation of code submissions.

Project Volt aims to create a comprehensive learning experience for aspiring programmers, combining theoretical knowledge with practical application. By participating in coding challenges and contests, students can enhance their problem-solving abilities, gain confidence in their coding skills, and prepare for real-world programming tasks.

### Features

- **User Management**: Comprehensive handling of user authentication, authorization, and profile management.
- **Challenge Management**: Tools to create, store, and manage coding challenges of varying difficulty levels.
- **Contest Management**: Organize and manage programming contests with real-time updates, leaderboards, and analytics.
- **Scalable Judging Service**: Integrates with VoltDynamo, a microservice dedicated to code submission judging, communicating over Kafka for reliable and scalable task processing.
- **Spring Boot Framework**: Utilizes the robust Spring Boot framework to build a high-performance, production-ready application.
- **Spring Data JPA**: Leverages Spring Data JPA for seamless integration with the PostgreSQL database, ensuring efficient and scalable data access.
- **PostgreSQL**: Uses PostgreSQL as the primary relational database for storing user data, challenge details, contest information, and submission records.
- **Kafka Integration**: Ensures efficient inter-service communication and real-time data processing with Apache Kafka.
- **Extensive Test Coverage**: Maintains high test coverage to ensure code quality, reliability, and ease of maintenance.

These features ensure that VoltCore is a powerful and reliable backbone for Project Volt, capable of supporting a growing user base and complex educational functionalities.

### Cloning

To get started with VoltCore, you need to clone the repository and initialize the submodules. Follow these steps:

1. **Clone the repository**:

   ```sh
   git clone https://github.com/ProjectVolt/VoltCore.git
   cd VoltCore
   ```

2. **Initialize and update submodules**:
   VoltCore uses VoltDynamo as a submodule. Initialize and update the submodule with the following commands:
   ```sh
   git submodule init
   git submodule update
   ```

Now you have successfully cloned VoltCore along with its submodule VoltDynamo and built VoltDynamo. You can proceed with the installation and running of VoltCore.

### License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
