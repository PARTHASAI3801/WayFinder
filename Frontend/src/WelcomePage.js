import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./WelcomePage.css";

function WelcomePage() {
  const [greeting, setGreeting] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const hour = new Date().getHours();
    if (hour < 12) setGreeting("Hello Good Morning!");
    else if (hour < 18) setGreeting("Hello Good Afternoon!");
    else setGreeting("Hello Good Evening!");
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    try {
      const response = await fetch("http://localhost:8080/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ 
            uname: username, 
            password: password }),
      });

      const data = await response.json();

      if (response.ok) {
        localStorage.setItem("token", data.token);
        localStorage.setItem("username", data.username);
        localStorage.setItem("userId",data.userId);
        console.log("Stored userId:", localStorage.getItem("userId"));
        console.log("stored username:", localStorage.getItem("username"));
        setMessage(data.message || "Login successful");
        // Redirect after 2 seconds
        setTimeout(() => {
          navigate("/routeFinder");
        }, 2000);
      } else {
        setError(data.message || "Invalid credentials");
      }
    } catch (err) {
      setError("Network error, try again later.");
    }
  };

  return (
    <div className="welcome-container">
      <div className="left-side">
        <div className="welcome-text">
          <h1>
            Welcome to <span className="highlight">WayFinder</span>
          </h1>
          <p className="subtext">Your smart route path finder</p>
        </div>
        <img
          src="/Paper map-cuate.png"
          alt="Map Illustration"
          className="welcome-image"
        />
      </div>

      <div className="right-side">
        <form className="login-form" onSubmit={handleLogin}>
          <div className="greeting-text">{greeting}</div>
          <h2>Login Your Account</h2>

          <input
            type="text"
            placeholder="Username"
            required
            className="input-field"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            required
            className="input-field"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button type="submit" className="submit-btn">
            Submit
          </button>

          <p className="not-registered">
            Don&apos;t have Account?{" "}
            <span className="create-account" onClick={() => navigate("/register")}>
              Create Account
            </span>
          </p>

          {message && <p className="success-message">{message}</p>}
          {error && <p className="error-message">{error}</p>}
        </form>
      </div>
    </div>
  );
}

export default WelcomePage;
