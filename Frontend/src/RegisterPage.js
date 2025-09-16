import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./WelcomePage.css";

function RegisterPage() {
  const navigate = useNavigate();

  // State for form inputs
  const [fullName, setFullName] = useState("");
  const [uname, setUname] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  // State for messages
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();

    setMessage(null);
    setError(null);

    // Client-side password match validation
    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    // Prepare data object as per API
    const body = {
      uname,
      password,
      confirmPassword,
      fullName,
    };

    try {
      const response = await fetch("http://localhost:8080/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage("User registered successfully");
        setError(null);
        // navigate to login or welcome page after delay
        setTimeout(() => {
          navigate("/welcome");
        }, 2000);
      } else {
        // If backend returns an error message
        setError(data.message || "Registration failed");
        setMessage(null);
      }
    } catch (err) {
      setError("Network error, please try again later.");
      setMessage(null);
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
          src="/Login-rafiki.png"
          alt="Map Illustration"
          className="welcome-image"
        />
      </div>

      <div className="right-side">
        <form className="login-form" onSubmit={handleSubmit}>
          <h2>Create Your Account</h2>

          <input
            type="text"
            placeholder="Enter FullName"
            required
            className="input-field"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
          />

          <input
            type="text"
            placeholder="Enter UserName"
            required
            className="input-field"
            value={uname}
            onChange={(e) => setUname(e.target.value)}
          />

          <input
            type="password"
            placeholder="Enter password"
            required
            className="input-field"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <input
            type="password"
            placeholder="Confirm password"
            required
            className="input-field"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />

          <p className="not-registered">
            Already have Account?{" "}
            <span className="create-account" onClick={() => navigate("/welcome")}>
              Login
            </span>
          </p>

          <button type="submit" className="submit-btn">
            Register
          </button>

          {message && <p className="success-message">{message}</p>}
          {error && <p className="error-message">{error}</p>}
        </form>
      </div>
    </div>
  );
}

export default RegisterPage;
