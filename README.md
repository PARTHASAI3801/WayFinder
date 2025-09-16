[comment]: # (You may find the following markdown cheat sheet useful: https://www.markdownguide.org/cheat-sheet/. You may also consider using an online Markdown editor such as StackEdit or makeareadme.) 

## Project title: A Crowdsourcing Tool for Route Optimisation

### Student name: Partha Sai Syed

### Student email: pss40@student.le.ac.uk

### Project description: 
This project aims to implement a crowdsourcing route planning tool designed to optimize the travel paths between the locations. The system stores route data as graph nodes with geographic coordinates like latitude, longitude and weighted edges representing distances or time of travel. Users can register and log in to search for routes between two locations, view multiple possible routes with descriptions such as "scenic" or "fastest" or "low traffic"  etc., and select preferred routes. Additionally, users can contribute by adding intermediate nodes ("via" points) and providing route feedback with ratings and suggestions. The backend dynamically fetches route data using external APIs when no data exists and stores user contributions for continual improvement. The frontend displays routes interactively using React Leaflet, allowing real-time visualization of paths and user inputs.
This application will be built using the following tech stack:
Frontend: React.js with React LeafLet for map integration.
Backend: Spring boot using java
Database: MySQL for storing user, route, and graph data
Authentication: JWT-based secure login and session management.

### List of requirements (objectives): 

[comment]: # (You can add as many additional bullet points as necessary by adding an additional hyphon symbol '-' at the end of each list) 

Essential:
- Implementing user registration and authentication with JWT-based security.
- Geographic data stored as nodes and weighted edges in a relational databse(MySQL).
- Implementation of a routing algorithm (A*) to calculate optimal paths between locations.
- Allows users to search for the routes between the two locations and retrive the multiple route options.
- Integration of external routing APIs to fetch the routes dynamically when data is missing.
- Visually displaying the routes on a  interactive map using the React Leaflet with markers and polylines.
- Users can be enabled to add intermediate via-points and route descriptions for crowdsourcing.
- Allowing users to rate and provide feedback on routes after travelling.

Desirable:
- Supporting user profile managment including route history and saved routes.
- Including the estimated time travel and distance based on user current location.
- Based on users feedback and ratings suggesting the optimized routes.
- Implementing the admin dashboard for monitoring all the activities of users and routes.

Optional:
- Adding real-time traffic or maybe road condition overlays to route descriptions.
- Implementing multimodal routing for example driving, walking,cycling.
- Incorporating AI/ML features to analyze real-time weather and road conditions and dynamically avoid unsafe routes based on the users vehicle type and conditions.
- Implementing voice assistant to interact the application with hands free.

# WayFinder Setup Guide

Follow these steps in order. If you do them one by one, the project will run without issues.  


# 1.Clone the repository
```bash
git clone <YOUR_REPO_URL>
cd <YOUR_REPO_FOLDER>
```

Inside, you will see 3 main folders:
```
Frontend/   → React app (user interface)
backend/    → Spring Boot app (server & APIs)
backend/db/ → Database dump (routeplanner.sql)
```

# 2.Setup the Database (MySQL)

If you **already have MySQL**, just import our schema:

# Option A: Workbench (easy GUI way)
1. Open **MySQL Workbench**.  
2. Go to **Server → Data Import**.  
3. Choose **Import from Self-Contained File**.  
   Select:
   ```
   backend/db/routeplanner.sql
   ```
4. Import into a schema called **routeplanner**.  

# Option B: Command line
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS routeplanner;"
mysql -u root -p routeplanner < backend/db/routeplanner.sql
```

After this, you’ll have **all tables, triggers, and views** ready.


#3. Setup the Backend (Spring Boot)

# Step 1: Go to backend folder
```bash
cd backend
```

# Step 2: Edit database settings
Open:  
```
src/main/resources/application.properties
```

Add these lines (change user/password if needed):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/routeplanner
spring.datasource.username={username}
spring.datasource.password={password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### Step 3: Add Gemini API key (optional features)
Please have a **Gemini API key**, add:
```properties
app.gemini.apiKey=YOUR_GEMINI_KEY
app.gemini.model=gemini-2.5-flash
app.gemini.url=https://generativelanguage.googleapis.com/v1beta
app.gemini.enabled=true
```


# Step 4: Run backend
```bash
./mvnw spring-boot:run   # (Mac/Linux)
mvnw spring-boot:run     # (Windows)
```

Backend runs on: http://localhost:8080


# 4.Setup the Frontend (React)

# Step 1: Go to frontend folder
```bash
cd ../Frontend
```

# Step 2: Install dependencies
```bash
npm install
```

# Step 3: Run the app
```bash
npm start
```

Frontend runs on: http://localhost:3000


# 5.That’s it
- Open **http://localhost:3000** in your browser.  
- Frontend will talk to backend on **http://localhost:8080**.  
- Backend will use MySQL database `routeplanner`.  

# Recap (3 things to check)
1. **Database** → Import `backend/db/routeplanner.sql`.  
2. **Backend** → Add DB details + (optional) Gemini API key.  
3. **Frontend** → Run `npm start`.  

Now everything works 


## Information about this repository
This is the repository that you are going to use **individually** for developing your project. Please use the resources provided in the module to learn about **plagiarism** and how plagiarism awareness can foster your learning.

Regarding the use of this repository, once a feature (or part of it) is developed and **working** or parts of your system are integrated and **working**, define a commit and push it to the remote repository. You may find yourself making a commit after a productive hour of work (or even after 20 minutes!), for example. Choose commit message wisely and be concise.

Please choose the structure of the contents of this repository that suits the needs of your project but do indicate in this file where the main software artefacts are located.
