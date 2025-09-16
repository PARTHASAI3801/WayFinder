// App.js
import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import WelcomePage from "./WelcomePage";
import RegisterPage from "./RegisterPage";
import MultiRouteMap from "./MultiRouteMap";
import ContributeRoute from "./ContributeRoute";
import LandingPage from "./LandingPage";


function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/LandingPage" />} />
      <Route path="/welcome" element={<WelcomePage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/routeFinder" element={<MultiRouteMap />} />
      <Route path="/contribute" element={<ContributeRoute />}/>
      <Route path="/LandingPage" element={<LandingPage />}/>
    </Routes>
  );
}

export default App;
