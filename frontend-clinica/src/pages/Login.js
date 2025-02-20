import React, { useState } from "react";

const Login = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);

  const signIn = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch("https://eu-west-3akiycc7tp.auth.eu-west-3.amazoncognito.com/oauth2/token", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: new URLSearchParams({
          grant_type: "password",
          client_id: "eu-west-3_akIyCC7tP",
          username: username,
          password: password,
        }),
      });

      const data = await response.json();
      if (data.access_token) {
        localStorage.setItem("token", data.access_token);
        window.location.href = "/dashboard"; // Redirige después de iniciar sesión
      } else {
        throw new Error(data.error_description || "Login failed");
      }
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <div>
      <h2>Login</h2>
      {error && <p style={{ color: "red" }}>{error}</p>}
      <form onSubmit={signIn}>
        <input type="text" placeholder="Username" value={username} onChange={(e) => setUsername(e.target.value)} required />
        <input type="password" placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit">Sign In</button>
      </form>
    </div>
  );
};

export default Login;
