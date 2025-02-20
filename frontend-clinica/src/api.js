export async function getUserInfo() {
    const token = localStorage.getItem("token");
  
    if (!token) {
      console.error("No hay token en el almacenamiento local.");
      return null;
    }
  
    try {
      const response = await fetch("http://localhost:8080/auth/userinfo", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
  
      if (!response.ok) {
        throw new Error("Error en la solicitud: " + response.statusText);
      }
  
      return await response.json();
    } catch (error) {
      console.error("Error obteniendo la información del usuario:", error);
      return null;
    }
  }
  